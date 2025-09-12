# models/model_trainer.py
import numpy as np
import pandas as pd

from sklearn.model_selection import StratifiedKFold, KFold
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler

from sksurv.util import Surv

import torch
import dill
import os
import re

from typing import List, Dict, Tuple, Any, Optional, Callable
from joblib import Parallel, delayed

from models.models import *
from utils.metrics import evaluate_all_metrics

from utils.settings import config_settings

class ModelTrainer:
    def __init__(self, models: Dict[str, BaseSurvivalModel], settings = config_settings, random_state: int = 42):
        self.settings = settings
        self.models = models
        self.n_splits = self.settings.cross_val_n_splits
        self.random_state = random_state
        self.results = dict()
        self.trained_models = dict()
        self.feature_names: List[str] = [] 
        self.max_time = self.settings.max_time

    def cross_validate(self, X: pd.DataFrame, y: pd.DataFrame, encoded_columns: Dict[str, List[str]]) -> List[Tuple[np.ndarray, np.ndarray]]:
        if self.settings.event_col in y.dtype.names:
            splitter = StratifiedKFold(n_splits=self.n_splits, shuffle=True, random_state=self.random_state)
            return list(splitter.split(X, y[self.settings.event_col].astype(str)))
        else:
            splitter = KFold(n_splits=self.n_splits, shuffle=True, random_state=self.random_state)
            return list(splitter.split(X))
    
    def _initialize_model(self, model_template: BaseSurvivalModel, input_size: Optional[int] = None) -> BaseSurvivalModel:
        model_class = type(model_template)
        kwargs = getattr(model_template, 'kwargs', {})

        if isinstance(model_template, (NNSurvivalModel, MultiTaskNNSurvivalModel)):
            kwargs['input_size'] = input_size
   
        return model_class(**kwargs)
    
    def save_model(self, model: BaseSurvivalModel, model_name: str)-> None:
        if not os.path.exists(self.settings.save_path):
            os.makedirs(self.settings.save_path)
            
        model_file = os.path.join(self.settings.save_path, f"{self.settings.outcome}_{model_name}")

        if isinstance(model, NNSurvivalModel):
         
            state = {'net_state': model.model.net.state_dict()}
            if hasattr(model, 'labtrans'):
                state['labtrans'] = model.labtrans
            if hasattr(model.model, 'baseline_hazards_'):
                state['baseline_hazards'] = model.model.baseline_hazards_
                state['baseline_cumulative_hazards'] = model.model.baseline_cumulative_hazards_

            torch.save(state, model_file + ".pt")
            print(f"NN Model weights and baseline hazards for {model_name} saved to {model_file}.pt")

        elif isinstance(model, MultiTaskNNSurvivalModel):
            state = {
                'net_state': model.model.net.state_dict(),
                'num_tasks': model.num_tasks,
                'labtrans': model.labtrans,
            }
            if hasattr(model.model, 'baseline_hazards_'):
                state['baseline_hazards'] = model.model.baseline_hazards_
                state['baseline_cumulative_hazards'] = model.model.baseline_cumulative_hazards_

            torch.save(state, model_file + ".pt")
            print(f"MultiTaskNN Model weights and baseline hazards for {model_name} saved to {model_file}.pt")

        else:
            with open(model_file + ".pkl", "wb") as f:
                dill.dump(model, f)
                
    @staticmethod
    def _set_attention_indices(model: BaseSurvivalModel, feature_names: List[str]):
        def col_idx(name: str):
            return feature_names.index(name) if name in feature_names else None

        immuno_cols = [
            'systemicTreatmentPlan_pembrolizumab',
            'systemicTreatmentPlan_nivolumab'
        ]
        immuno_idx_list = [feature_names.index(c) for c in immuno_cols if c in feature_names]
        immuno_idx_list = immuno_idx_list if immuno_idx_list else None

        panitumumab_idx = col_idx('systemicTreatmentPlan_panitumumab')
        msi_idx = col_idx('hasMsi')
        ras_idx = col_idx('hasRasMutation')

        treatment_idxs = [
            i for i, f in enumerate(feature_names)
            if f.startswith('systemicTreatmentPlan_') or f == 'treatment'
        ] or None

        if isinstance(model, NNSurvivalModel) and hasattr(model.net, 'attention'):
            attn = model.net.attention
            attn.msi_index = msi_idx
            attn.immuno_index = immuno_idx_list
            attn.ras_index = ras_idx
            attn.panitumumab_index = panitumumab_idx
            attn.treatment_indices = treatment_idxs
                                                                                            
            return

        if isinstance(model, MultiTaskNNSurvivalModel):
            model.net.msi_index = msi_idx
            model.net.ras_index = ras_idx
                                                                                                  
        return
    
    def _prepare_fold_data(
        self, 
        X: pd.DataFrame, 
        y: pd.DataFrame, 
        indices: Tuple[np.ndarray, np.ndarray]
    ) -> Tuple[pd.DataFrame, np.ndarray, pd.DataFrame, np.ndarray, pd.DataFrame]:
        train_idx, val_idx = indices
        X_train, X_val = X.iloc[train_idx], X.iloc[val_idx]

        y_df = pd.DataFrame({'duration': y[self.settings.duration_col], 'event': y[self.settings.event_col]}, index=X.index)
        y_train_df, y_test_df = y_df.iloc[train_idx], y_df.iloc[val_idx]

        y_train_structured = Surv.from_dataframe('event', 'duration', y_train_df)
        y_test_structured = Surv.from_dataframe('event', 'duration', y_test_df)

        return X_train, y_train_structured, X_val, y_test_structured, y_test_df

    def _get_survival_metrics(
        self, 
        model: BaseSurvivalModel, 
        model_name: str, 
        surv_funcs: Optional[List[Callable[[np.ndarray], np.ndarray]]],
        risk_scores: np.ndarray, 
        X_val: pd.DataFrame, 
        y_val_structured: np.ndarray
    ) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:  
        
        grid = np.asarray(config_settings.fixed_time_bins, dtype=int)
        lower = int(y_val_structured['duration'].min())
        upper = int(min(self.max_time, y_val_structured['duration'].max()))

        times = grid[(grid > lower) & (grid < upper)]
        if times.size == 0:
            times = np.array([min(upper, grid[-1])])

        
        if surv_funcs is None:  
            predictions = risk_scores
            auc_input = -risk_scores
        else:
            if isinstance(model, MultiTaskNNSurvivalModel) and isinstance(surv_funcs, pd.DataFrame):
                df_disc = surv_funcs.reindex(times, method='nearest', fill_value='extrapolate')
                # df_disc is shape [len(times), n_samples], so transpose
                predictions = df_disc.values.T
                auc_input = -predictions
            else:
                predictions = np.row_stack([fn(times) for fn in surv_funcs])
                auc_input = -predictions 
                
        return times, predictions, auc_input

    def _evaluate_model(
        self, 
        model: BaseSurvivalModel, 
        X: pd.DataFrame, 
        y_train: np.ndarray, 
        y_test: np.ndarray, 
        model_name: str
    ) -> Dict[str, float]:
        return evaluate_all_metrics(
            model=model,
            model_name=model_name,
            X=X,
            y_train=y_train,
            y_test=y_test,
            settings=self.settings,
            feature_names=self.feature_names,
        )

    def _run_one_fold(
        self,
        model_name: str,
        model_template: BaseSurvivalModel,
        fold_indices: Tuple[np.ndarray, np.ndarray],
        X: pd.DataFrame,
        y: pd.DataFrame,
        encoded_columns: Dict[str, List[str]],
    ) -> Dict[str, float]:
        
        X_tr, y_tr_struct, X_val, y_val_struct, y_val_df = self._prepare_fold_data(X, y, fold_indices)
        input_size = X_tr.drop(columns=['treatment_group_idx']).shape[1]
        num_tasks_fold = int(max(X_tr['treatment_group_idx'].max(),
                         X_val['treatment_group_idx'].max())) + 1

        # pass into the template kwargs so _initialize_model picks it up
        if isinstance(model_template, MultiTaskNNSurvivalModel):
            model_template.kwargs['num_tasks'] = num_tasks_fold
   
        model = self._initialize_model(model_template, input_size=input_size)
        ModelTrainer._set_attention_indices(model, self.feature_names)

        if isinstance(model, NNSurvivalModel):
            X_tr_input = X_tr.drop(columns=['treatment_group_idx'], errors="ignore")
            val_data = (X_val.drop(columns=['treatment_group_idx'], errors="ignore").values.astype('float32'),
                        y_val_struct)
            model.fit(X_tr_input, y_tr_struct, val_data=val_data)

        elif isinstance(model, MultiTaskNNSurvivalModel):
            task_idx_tr = X_tr['treatment_group_idx'].values.astype(int)
            task_idx_val = X_val['treatment_group_idx'].values.astype(int)
            X_tr_input  = X_tr.drop(columns=['treatment_group_idx'])
            X_val_input = X_val.drop(columns=['treatment_group_idx'])
            model.fit(X_tr_input, task_idx_tr, y_tr_struct, val_data=(X_val_input, y_val_df))

        else:
            # Cox, RSF, DeepSurv, etc.
            X_tr_input = X_tr.drop(columns=['treatment_group_idx'], errors="ignore")
            model.fit(X_tr_input, y_tr_struct)

        metrics = self._evaluate_model(model, X_val, y_tr_struct, y_val_struct, model_name)
        
        return metrics
    
    def train_and_evaluate(
        self, 
        X_train: pd.DataFrame, y_train: pd.DataFrame, 
        X_test: pd.DataFrame, y_test: pd.DataFrame, 
        encoded_columns: Dict[str, List[str]], 
    ) -> Tuple[pd.DataFrame, Dict[str, BaseSurvivalModel]]:
        self.feature_names = X_train.columns.tolist()
        folds = self.cross_validate(X_train, y_train, encoded_columns)

        for model_name, model_template in self.models.items():
            print(f"training model: {model_name}")
            model_metrics = {'cv': {'c_index': [], 'ibs': [], 'ce': [], 'auc': []}}

            cv_fold_results = Parallel(n_jobs=self.settings.n_jobs , backend="threading", verbose=10)(
                delayed(self._run_one_fold)(
                    model_name,
                    model_template,
                    fold_idx,
                    X_train,
                    y_train,
                    encoded_columns)
                for fold_idx in folds)

            mean_results = {
                metric: np.mean([fold[metric] for fold in cv_fold_results])
                for metric in cv_fold_results[0]
            }
            self.results[model_name] = mean_results
            print(f"{model_name} CV Results: {mean_results}")
            
            print("training final model")
            num_tasks_final = int(max(X_train['treatment_group_idx'].max(), X_test['treatment_group_idx'].max())) + 1
            if isinstance(model_template, MultiTaskNNSurvivalModel):
                model_template.kwargs['num_tasks'] = num_tasks_final

            final_model = self._initialize_model(
                model_template, input_size=X_train.drop(columns=['treatment_group_idx']).shape[1]
            )

            ModelTrainer._set_attention_indices(final_model, self.feature_names)

            y_train_df = pd.DataFrame({'duration': y_train[self.settings.duration_col], 'event': y_train[self.settings.event_col]}, index=X_train.index)
            y_train_structured = Surv.from_dataframe('event', 'duration', y_train_df)
            y_test_df = pd.DataFrame({'duration': y_test[self.settings.duration_col], 'event': y_test[self.settings.event_col]}, index=X_test.index)
            y_test_structured = Surv.from_dataframe('event', 'duration', y_test_df)

            task_idx_train = X_train['treatment_group_idx'].values.astype(int)
            X_train_input  = X_train.drop(columns=['treatment_group_idx'])

            if isinstance(final_model, NNSurvivalModel):
                X_train_final, X_val_final, y_train_final, y_val_final = train_test_split(X_train_input, y_train_df, test_size=0.1, random_state=self.random_state)
        
                y_train_structured_final = Surv.from_dataframe('event', 'duration', y_train_final)
                y_val_structured_final = Surv.from_dataframe('event', 'duration', y_val_final)
                val_data = (X_val_final.values.astype('float32'), y_val_structured_final)

                final_model.fit(X_train_final, y_train_structured_final, val_data=val_data)
                
            elif isinstance(final_model, MultiTaskNNSurvivalModel):
                final_model.fit(X_train_input, task_idx_train, y_train_structured)

            else:
                final_model.fit(X_train_input, y_train_structured)
                
                
            holdout_metrics = self._evaluate_model(
               final_model, X_test, y_train_structured, y_test_structured, model_name
            )
            
            if self.settings.save_models:
                self.save_model(final_model, model_name)
                
            print(f"{model_name} Hold-Out Results: {holdout_metrics}")

            self.trained_models[model_name] = final_model
            self.results[model_name]['holdout'] = holdout_metrics

        return self.results, self.trained_models
