import numpy as np
import pandas as pd

from sklearn.model_selection import StratifiedKFold
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler

from sksurv.util import Surv
import os
import torch
import joblib
import dill

from lifelines.utils import concordance_index
from typing import Dict, Any, Tuple, List

from ..utils.metrics import calculate_c_index, calculate_brier_score, calibration_assessment, calculate_time_dependent_auc
from .survival_models import BaseSurvivalModel, NNSurvivalModel

class ModelTrainer:
    def __init__(self, models, n_splits=5, random_state=42):
        """
        Initialize ModelTrainer with multiple models.
        
        Args:
            models: A dictionary of model instances to train and evaluate.
            n_splits: Number of folds for cross-validation.
            random_state: Random seed for reproducibility.
        """
        self.models = models
        self.n_splits = n_splits
        self.random_state = random_state
        self.results = dict()
        self.trained_models = dict()

    def cross_validate(self, X, y, event_col, encoded_columns):
        """
        Perform stratified k-fold cross-validation.

        Args:
            X: Feature DataFrame.
            y: Target DataFrame.
            encoded_columns: Dictionary of encoded column mappings.

        Returns:
            A list of train-test index splits for cross-validation.
        """
        skf = StratifiedKFold(n_splits=self.n_splits, shuffle=True, random_state=self.random_state)
      
        return list(skf.split(X, y[event_col].astype(str)))
    
    def _initialize_model(self, model_template, input_size=None):
        """
        Create a new instance of a model from the template.
        """
        model_class = type(model_template)
        kwargs = getattr(model_template, 'kwargs', {})

        if issubclass(model_class, NNSurvivalModel):
            kwargs['input_size'] = input_size
        
        return model_class(**kwargs)
    
    def save_model(self, model, model_name, title, save_path="src/models/trained_models"):
        if not os.path.exists(save_path):
            os.makedirs(save_path)

        model_file = os.path.join(save_path, f"{title}_{model_name}")

        if isinstance(model, NNSurvivalModel):
            torch.save(model.model.net.state_dict(), model_file + ".pt")
            print(f"NN Model weights for {model_name} saved to {model_file}.pt")
        else:
            with open(model_file + ".pkl", "wb") as f:
                dill.dump(model, f)
    
    def _prepare_fold_data(self, X: pd.DataFrame, y: pd.DataFrame, indices: Tuple[np.ndarray, np.ndarray], event_col: str, duration_col: str):
        """
        Prepare training and validation data for a fold.

        Args:
            X: Feature DataFrame.
            y: Target DataFrame (structured array or DataFrame).
            indices: Train and validation indices.
            event_col: Column name for event indicator.
            duration_col: Column name for duration.

        Returns:
            Prepared training and validation data, including feature DataFrames and survival-structured arrays.
        """
        train_idx, val_idx = indices
        X_train, X_val = X.iloc[train_idx], X.iloc[val_idx]

        y_df = pd.DataFrame({'duration': y[duration_col], 'event': y[event_col]}, index=X.index)
        y_train_df, y_val_df = y_df.iloc[train_idx], y_df.iloc[val_idx]

        y_train_structured = Surv.from_dataframe('event', 'duration', y_train_df)
        y_val_structured = Surv.from_dataframe('event', 'duration', y_val_df)

        return X_train, y_train_structured, X_val, y_val_structured, y_val_df

    def _get_survival_metrics(self, model, model_name, surv_funcs, risk_scores, X_val, y_val_structured):
        """
        Helper function to handle survival function and risk score calculations.

        Returns:
            Tuple containing times, predictions, and risk_scores for AUC calculation.
        """
        max_follow_up = y_val_structured['duration'].max()
        upper_bound = min(1825, max_follow_up)
        if surv_funcs is not None and model_name != 'AalenAdditive':
            max_times = [fn.x[-1] for fn in surv_funcs]
            global_max_time = min(max_times)
            upper_bound = min(upper_bound, global_max_time)
            
        if upper_bound < 30: 
            times = np.array([upper_bound])
        else: # Calculate how many monthly intervals we can have from 30 days to upper_bound
            months_count = int((upper_bound - 30) // 30) + 1
            end_time = 30 + (months_count - 1)*30
            times = np.arange(30, end_time + 1, 30)
            times = times[times <= upper_bound]

            if times.size == 0:
                times = np.array([upper_bound])
        
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

    def _evaluate_model(self, model, X_val, y_train_structured, y_val_structured, model_name):
        """
        Evaluate a model on validation data.

        Returns:
            A dictionary of evaluation metrics.
        """
        results = {}

        # Predict risk scores or survival functions
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

        # Calculate metrics
        results['c_index'] = calculate_c_index(
            y_val_structured['duration'], risk_scores, y_val_structured['event']
        )
        results['ibs'] = calculate_brier_score(y_train_structured, y_val_structured, predictions, times)
        results['ce'] = calibration_assessment(predictions, y_val_structured, times)

        auc_times, mean_auc = calculate_time_dependent_auc(y_train_structured, y_val_structured, auc_input, times)
        results['auc'] = mean_auc

        return results

    
    def train_and_evaluate(self, X_train, y_train, X_test, y_test, encoded_columns, event_col, duration_col, title, save_models=False, save_path="src/models/trained_models"):
        """
        Train and evaluate all models with cross-validation and hold-out evaluation.

        Args:
            X_train, y_train: Training data.
            X_test, y_test: Hold-out test data.
            encoded_columns: Dictionary of encoded columns.
            event_col, duration_col: Survival event and duration columns.

        Returns:
            results: Nested dictionary of evaluation metrics.
            trained_models: Dictionary of final trained models.
        """
        folds = self.cross_validate(X_train, y_train, event_col, encoded_columns)

        for model_name, model_template in self.models.items():
            print(f"training model: {model_name}")
            model_metrics = {'cv': {'c_index': [], 'ibs': [], 'ce': [], 'auc': []}}

            # Cross-validation
            for fold_indices in folds:
                X_fold_train, y_fold_train_structured, X_fold_val, y_fold_val_structured, y_fold_val_df = self._prepare_fold_data(
                    X_train, y_train, fold_indices, event_col, duration_col
                )
                model = self._initialize_model(model_template, input_size=X_train.shape[1])

                if isinstance(model, NNSurvivalModel):
                    val_data = (X_fold_val.values.astype('float32'), y_fold_val_structured)
                    model.fit(X_fold_train, y_fold_train_structured, val_data=val_data)
                else:
                    model.fit(X_fold_train, y_fold_train_structured)

                metrics = self._evaluate_model(
                   model, X_fold_val, y_fold_train_structured, y_fold_val_structured, model_name
                )
                for key, value in metrics.items():
                    model_metrics['cv'][key].append(value)

            self.results[model_name] = {key: np.nanmean(values) for key, values in model_metrics['cv'].items()}
            print(f"{model_name} CV Results: {self.results[model_name]}")

            # Train final model on the entire training set
            final_model = self._initialize_model(model_template, input_size=X_train.shape[1])
            y_train_df = pd.DataFrame({'duration': y_train[duration_col], 'event': y_train[event_col]}, index=X_train.index)
            y_train_structured = Surv.from_dataframe('event', 'duration', y_train_df)
            y_test_df = pd.DataFrame({'duration': y_test[duration_col], 'event': y_test[event_col]}, index=y_test.index)
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
            
            if save_models:
                self.save_model(final_model, model_name, title, save_path=save_path)
                
            print(f"{model_name} Hold-Out Results: {holdout_metrics}")

            self.trained_models[model_name] = final_model
            self.results[model_name]['holdout'] = holdout_metrics

        return self.results, self.trained_models


