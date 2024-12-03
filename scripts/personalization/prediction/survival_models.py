import numpy as np
import pandas as pd

from lifelines import AalenAdditiveFitter
from sksurv.linear_model import CoxPHSurvivalAnalysis
from sksurv.ensemble import RandomSurvivalForest, GradientBoostingSurvivalAnalysis

from sklearn.feature_selection import VarianceThreshold
from sklearn.model_selection import train_test_split

from scipy.interpolate import interp1d

import torch
import torchtuples as tt

from pycox.models import CoxPH
from pycox.evaluation import EvalSurv

torch.manual_seed(0)

class BaseSurvivalModel:
    def __init__(self):
        pass

    def fit(self, X, y):
        """
        Fit the survival model.
        """
        raise NotImplementedError

    def predict(self, X):
        """
        Predict survival probabilities or risk scores.
        """
        raise NotImplementedError


class CoxPHModel(BaseSurvivalModel):
    def __init__(self):
        super().__init__()
        self.model = CoxPHSurvivalAnalysis()
        self.selected_features = None
    
    def drop_low_variance_features(self, X, threshold=1e-5):
        """
        Drop features with variance below a given threshold.
        """
        selector = VarianceThreshold(threshold=threshold)
        
        X_reduced = selector.fit_transform(X)
        retained_features = X.columns[selector.get_support()]
        
        return pd.DataFrame(X_reduced, columns=retained_features, index=X.index)
    
    def drop_highly_correlated_features(self, X, threshold=0.95):
        corr_matrix = X.corr().abs()
        upper_triangle = corr_matrix.where(np.triu(np.ones(corr_matrix.shape), k=1).astype(bool))
        to_drop = [column for column in upper_triangle.columns if any(upper_triangle[column] > threshold)]

        return X.drop(columns=to_drop, errors="ignore")

    def fit(self, X, y):       
        X = self.drop_low_variance_features(X)
        X = self.drop_highly_correlated_features(X)
        
        self.selected_features = X.columns

        self.model.fit(X, y)


    def predict(self, X):
        """
        Predict risk scores
        """
        
        return self.model.predict(X[self.selected_features])
    
    def predict_survival_function(self, X):
        """
        Predict survival functions.
        """
        return self.model.predict_survival_function(X[self.selected_features])


class RandomSurvivalForestModel(BaseSurvivalModel):
    def __init__(self, n_estimators=100, min_samples_split=10, min_samples_leaf=15):
        super().__init__()
        self.model = RandomSurvivalForest(
            n_estimators=n_estimators,
            min_samples_split=min_samples_split,
            min_samples_leaf=min_samples_leaf,
            random_state=42,
        )

    def fit(self, X, y):
        """
        Fit the Random Survival Forest model.
        X: DataFrame of covariates.
        y: Structured array with 'event' and 'duration' fields.
        """
        self.model.fit(X, y)
        
    def predict(self, X):
        """
        Predict risk scores
        """
        return self.model.predict(X)

    def predict_survival_function(self, X):
        """
        Predict survival functions for each sample.
        """
        return self.model.predict_survival_function(X)

class GradientBoostingSurvivalModel(BaseSurvivalModel):
    def __init__(self):
        """
        Initialize the Gradient Boosting Survival Analysis model.
        :param kwargs: Parameters to pass to GradientBoostingSurvivalAnalysis.
        """
        super().__init__()
        self.model = GradientBoostingSurvivalAnalysis(loss="coxph", learning_rate=0.1, n_estimators=100, subsample=0.8, max_depth=3, random_state=42)
        
    def fit(self, X, y):
        """
        Fit the Gradient Boosting Survival Analysis model.
        X: DataFrame of covariates.
        y: Structured array with 'event' and 'duration' fields.
        """
        self.model.fit(X, y)

    def predict_survival_function(self, X):
        """
        Predict survival functions for each sample.
        X: DataFrame of covariates.
        Returns: List of survival functions for each sample.
        """
        return self.model.predict_survival_function(X)

    def predict(self, X):
        """
        Predict risk scores.
        X: DataFrame of covariates.
        Returns: Array of risk scores for each sample.
        """
        return self.model.predict(X)

class AalenAdditiveModel(BaseSurvivalModel):
    def __init__(self, coef_penalizer=1.0, drop_variance_threshold=1e-5, correlation_threshold=0.95):
        super().__init__()
        self.model = AalenAdditiveFitter(coef_penalizer=coef_penalizer)
        self.selected_features = None
        self.drop_variance_threshold = drop_variance_threshold
        self.correlation_threshold = correlation_threshold

    def drop_low_variance_features(self, X, threshold):
        selector = VarianceThreshold(threshold=threshold)
        X_reduced = selector.fit_transform(X)
        retained_features = X.columns[selector.get_support()]
        return pd.DataFrame(X_reduced, columns=retained_features, index=X.index)

    def drop_highly_correlated_features(self, X, threshold):
        corr_matrix = X.corr().abs()
        upper_triangle = corr_matrix.where(np.triu(np.ones(corr_matrix.shape), k=1).astype(bool))
        to_drop = [column for column in upper_triangle.columns if any(upper_triangle[column] > threshold)]
        return X.drop(columns=to_drop, errors="ignore")

    def fit(self, X, y):
        # Drop low-variance and highly correlated features
        X = self.drop_low_variance_features(X, threshold=self.drop_variance_threshold)
        X = self.drop_highly_correlated_features(X, threshold=self.correlation_threshold)
        
        y_df = pd.DataFrame({'duration': y['duration'], 'event': y['event']})
        df = pd.concat([X, y_df], axis=1)

        self.model.fit(df, duration_col='duration', event_col='event')
        self.selected_features = X.columns

    def predict(self, X, durations):
        # Ensure that X has the same columns as during training
        X = X[self.selected_features].copy()

        cumulative_coefs = self.model.cumulative_hazards_
        coef_times = cumulative_coefs.index.values

        if cumulative_coefs.empty:
            return np.zeros(len(X))


        interpolators = {
            col: interp1d(coef_times, cumulative_coefs[col], bounds_error=False, fill_value="extrapolate")
            for col in cumulative_coefs.columns
        }

        X_coefs = X.reindex(columns=cumulative_coefs.columns, fill_value=0)

        min_time, max_time = coef_times[0], coef_times[-1]
        durations = np.clip(durations, min_time, max_time)
        durations = np.asarray(durations).flatten()

        interpolated_coefs = np.column_stack([
            interpolators[col](durations)
            for col in cumulative_coefs.columns
        ])

        X_array = X_coefs.values
        risk_scores = np.einsum('ij,ij->i', X_array, interpolated_coefs)

        return risk_scores

    def predict_survival_function(self, X, times=None):
        X = X[self.selected_features].copy()
        survival_functions = self.model.predict_survival_function(X)
        if times is not None:
            surv_funcs = []
            for i in range(survival_functions.shape[1]):
                sf = survival_functions.iloc[:, i]
                interpolator = interp1d(sf.index.values, sf.values, bounds_error=False, fill_value="extrapolate")
                surv_funcs.append(interpolator(times))
            return np.array(surv_funcs)
        else:
            return survival_functions


class DeepSurv(BaseSurvivalModel):
    def __init__(self, input_size, num_nodes=[128, 64, 32], dropout=0.1, lr=1e-3, batch_size=64, epochs=100):
        super().__init__()
        self.num_nodes = num_nodes
        self.dropout = dropout
        self.lr = lr
        self.batch_size = batch_size
        self.epochs = epochs
        self.duration_index = None
        
        # Define the neural network architecture
        self.net = tt.practical.MLPVanilla(input_size, num_nodes, 1, dropout, output_bias=False)
        self.optimizer = tt.optim.Adam(self.lr, weight_decay=1e-4)
        self.model = CoxPH(self.net, self.optimizer)
    
    def fit(self, X, y):
        durations = y['duration'].astype('float32')
        events = y['event'].astype('float32')  
   
        X_tensor = X.values.astype('float32')

        callbacks = [tt.callbacks.EarlyStopping(patience=10)]
        self.model.fit(X_tensor, (durations, events), self.batch_size, self.epochs, callbacks, verbose=True)

    
    def predict_survival_function(self, X, times=None):
        X_tensor = X.astype('float32')
        surv = self.model.predict_surv_df(X_tensor)
        
        if times is not None:
            surv = surv.reindex(times, method='nearest', fill_value='extrapolate')
        
        return surv
    
    def predict(self, X):
        X_tensor = X.values.astype('float32')
        risk_scores = -self.model.predict(X_tensor).flatten()
        return risk_scores