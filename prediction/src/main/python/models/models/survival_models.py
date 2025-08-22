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
