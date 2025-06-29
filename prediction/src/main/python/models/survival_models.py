import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import torchtuples as tt

from sklearn.feature_selection import VarianceThreshold
from sklearn.model_selection import train_test_split

from sksurv.ensemble import RandomSurvivalForest, GradientBoostingSurvivalAnalysis
from sksurv.linear_model import CoxPHSurvivalAnalysis

from typing import Dict, Any, Optional, List
from pycox.models import CoxPH, LogisticHazard, DeepHitSingle, PCHazard, MTLR
from scipy.interpolate import interp1d

from utils.settings import config_settings

torch.manual_seed(0)

class BaseSurvivalModel:
    def __init__(self, drop_variance_threshold: float = 1e-5, correlation_threshold: float = 0.95):
        self.drop_variance_threshold = drop_variance_threshold
        self.correlation_threshold = correlation_threshold

    def fit(self, X: pd.DataFrame, y: pd.DataFrame) -> None:
        
        raise NotImplementedError

    def predict_survival_function(self, X: pd.DataFrame) -> np.ndarray:
    
        raise NotImplementedError
        
    def predict(self, X: pd.DataFrame) -> np.ndarray:
       
        raise NotImplementedError
        
    @staticmethod
    def drop_low_variance_features(X: pd.DataFrame, threshold: float = 1e-5) -> pd.DataFrame:
        selector = VarianceThreshold(threshold=threshold)
        X_reduced = selector.fit_transform(X)
        retained_features = X.columns[selector.get_support()]
        return pd.DataFrame(X_reduced, columns=retained_features, index=X.index)

    @staticmethod
    def drop_highly_correlated_features(X: pd.DataFrame, threshold: float = 0.95) -> pd.DataFrame:
        corr_matrix = X.corr().abs()
        upper_triangle = corr_matrix.where(np.triu(np.ones(corr_matrix.shape), k=1).astype(bool))
        to_drop = [column for column in upper_triangle.columns if any(upper_triangle[column] > threshold)]
        return X.drop(columns=to_drop, errors="ignore")
        
class CoxPHModel(BaseSurvivalModel):
    def __init__(self, **kwargs: Dict[str, Any]):
        super().__init__()
        self.kwargs = kwargs
        self.model = CoxPHSurvivalAnalysis(**self.kwargs)
        self.selected_features = None

    def fit(self, X: pd.DataFrame, y: np.ndarray) -> None:       
        X = self.drop_low_variance_features(X)
        X = self.drop_highly_correlated_features(X)
        
        self.selected_features = X.columns

        self.model.fit(X, y)

    def predict_survival_function(self, X: pd.DataFrame) -> np.ndarray:
        return self.model.predict_survival_function(X[self.selected_features])

    def predict(self, X: pd.DataFrame) -> np.ndarray:       
        return self.model.predict(X[self.selected_features])

class RandomSurvivalForestModel(BaseSurvivalModel):
    def __init__(self, **kwargs: Dict[str, Any]):
        super().__init__()
        self.kwargs = kwargs
        self.model = RandomSurvivalForest(n_jobs = config_settings.n_jobs, **self.kwargs)

    def fit(self, X: pd.DataFrame, y: pd.DataFrame) -> None:
        self.model.fit(X, y)
        
    def predict_survival_function(self, X: pd.DataFrame) -> np.ndarray:
        return self.model.predict_survival_function(X)
        
    def predict(self, X: pd.DataFrame) -> np.ndarray:
        return self.model.predict(X)

class GradientBoostingSurvivalModel(BaseSurvivalModel):
    def __init__(self, **kwargs: Dict[str, Any]):
        super().__init__()
        self.kwargs = kwargs
        self.model = GradientBoostingSurvivalAnalysis(**self.kwargs)
        
    def fit(self, X: pd.DataFrame, y: pd.DataFrame) -> None:
        self.model.fit(X, y)

    def predict_survival_function(self, X: pd.DataFrame) -> np.ndarray:
        return self.model.predict_survival_function(X)

    def predict(self, X: pd.DataFrame) -> np.ndarray:
        return self.model.predict(X)

    
class FeatureAttention(nn.Module):
    def __init__(self, input_size: int, msi_index: int=None, immuno_index: List[int]=None, ras_index: int = None, panitumumab_index: int = None, treatment_indices: List[int]=None):
        super().__init__()
        self.msi_index  = msi_index
        self.immuno_index  = immuno_index or []
        self.ras_index = ras_index
        self.panitumumab_index = panitumumab_index
        self.treatment_indices: List[int] = treatment_indices or []
        
        self.attn = nn.Sequential(
            nn.Linear(input_size, input_size),
            nn.Tanh(),
            nn.Linear(input_size, input_size),
            nn.Sigmoid()
        )

    def _apply_gate(self, x):
        if config_settings.use_gate:
            if self.msi_index is not None and self.immuno_index is not None:
                msi_gate = x[:, self.msi_index].unsqueeze(1)
                x[:, self.immuno_index] *= msi_gate
            if self.ras_index is not None and self.panitumumab_index is not None:
                ras_gate = 1 - x[:, self.ras_index]
                x[:, self.panitumumab_index] *= ras_gate
            if self.treatment_indices:
                mask = x[:, self.treatment_indices]
                gate = (mask > 0).float()
                x[:, self.treatment_indices] *= gate
        return x

    def forward(self, x):
        x = self._apply_gate(x)
        weights = self.attn(x)
        return x * weights
    
class AttentionMLP(nn.Module):
    def __init__(self, input_size: int, num_nodes: List[int], out_features: int, activation, batch_norm: bool, dropout: float, msi_index: int = None, immuno_index: List[int] = None, ras_index: int = None, panitumumab_index: int = None, treatment_indices: List[int]=None):
        super().__init__()
        self.attention = FeatureAttention(input_size, msi_index, immuno_index, ras_index, panitumumab_index, treatment_indices)
        self.mlp = tt.practical.MLPVanilla(
            in_features=input_size, num_nodes=num_nodes, out_features=out_features,
            activation=activation, batch_norm=batch_norm, dropout=dropout)
    
    def forward(self, x):
        x = self.attention(x)
        return self.mlp(x)

class NNSurvivalModel(BaseSurvivalModel):
    def __init__(
        self, 
        model_class, 
        input_size: int, 
        num_nodes: List[int] = [128, 64, 32], 
        dropout: float = 0.1, 
        lr: float = 1e-3, 
        batch_size: int = 64, 
        epochs: int = 100, 
        num_durations: int = 1, 
        early_stopping_patience: int = 5, 
        batch_norm: bool = False, 
        weight_decay: float = 1e-4, 
        activation: str = 'relu', 
        optimizer: str = 'Adam',
        use_attention: bool = False,
        gate_msi_index = None, 
        gate_immuno_index = None,
        gate_ras_index = None, 
        gate_panitumumab_index = None,
        treatment_indices: List[int]=None,
        **kwargs: Dict[str, Any]
    ):
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
            'use_attention': use_attention,
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
        
        if use_attention:
            self.net = AttentionMLP(
                input_size=self.input_size, 
                num_nodes=self.num_nodes, 
                out_features=self.num_durations,
                activation=activation_fn, 
                batch_norm=batch_norm, 
                dropout=self.dropout, 
                msi_index = gate_msi_index, 
                immuno_index = gate_immuno_index, 
                ras_index = gate_ras_index, 
                panitumumab_index = gate_panitumumab_index, 
                treatment_indices = treatment_indices
            )
        else:
            self.net = tt.practical.MLPVanilla(
                in_features=self.input_size,
                num_nodes=self.num_nodes,
                out_features=self.num_durations,
                activation=activation_fn,
                batch_norm=batch_norm,
                dropout=self.dropout
            )
        
        if optimizer.lower() == "adam":
            self.optimizer = tt.optim.Adam(self.lr, weight_decay=weight_decay)
        elif optimizer.lower() == "rmsprop":
            self.optimizer = torch.optim.RMSprop(self.net.parameters(), self.lr, weight_decay=weight_decay)
        else:
            raise ValueError(f"Unknown optimizer: {optimizer}")

        model_specific_kwargs = {k:v for k,v in self.kwargs.items() if k not in {
            'model_class', 'input_size', 'num_nodes', 'dropout', 'lr',
            'batch_size', 'epochs', 'num_durations', 'early_stopping_patience',
            'batch_norm', 'weight_decay', 'activation', 'optimizer', 'use_attention', 'gate_msi_index', 'gate_immuno_index', 'treatment_indices'
        }}

        self.model = model_class(self.net, self.optimizer, **model_specific_kwargs)

    def fit(self, X: pd.DataFrame, y: pd.DataFrame, val_data: pd.DataFrame = None) -> None:
        durations = y['duration'].astype('float32')
        events = y['event'].astype('float32')
        X_tensor = X.values.astype('float32')

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


    def predict_survival_function(self, X: pd.DataFrame, times: np.ndarray = None) -> np.ndarray:
        X_tensor = X.values.astype('float32')
        surv = self.model.predict_surv_df(X_tensor)

        if times is not None:
            surv = surv.reindex(times, method='nearest', fill_value='extrapolate')

        return [interp1d(surv.index.values, surv.iloc[:, i].values, bounds_error=False, fill_value='extrapolate') for i in range(surv.shape[1])]

    def predict(self, X: pd.DataFrame) -> np.ndarray:        
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
    def __init__(self, **kwargs: Dict[str, Any]):
        kwargs.pop('model_class', None)
        super().__init__(model_class = CoxPH, **kwargs)

class LogisticHazardModel(NNSurvivalModel):
    def __init__(self, **kwargs: Dict[str, Any]):
        kwargs.pop('model_class', None)
        kwargs.pop('num_durations', None) 
        super().__init__(model_class = LogisticHazard, num_durations=60, **kwargs)

class DeepHitModel(NNSurvivalModel):
    def __init__(self, **kwargs: Dict[str, Any]):
        kwargs.pop('num_durations', None) 
        kwargs.pop('model_class', None)
        super().__init__(model_class = DeepHitSingle, num_durations=60, **kwargs)

class PCHazardModel(NNSurvivalModel):
    def __init__(self, **kwargs: Dict[str, Any]):
        kwargs.pop('num_durations', None) 
        kwargs.pop('model_class', None)
        super().__init__(model_class = PCHazard, num_durations=60, **kwargs)

class MTLRModel(NNSurvivalModel):
    def __init__(self, **kwargs: Dict[str, Any]):
        kwargs.pop('num_durations', None) 
        kwargs.pop('model_class', None)
        super().__init__(model_class = MTLR, num_durations=60, **kwargs)