import numpy as np
import pandas as pd
from sklearn.model_selection import StratifiedKFold
from sklearn.preprocessing import MinMaxScaler

from sksurv.util import Surv

from lifelines.utils import concordance_index
from metrics import calculate_c_index, calculate_brier_score, calibration_assessment

class ModelTrainer:
    def __init__(self, models, n_splits=5, random_state=42):
        """
        models: A dictionary of model instances to train and evaluate.
        n_splits: Number of folds for cross-validation.
        """
        self.models = models
        self.n_splits = n_splits
        self.random_state = random_state
        self.results = dict()

    def stratify_by_treatment(self, X, treatment_col, encoded_columns):
        """
        Create a stratification label based only on treatment type.
        """
        if treatment_col in encoded_columns:
            treatment_col_encoded = encoded_columns[treatment_col]
            if len(treatment_col_encoded) == 1:
                treatment_data = X[treatment_col_encoded[0]]
            else:
                treatment_data = X[treatment_col_encoded].idxmax(axis=1)
        else:
            treatment_data = X[treatment_col]

        return treatment_data.astype(str)

    def cross_validate(self, X, y, treatment_col, encoded_columns):
        """
        Perform stratified k-fold cross-validation.
        """
        skf = StratifiedKFold(n_splits=self.n_splits, shuffle=True, random_state=self.random_state)
        stratify_labels = self.stratify_by_treatment(X, treatment_col, encoded_columns)
        folds = list(skf.split(X, stratify_labels))
        return folds

 
    def train_and_evaluate(self, X_train, y_train, X_test, y_test, treatment_col, encoded_columns, event_col, duration_col):
        """
        Train and evaluate models.
        """
        folds = self.cross_validate(X_train, y_train, treatment_col, encoded_columns)
        for model_name, model in self.models.items():
            print(f"Training {model_name}...")
            c_indices = []
            ibs_scores = []
            calibration_errors = []
            
            for fold_idx, (train_idx, val_idx) in enumerate(folds):
                X_fold_train = X_train.iloc[train_idx]
                y_fold_train = y_train[train_idx]
                X_fold_val = X_train.iloc[val_idx]
                y_fold_val = y_train[val_idx]
            
                # Convert structured arrays to DataFrames with matching indices
                y_fold_train_df = pd.DataFrame({
                    'duration': y_fold_train[duration_col],
                    'event': y_fold_train[event_col]
                }, index=X_fold_train.index)

                y_fold_val_df = pd.DataFrame({
                    'duration': y_fold_val[duration_col],
                    'event': y_fold_val[event_col]
                }, index=X_fold_val.index) 
                
                # Reset indices for both features and target
                X_fold_train.reset_index(drop=True, inplace = True)
                y_fold_train_df.reset_index(drop=True, inplace = True)
                X_fold_val.reset_index(drop=True, inplace = True)
                y_fold_val_df.reset_index(drop=True, inplace = True)
                
                # Prepare survival data
                y_fold_train_structured = Surv.from_dataframe('event', 'duration', y_fold_train_df)
                y_fold_val_structured = Surv.from_dataframe('event', 'duration', y_fold_val_df)           

                model.fit(X_fold_train, y_fold_train_structured)
                
                try:
                    surv_funcs = model.predict_survival_function(X_fold_val)
                except AttributeError:
                    surv_funcs = None
                
                if model_name == 'AalenAdditive':
                    durations = y_fold_val_df['duration'].values
                    risk_scores = model.predict(X_fold_val, durations=durations)

                else:
                    risk_scores = model.predict(X_fold_val)
                
                c_index = calculate_c_index(y_fold_val_structured['duration'], risk_scores, y_fold_val[event_col].astype(bool))
                c_indices.append(c_index)
            

                if surv_funcs is None:
                    times_min = y_fold_val_df['duration'].min()
                    times_max = y_fold_val_df['duration'].max()
                    times_max = np.nextafter(times_max, -np.inf)  # Ensure times are less than max duration
                    times = np.linspace(times_min, times_max, 100)
                    
                    ibs = calculate_brier_score(y_fold_train_structured, y_fold_val_structured, risk_scores, times, ipcw = True)
                    ibs_scores.append(ibs)
                    predictions = risk_scores
                else:
                    if model_name == 'AalenAdditive':
                        coef_times = surv_funcs.index.values
                        times_min = max(y_fold_val_df['duration'].min(), coef_times[0])
                        times_max = min(y_fold_val_df['duration'].max(), coef_times[-1])
                        times_max = np.nextafter(times_max, -np.inf) 
                        times = np.linspace(times_min, times_max, 100, endpoint=False)
                        surv_probs = model.predict_survival_function(X_fold_val, times=times)
                    

                    else:
                        times_min = max(y_fold_val_df['duration'].min(), surv_funcs[0].x[0])
                        times_max = min(y_fold_val_df['duration'].max(), surv_funcs[0].x[-1])
                        times_max = np.nextafter(times_max, -np.inf) 
                        times = np.linspace(times_min, times_max, 100, endpoint=False)
                
                        surv_probs = np.row_stack([fn(times) for fn in surv_funcs])
                    ibs = calculate_brier_score(y_fold_train_structured, y_fold_val_structured, surv_probs, times)
                    ibs_scores.append(ibs)
                    predictions = surv_probs
                    
                calibration_error = calibration_assessment(predictions, y_fold_val_structured, times)
                calibration_errors.append(calibration_error)

            self.results[model_name] = {
                'c_index': np.nanmean(c_indices),
                'ibs': np.nanmean(ibs_scores),
                'ce': np.nanmean(calibration_errors),
            }
            print(f"{model_name} - C-Index: {self.results[model_name]['c_index']:.4f}, "
                  f"IBS: {self.results[model_name]['ibs']:.4f}, "
                  f"CE: {self.results[model_name]['ce']:.4f}")
        return self.results



