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
from ..utils.utils import stratify_by_treatment
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

    def cross_validate(self, X, y, treatment_col, encoded_columns):
        """
        Perform stratified k-fold cross-validation.

        Args:
            X: Feature DataFrame.
            y: Target DataFrame.
            treatment_col: The column name representing treatments.
            encoded_columns: Dictionary of encoded column mappings.

        Returns:
            A list of train-test index splits for cross-validation.
        """
        skf = StratifiedKFold(n_splits=self.n_splits, shuffle=True, random_state=self.random_state)
        stratify_labels = stratify_by_treatment(X, treatment_col, encoded_columns)
    
        return list(skf.split(X, stratify_labels))
    
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
    


    
    def _get_survival_metrics(self, model, model_name, surv_funcs, risk_scores, X_val, y_val_df, y_val_structured):
        """
        Helper function to handle survival function and risk score calculations.

        Args:
            model_name: Name of the model.
            surv_funcs: Predicted survival functions (if available).
            risk_scores: Predicted risk scores (if survival functions are not available).
            X_val: Validation features.
            y_val_df: Validation target DataFrame.
            y_val_structured: Structured array of validation targets.

        Returns:
            Tuple containing times, predictions, and auc_input for further metric calculations.
        """
        if surv_funcs is None:  # Risk score-based models
            times = np.linspace(
                y_val_structured['duration'].min(),
                np.nextafter(y_val_structured['duration'].max(), -np.inf),
                100
            )
            predictions = risk_scores
            auc_input = -risk_scores
        else:  # Survival probability-based models
            if model_name == 'AalenAdditive':
                coef_times = surv_funcs.index.values
                times_min = max(y_val_structured['duration'].min(), coef_times[0])
                times_max = min(y_val_structured['duration'].max(), coef_times[-1])
                times = np.linspace(times_min, np.nextafter(times_max, -np.inf), 100, endpoint=False)
                predictions = model.predict_survival_function(X_val, times=times)
            else:
                times_min = max(y_val_structured['duration'].min(), surv_funcs[0].x[0])
                times_max = min(y_val_structured['duration'].max(), surv_funcs[0].x[-1])
                times = np.linspace(times_min, np.nextafter(times_max, -np.inf), 100, endpoint=False)
                predictions = np.row_stack([fn(times) for fn in surv_funcs])

            mid_time = np.median(times)
            idx = np.searchsorted(times, mid_time)
            auc_input = -predictions[:, idx]

        return times, predictions, auc_input


    def _evaluate_model(self, model, X_val, y_train_structured, y_val_structured, y_val_df, model_name, event_col):
        """
        Evaluate a model on validation data.

        Args:
            model: The trained model.
            X_val: Validation feature DataFrame.
            y_train_structured: Structured array of training targets.
            y_val_structured: Structured array of validation targets.
            y_val_df: Validation DataFrame with duration and event columns.
            model_name: Name of the model.
            event_col: Column name for event indicator.

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
            durations = y_val_df['duration'].values
            risk_scores = model.predict(X_val, durations=durations)
        else:
            risk_scores = model.predict(X_val)
    
        times, predictions, auc_input = self._get_survival_metrics(
            model, model_name, surv_funcs, risk_scores, X_val, y_val_df, y_val_structured
        )
        
        # Calculate metrics
        results['c_index'] = calculate_c_index(
            y_val_structured['duration'], risk_scores, y_val_structured['event'].astype(bool)
        )
        results['ibs'] = calculate_brier_score(y_train_structured, y_val_structured, predictions, times)
        results['ce'] = calibration_assessment(predictions, y_val_structured, times)
        auc_times, auc_scores = calculate_time_dependent_auc(y_train_structured, y_val_structured, auc_input, times)
        results['auc'] = np.nanmean(auc_scores)

        return results    
    
    def train_and_evaluate(self, X_train, y_train, X_test, y_test, treatment_col, encoded_columns, event_col, duration_col, title, save_models=True, save_path="src/models/trained_models"):
        """
        Train and evaluate all models with cross-validation and hold-out evaluation.

        Args:
            X_train, y_train: Training data.
            X_test, y_test: Hold-out test data.
            treatment_col: Column name for treatment stratification.
            encoded_columns: Dictionary of encoded columns.
            event_col, duration_col: Survival event and duration columns.

        Returns:
            results: Nested dictionary of evaluation metrics.
            trained_models: Dictionary of final trained models.
        """
        folds = self.cross_validate(X_train, y_train, treatment_col, encoded_columns)

        for model_name, model_template in self.models.items():
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
                    model,
                    X_fold_val,
                    y_fold_train_structured,
                    y_fold_val_structured,
                    y_fold_val_df,
                    model_name,
                    event_col
                )
                for key, value in metrics.items():
                    model_metrics['cv'][key].append(value)

            # Average cross-validation metrics
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

            # Evaluate on hold-out set
            holdout_metrics = self._evaluate_model(
                final_model,
                X_test,
                y_train_structured,
                y_test_structured,
                y_test_df,
                model_name,
                event_col
            )
            print(f"{model_name} Hold-Out Results: {holdout_metrics}")

            self.trained_models[model_name] = final_model
            self.results[model_name]['holdout'] = holdout_metrics
            
            if save_models:
                self.save_model(final_model, model_name, title, save_path=save_path)

        return self.results, self.trained_models


