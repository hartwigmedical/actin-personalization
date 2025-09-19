# utils/counterfactuals.py

from __future__ import annotations
import numpy as np
import pandas as pd
from typing import Dict, List, Optional

_TREATMENT_COMPONENT_COLUMNS = [
    "systemicTreatmentPlan_5-FU",
    "systemicTreatmentPlan_oxaliplatin",
    "systemicTreatmentPlan_irinotecan",
    "systemicTreatmentPlan_bevacizumab",
    "systemicTreatmentPlan_panitumumab",
    "systemicTreatmentPlan_pembrolizumab",
    "systemicTreatmentPlan_nivolumab",
]

_TREATMENT_LABEL_TO_COMPONENTS: Dict[str, tuple[int, ...]] = {
    "No Treatment": (0, 0, 0, 0, 0, 0, 0),
    "5-FU": (1, 0, 0, 0, 0, 0, 0),
    "5-FU + bevacizumab": (1, 0, 0, 1, 0, 0, 0),
    "5-FU + oxaliplatin": (1, 1, 0, 0, 0, 0, 0),
    "5-FU + oxaliplatin + bevacizumab": (1, 1, 0, 1, 0, 0, 0),
    "5-FU + oxaliplatin + panitumumab": (1, 1, 0, 0, 1, 0, 0),
    "5-FU + irinotecan": (1, 0, 1, 0, 0, 0, 0),
    "5-FU + irinotecan + bevacizumab": (1, 0, 1, 1, 0, 0, 0),
    "5-FU + oxaliplatin + irinotecan": (1, 1, 1, 0, 0, 0, 0),
    "5-FU + oxaliplatin + irinotecan + bevacizumab": (1, 1, 1, 1, 0, 0, 0),
    "PEMBROLIZUMAB": (0, 0, 0, 0, 0, 1, 0),
}

def ensure_treatment_columns_exist(X: pd.DataFrame) -> pd.DataFrame:
    missing_columns = {col: 0 for col in _TREATMENT_COMPONENT_COLUMNS if col not in X.columns}
    if missing_columns:
        X = X.copy()
        for column, default_value in missing_columns.items():
            X[column] = default_value
    return X

def apply_treatment_components(X: pd.DataFrame, treatment_label: str) -> pd.DataFrame:
    if treatment_label not in _TREATMENT_LABEL_TO_COMPONENTS:
        raise ValueError(f"Unknown treatment label: {treatment_label}")
    X = ensure_treatment_columns_exist(X)
    treatment_values = _TREATMENT_LABEL_TO_COMPONENTS[treatment_label]
    updated_X = X.copy()
    for column, value in zip(_TREATMENT_COMPONENT_COLUMNS, treatment_values):
        updated_X[column] = value
    if "hasTreatment" in updated_X.columns:
        updated_X["hasTreatment"] = int(any(treatment_values))
    return updated_X

def interpolate_survival_probabilities_at_days(survival_df: pd.DataFrame, days: float) -> np.ndarray:
    time_points = survival_df.index.to_numpy(dtype=float)
    survival_values = survival_df.to_numpy(dtype=float)
    interpolated_probabilities = np.empty(survival_values.shape[1], dtype=float)
    for column_index in range(survival_values.shape[1]):
        interpolated_probabilities[column_index] = np.interp(days, time_points, survival_values[:, column_index])
    return interpolated_probabilities

def predict_survival_probabilities(
    model,
    X: pd.DataFrame,
    evaluation_days: int,
    task_indices: Optional[np.ndarray] = None
) -> pd.Series:
    try:
        if task_indices is None:
            survival_function = model.predict_survival_function(X)
        else:
            survival_function = model.predict_survival_function(X, task_indices)
    except TypeError:
        if task_indices is None and hasattr(model, "num_tasks"):
            task_indices = np.zeros(len(X), dtype=int)
            survival_function = model.predict_survival_function(X, task_indices)
        else:
            raise

    if isinstance(survival_function, pd.DataFrame):
        probabilities = interpolate_survival_probabilities_at_days(survival_function, evaluation_days)
        return pd.Series(probabilities, index=X.index)

    if isinstance(survival_function, (list, tuple)):
        probabilities = np.array([float(func(evaluation_days)) for func in survival_function], dtype=float)
        return pd.Series(probabilities, index=X.index)
    
    try:
        arr = np.asarray(survival_function)
        if arr.dtype == object and arr.size > 0:
            first = arr[0]
            if callable(first) or (hasattr(first, "x") and hasattr(first, "y")):
                probs = np.array([float(fn(float(evaluation_days))) for fn in list(arr)], dtype=float)
                return pd.Series(probs, index=X.index)
    except Exception:
        survival_array = np.asarray(survival_function)
        if survival_array.ndim == 2:
            time_grid = None
            label_transform = getattr(model, "labtrans", None)
            if label_transform is not None and hasattr(label_transform, "cuts"):
                time_grid = np.asarray(label_transform.cuts, dtype=float)
            elif hasattr(model, "times_"):
                time_grid = np.asarray(getattr(model, "times_"), dtype=float)
            if time_grid is None:
                raise ValueError("Survival function returned 2D array but no time grid found on model.")
            survival_df = pd.DataFrame(survival_array, index=time_grid)
            probabilities = interpolate_survival_probabilities_at_days(survival_df, evaluation_days)
            return pd.Series(probabilities, index=X.index)


