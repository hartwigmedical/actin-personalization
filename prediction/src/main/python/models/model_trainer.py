import numpy as np
import pandas as pd

from sklearn.model_selection import StratifiedKFold, KFold
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler

from sksurv.util import Surv

import torch
import dill
import os

from typing import List, Dict, Tuple, Any, Optional, Callable
from joblib import Parallel, delayed

from .survival_models import BaseSurvivalModel, NNSurvivalModel
from utils.metrics import calculate_time_dependent_c_index, calculate_brier_score, calibration_assessment, calculate_time_dependent_auc
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
        
        return list(skf.split(X, stratify_labels))
    
    def _initialize_model(self, model_template: BaseSurvivalModel, input_size: Optional[int] = None) -> BaseSurvivalModel:
        model_class = type(model_template)
        kwargs = getattr(model_template, 'kwargs', {})

        if issubclass(model_class, NNSurvivalModel):
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

        else:
            with open(model_file + ".pkl", "wb") as f:
                dill.dump(model, f)
                
    @staticmethod
    def _set_attention_indices(model: BaseSurvivalModel, feature_names: List[str]):
        
        if not (isinstance(model, NNSurvivalModel) and hasattr(model.net, 'attention')):
            return

        attn = model.net.attention

        if 'hasMsi' in feature_names:
            attn.msi_index = feature_names.index('hasMsi')
        else:
            attn.msi_index = None

        immuno_cols = [
            'systemicTreatmentPlan_pembrolizumab',
            'systemicTreatmentPlan_nivolumab'
        ]
        attn.immuno_index = [feature_names.index(c) for c in immuno_cols if c in feature_names] or None

        if 'hasRasMutation' in feature_names:
            attn.ras_index = feature_names.index('hasRasMutation')
        else:
            attn.ras_index = None

        if 'systemicTreatmentPlan_panitumumab' in feature_names:
            attn.panitumumab_index = feature_names.index('systemicTreatmentPlan_panitumumab')
        else:
            attn.panitumumab_index = None

        treatment_idxs = [
            i for i, f in enumerate(feature_names)
            if f.startswith('systemicTreatmentPlan_') or f == 'treatment'
        ]
        
        attn.treatment_indices = treatment_idxs if treatment_idxs else None

    
    def _prepare_fold_data(
        self, 
        X: pd.DataFrame, 
        y: pd.DataFrame, 
        indices: Tuple[np.ndarray, np.ndarray]
    ) -> Tuple[pd.DataFrame, np.ndarray, pd.DataFrame, np.ndarray, pd.DataFrame]:
        train_idx, val_idx = indices
        X_train, X_val = X.iloc[train_idx], X.iloc[val_idx]

        y_df = pd.DataFrame({'duration': y[self.settings.duration_col], 'event': y[self.settings.event_col]}, index=X.index)
        y_train_df, y_val_df = y_df.iloc[train_idx], y_df.iloc[val_idx]

        y_train_structured = Surv.from_dataframe('event', 'duration', y_train_df)
        y_val_structured = Surv.from_dataframe('event', 'duration', y_val_df)

        return X_train, y_train_structured, X_val, y_val_structured, y_val_df

    def _get_survival_metrics(
        self, 
        model: BaseSurvivalModel, 
        model_name: str, 
        surv_funcs: Optional[List[Callable[[np.ndarray], np.ndarray]]],
        risk_scores: np.ndarray, 
        X_val: pd.DataFrame, 
        y_val_structured: np.ndarray
    ) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:  
        
        max_follow_up = y_val_structured['duration'].max()
        min_follow_up = y_val_structured['duration'].min()
        upper_bound = min(self.max_time, max_follow_up)
        
        if surv_funcs is not None and model_name != 'AalenAdditive':
            max_times = [fn.x[-1] for fn in surv_funcs]
            global_max_time = min(max_times)
            upper_bound = min(upper_bound, global_max_time)
         
        if upper_bound < 30: 
            times = np.array([upper_bound])
        else:
            start_time = max(30, min_follow_up)
            months_count = int((upper_bound - 30) // 30) + 1
            end_time = start_time + (months_count - 1)*30
            times = np.arange(start_time, end_time + 1, 30)
    
            if times.size == 0:
                times = np.array([upper_bound])
    
        times = times[(times > min_follow_up) & (times < max_follow_up)].astype(int)
        
        if surv_funcs is None:  
            predictions = risk_scores
            auc_input = -risk_scores
        else:
            if model_name == 'AalenAdditive':
                predictions = model.predict_survival_function(X_val, times=times)
            else:
                predictions = np.row_stack([fn(times) for fn in surv_funcs])
            auc_input = -predictions 

        return times, predictions, auc_input

    def _evaluate_model(
        self, 
        model: BaseSurvivalModel, 
        X_val: pd.DataFrame, 
        y_train_structured: pd.DataFrame, 
        y_val_structured: pd.DataFrame, 
        model_name: str
    ) -> Dict[str, List[float]]:
        results = {}

        try:
            surv_funcs = model.predict_survival_function(X_val)
        except AttributeError:
            surv_funcs = None

        if model_name == 'AalenAdditive':
            durations = y_val_structured['duration']
            risk_scores = model.predict(X_val, durations=durations)
        else:
            risk_scores = model.predict(X_val)

        times, predictions, auc_input = self._get_survival_metrics(
            model, model_name, surv_funcs, risk_scores, X_val, y_val_structured
        )

        results['c_index'] = calculate_time_dependent_c_index(predictions, y_val_structured['duration'], y_val_structured['event'], times)
        results['ce'] = calibration_assessment(predictions, y_val_structured, times)
        
        max_train_time = float(y_train_structured["duration"].max())  - 1e-5
        safe_times = times[times < (max_train_time)]
        y_val_clipped = y_val_structured.copy()
        y_val_clipped["duration"] = np.minimum(y_val_clipped["duration"], max_train_time)
        
        results['ibs'] = calculate_brier_score(y_train_structured, y_val_clipped, predictions, safe_times)
        
        _, mean_auc = calculate_time_dependent_auc(y_train_structured,y_val_clipped,auc_input,safe_times)
        results['auc'] = mean_auc

        return results
    
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

        model = self._initialize_model(model_template, input_size=X.shape[1])
        ModelTrainer._set_attention_indices(model, self.feature_names)

        if isinstance(model, NNSurvivalModel):
            val_data = (X_val.values.astype('float32'), y_val_struct)
            model.fit(X_tr, y_tr_struct, val_data=val_data)
        else:
            model.fit(X_tr, y_tr_struct)

        metrics = self._evaluate_model(model, X_val, y_tr_struct, y_val_struct, model_name)
        
        return metrics
    
    def train_and_evaluate(
        self, 
        X_train:  pd.DataFrame, y_train: pd.DataFrame, 
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
            final_model = self._initialize_model(model_template, input_size=X_train.shape[1])
            ModelTrainer._set_attention_indices(final_model,
                                                    self.feature_names)
            
            y_train_df = pd.DataFrame({'duration': y_train[self.settings.duration_col], 'event': y_train[self.settings.event_col]}, index=X_train.index)
            y_train_structured = Surv.from_dataframe('event', 'duration', y_train_df)
            y_test_df = pd.DataFrame({'duration': y_test[self.settings.duration_col], 'event': y_test[self.settings.event_col]}, index=X_test.index)
            y_test_structured = Surv.from_dataframe('event', 'duration', y_test_df)

            if isinstance(final_model, NNSurvivalModel):
                X_train_final, X_val_final, y_train_final, y_val_final = train_test_split(X_train, y_train_df, test_size=0.1, random_state=self.random_state)
        
                y_train_structured_final = Surv.from_dataframe('event', 'duration', y_train_final)
                y_val_structured_final = Surv.from_dataframe('event', 'duration', y_val_final)
                val_data = (X_val_final.values.astype('float32'), y_val_structured_final)

                final_model.fit(X_train_final, y_train_structured_final, val_data=val_data)
            else:
                final_model.fit(X_train, y_train_structured)
                
                
            holdout_metrics = self._evaluate_model(
               final_model, X_test, y_train_structured, y_test_structured, model_name
            )
            
            if self.settings.save_models:
                self.save_model(final_model, model_name)
                
            print(f"{model_name} Hold-Out Results: {holdout_metrics}")

            self.trained_models[model_name] = final_model
            self.results[model_name]['holdout'] = holdout_metrics

        return self.results, self.trained_models

