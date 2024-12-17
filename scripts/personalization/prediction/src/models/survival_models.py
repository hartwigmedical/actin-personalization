import numpy as np
import pandas as pd

from lifelines import AalenAdditiveFitter

from sksurv.linear_model import CoxPHSurvivalAnalysis
from sksurv.ensemble import RandomSurvivalForest, GradientBoostingSurvivalAnalysis

from sklearn.feature_selection import VarianceThreshold
from sklearn.model_selection import train_test_split

from scipy.interpolate import interp1d

import torch
import torch.nn as nn
import torchtuples as tt

from pycox.models import CoxPH    
from pycox.models import LogisticHazard
from pycox.models import DeepHitSingle
from pycox.models import PCHazard
from pycox.models import MTLR

torch.manual_seed(0)

class BaseSurvivalModel:
    def __init__(self, drop_variance_threshold=1e-5, correlation_threshold=0.95):
        self.drop_variance_threshold = drop_variance_threshold
        self.correlation_threshold = correlation_threshold

    def fit(self, X, y) -> None:
        """
        Fit the survival model.
        """
        raise NotImplementedError

    def predict_survival_function(self, X):
        """
        Predict survival functions.
        """
        raise NotImplementedError
        
    def predict(self, X) -> np.ndarray:
        """
        Predict survival probabilities or risk scores.
        """
        raise NotImplementedError
        
    @staticmethod
    def drop_low_variance_features(X, threshold=1e-5):
        """
        Drop features with variance below a given threshold.
        """
        selector = VarianceThreshold(threshold=threshold)
        X_reduced = selector.fit_transform(X)
        retained_features = X.columns[selector.get_support()]
        return pd.DataFrame(X_reduced, columns=retained_features, index=X.index)

    @staticmethod
    def drop_highly_correlated_features(X, threshold=0.95):
        """
        Drop highly correlated features above a given threshold.
        """
        corr_matrix = X.corr().abs()
        upper_triangle = corr_matrix.where(np.triu(np.ones(corr_matrix.shape), k=1).astype(bool))
        to_drop = [column for column in upper_triangle.columns if any(upper_triangle[column] > threshold)]
        return X.drop(columns=to_drop, errors="ignore")
        
class CoxPHModel(BaseSurvivalModel):
    def __init__(self, **kwargs):
        super().__init__()
        self.kwargs = kwargs
        self.model = CoxPHSurvivalAnalysis(**self.kwargs)
        self.selected_features = None

    def fit(self, X, y):       
        X = self.drop_low_variance_features(X)
        X = self.drop_highly_correlated_features(X)
        
        self.selected_features = X.columns

        self.model.fit(X, y)

    def predict_survival_function(self, X):
        return self.model.predict_survival_function(X[self.selected_features])

    def predict(self, X):       
        return self.model.predict(X[self.selected_features])
    
class AalenAdditiveModel(BaseSurvivalModel):
    def __init__(self, **kwargs):
        super().__init__()
        self.kwargs = kwargs
        self.model = AalenAdditiveFitter(**self.kwargs)

        self.selected_features = None

    def fit(self, X, y):
        X = self.drop_low_variance_features(X, threshold=self.drop_variance_threshold)
        X = self.drop_highly_correlated_features(X, threshold=self.correlation_threshold)
        
        y_df = pd.DataFrame({'duration': y['duration'], 'event': y['event']})
        X = X.reset_index(drop=True)
        y_df = y_df.reset_index(drop=True)

        df = pd.concat([X, y_df], axis=1)

        self.model.fit(df, duration_col='duration', event_col='event')
        self.selected_features = X.columns
        
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

    def predict(self, X, durations):
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

        interpolated_coefs = np.column_stack([interpolators[col](durations)for col in cumulative_coefs.columns])

        X_array = X_coefs.values
        risk_scores = np.einsum('ij,ij->i', X_array, interpolated_coefs)

        return risk_scores

class RandomSurvivalForestModel(BaseSurvivalModel):
    def __init__(self, **kwargs):
        super().__init__()
        self.kwargs = kwargs
        self.model = RandomSurvivalForest(**self.kwargs)

    def fit(self, X, y):
        self.model.fit(X, y)
        
    def predict_survival_function(self, X):
        return self.model.predict_survival_function(X)
        
    def predict(self, X):
        return self.model.predict(X)

class GradientBoostingSurvivalModel(BaseSurvivalModel):
    def __init__(self, **kwargs):
        super().__init__()
        self.kwargs = kwargs
        self.model = GradientBoostingSurvivalAnalysis(random_state=42, **self.kwargs)
        
    def fit(self, X, y):
        self.model.fit(X, y)

    def predict_survival_function(self, X):
        return self.model.predict_survival_function(X)

    def predict(self, X):
        return self.model.predict(X)

class NNSurvivalModel(BaseSurvivalModel):
    def __init__(self, model_class, input_size, num_nodes=[128, 64, 32], dropout=0.1, lr=1e-3, batch_size=64, epochs=100, num_durations=1, early_stopping_patience=5, batch_norm=False, weight_decay=1e-4, activation='relu', optimizer='Adam', **kwargs):
        """
        Generalized neural survival model.
        :param model_class: The specific survival model class (e.g., LogisticHazard, DeepHitSingle, etc.).
        :param input_size: Number of input features.
        :param num_nodes: List of nodes per hidden layer.
        :param dropout: Dropout rate for regularization.
        :param lr: Learning rate.
        :param batch_size: Batch size for training.
        :param epochs: Number of epochs for training.
        :param kwargs: Additional model-specific parameters.
        """
        super().__init__()
        self.model_class = model_class
        self.input_size = input_size
        self.num_nodes = num_nodes
        self.dropout = dropout
        self.lr = lr
        self.batch_size = batch_size
        self.epochs = epochs
        self.duration_index = None
        self.num_durations = num_durations
        self.early_stopping_patience = early_stopping_patience
        self.kwargs = {
            'model_class': model_class,
            'input_size': input_size,
            'num_nodes': num_nodes,
            'dropout': dropout,
            'lr': lr,
            'batch_size': batch_size,
            'epochs': epochs,
            'num_durations': num_durations,
            'early_stopping_patience': early_stopping_patience,
            'batch_norm': batch_norm,
            'weight_decay': weight_decay,
            'activation': activation,
            'optimizer': optimizer,
        }

        activation_map = {
            'relu': nn.ReLU,
            'elu': nn.ELU,
            'swish': lambda: nn.SiLU()
        }

        if activation in activation_map:
            activation_fn = activation_map[activation]
        else:
            raise ValueError(f"Unknown activation function: {activation}")
        
        # Define the neural network architecture
        self.net = tt.practical.MLPVanilla(in_features=self.input_size, num_nodes=self.num_nodes, out_features=self.num_durations, activation=activation_fn, batch_norm=batch_norm, dropout=self.dropout)
        
        if optimizer.lower() == "adam":
            self.optimizer = tt.optim.Adam(self.lr, weight_decay=weight_decay)
        elif optimizer.lower() == "rmsprop":
            self.optimizer = torch.optim.RMSprop(self.net.parameters(), self.lr, weight_decay=weight_decay)
        else:
            raise ValueError(f"Unknown optimizer: {optimizer}")

        model_specific_kwargs = {k:v for k,v in self.kwargs.items() if k not in {
            'model_class', 'input_size', 'num_nodes', 'dropout', 'lr',
            'batch_size', 'epochs', 'num_durations', 'early_stopping_patience',
            'batch_norm', 'weight_decay', 'activation', 'optimizer'
        }}

        self.model = model_class(self.net, self.optimizer, **model_specific_kwargs)

    def fit(self, X, y, val_data=None):
        durations = y['duration'].astype('float32')
        events = y['event'].astype('float32')
        X_tensor = X.values.astype('float32')

        # Apply label transformation if required
        if hasattr(self.model_class, 'label_transform'):
            self.labtrans = self.model_class.label_transform(self.num_durations)
            y = self.labtrans.fit_transform(durations, events)
            self.model.duration_index = self.labtrans.cuts
        else:
            y = (durations, events)
  
        if val_data is not None:
            X_val, y_val = val_data
            if hasattr(self.model_class, 'label_transform'):
                y_val = self.labtrans.transform(
                    y_val['duration'].astype('float32'), y_val['event'].astype('float32')
                )
            else:
                durations_val = y_val['duration'].astype('float32')
                events_val = y_val['event'].astype('float32')
                y_val = (durations_val, events_val)
            val_data = (X_val.astype('float32'), y_val)

        callbacks = [tt.callbacks.EarlyStopping(patience=self.early_stopping_patience, min_delta=1e-4)]
        self.model.fit(X_tensor, y, self.batch_size, self.epochs, callbacks, verbose=False, val_data=val_data)

        if hasattr(self.model, 'compute_baseline_hazards'):
            self.model.compute_baseline_hazards()


    def predict_survival_function(self, X, times=None):
        X_tensor = X.values.astype('float32')
        surv = self.model.predict_surv_df(X_tensor)

        if times is not None:
            surv = surv.reindex(times, method='nearest', fill_value='extrapolate')

        return [interp1d(surv.index.values, surv.iloc[:, i].values, bounds_error=False, fill_value='extrapolate') for i in range(surv.shape[1])]

    def predict(self, X):
        X_tensor = X.values.astype('float32')
        if hasattr(self.model, 'predict_risk'):
            risk_scores = self.model.predict_risk(X_tensor)
            
        elif hasattr(self.model, 'predict_cumulative_hazard'):
            cum_haz = self.model.predict_cumulative_hazard(X_tensor)
            time_point = cum_haz.index[-1]
            cum_haz_at_time = cum_haz.loc[time_point].values
            risk_scores = cum_haz_at_time
            
        elif hasattr(self.model, 'predict_surv_df'):
            surv = self.model.predict_surv_df(X_tensor)
            
            time_point = surv.index[-1]
            surv_at_time = surv.loc[time_point].values
            surv_at_time = np.clip(surv_at_time, 1e-10, 1.0)
            
            risk_scores = -np.log(surv_at_time)
            
        else:
            risk_scores = self.model.predict(X_tensor).flatten()
            
        return risk_scores
    
class DeepSurv(NNSurvivalModel):
    def __init__(self, **kwargs):
        kwargs.pop('model_class', None)
        super().__init__(model_class = CoxPH, **kwargs)

class LogisticHazardModel(NNSurvivalModel):
    def __init__(self, **kwargs):
        kwargs.pop('model_class', None)
        kwargs.pop('num_durations', None) 
        super().__init__(model_class = LogisticHazard, num_durations=60, **kwargs)

class DeepHitModel(NNSurvivalModel):
    def __init__(self, **kwargs):
        kwargs.pop('num_durations', None) 
        kwargs.pop('model_class', None)
        super().__init__(model_class = DeepHitSingle, num_durations=60, **kwargs)

class PCHazardModel(NNSurvivalModel):
    def __init__(self, **kwargs):
        kwargs.pop('num_durations', None) 
        kwargs.pop('model_class', None)
        super().__init__(model_class = PCHazard, num_durations=60, **kwargs)

class MTLRModel(NNSurvivalModel):
    def __init__(self, **kwargs):
        kwargs.pop('num_durations', None) 
        kwargs.pop('model_class', None)
        super().__init__(model_class = MTLR, num_durations=60, **kwargs)
