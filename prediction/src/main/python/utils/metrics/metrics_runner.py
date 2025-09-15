# utils/metric_helpers/metrics_runner.py
from __future__ import annotations
import numpy as np
import pandas as pd
import re  
from typing import Any, Dict, List, Tuple
from sksurv.util import Surv

from .base_metrics import compute_time_dependent_c_index, compute_integrated_brier_score, compute_time_dependent_auc
from .counterfactuals import apply_treatment_components, predict_survival_probabilities
from utils.metrics.base_metrics import compute_cfb_for_treatment_pair

def counterfactual_S_tau_by_treatment(
    model,
    X: pd.DataFrame,
    feature_columns: List[str],
    treatment_groups: List[str],
    tau_days: int,
) -> Dict[str, pd.Series]:
    """
    Returns a dict: treatment label -> Series of S_i(tau) for all i in X (counterfactual).
    """
    X_base = X[[c for c in feature_columns if c != "treatment_group_idx"]].copy()
    n = len(X_base)

    is_multitask = hasattr(model, "predict_survival_function") and hasattr(model, "num_tasks")

    out: Dict[str, pd.Series] = {}
    for t in treatment_groups:
        X_t = apply_treatment_components(X_base.copy(), t)
        if is_multitask:
            t_idx = treatment_groups.index(t)
            task_idx = np.full(n, t_idx, dtype=int)
            out[t] = predict_survival_probabilities(model, X_t, tau_days, task_indices=task_idx)
        else:
            out[t] = predict_survival_probabilities(model, X_t, tau_days, task_indices=None)
    return out


def generate_evaluation_time_grid(
    y: np.ndarray, max_time: int, fixed_time_bins: List[int]
) -> np.ndarray:
    grid = np.asarray(fixed_time_bins, dtype=int)
    lower_bound = int(y['duration'].min())
    upper_bound = int(min(max_time, y['duration'].max()))
    valid_times = grid[(grid > lower_bound) & (grid < upper_bound)]
    return valid_times if valid_times.size > 0 else np.array([min(upper_bound, grid[-1])])

def evaluate_model_predictions(
    model,
    X: pd.DataFrame,
    evaluation_times: np.ndarray
) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:
    """
    Returns:
      evaluation_times: np.ndarray of times
      predictions:      np.ndarray shape [n_samples, len(evaluation_times)] with survival S(t)
      risk_scores:      same shape, here we use -S(t) for time-dependent AUC
    """

    if 'treatment_group_idx' in X.columns:
        input_features = X.drop(columns=['treatment_group_idx'], errors="ignore")
        is_multitask = hasattr(model, 'predict_survival_function') and hasattr(model, 'num_tasks')
        task_indices = X['treatment_group_idx'].values.astype(int) if is_multitask else None
    else:
        input_features = X
        is_multitask = hasattr(model, 'predict_survival_function') and hasattr(model, 'num_tasks')
        task_indices = None

    if is_multitask:
        sf = model.predict_survival_function(input_features, task_indices)
    else:
        sf = model.predict_survival_function(input_features)

    if isinstance(sf, pd.DataFrame):
        df = sf.reindex(evaluation_times, method='nearest', fill_value='extrapolate')
        predictions = df.values.T  # -> [n_samples, len(times)]
    elif isinstance(sf, (list, tuple)):
        predictions = np.row_stack([fn(evaluation_times) for fn in sf])  # [n_samples, len(times)]
    else:
        arr = np.asarray(sf)
        if arr.ndim == 2:
            tgrid = None
            labtrans = getattr(model, "labtrans", None)
            if labtrans is not None and hasattr(labtrans, "cuts"):
                tgrid = np.asarray(labtrans.cuts, dtype=float)
            elif hasattr(model, "times_"):
                tgrid = np.asarray(getattr(model, "times_"), dtype=float)
            if tgrid is None:
                raise ValueError("Survival function returned 2D array but no time grid found on model.")
            df = pd.DataFrame(arr, index=tgrid).reindex(evaluation_times, method='nearest', fill_value='extrapolate')
            predictions = df.values.T
        else:
            raise ValueError("Unsupported output from predict_survival_function.")

    risk_scores = -predictions
    return evaluation_times, predictions, risk_scores

def evaluate_all_metrics(
    *,
    model,
    model_name: str,
    X: pd.DataFrame,
    y_train: np.ndarray,
    y_test: np.ndarray,
    settings,
    feature_names: List[str]
) -> Dict[str, Any]: 
    results: Dict[str, Any] = {}

    y_test_df = pd.DataFrame({
        "duration": y_test["duration"],
        "event": y_test["event"],
    }, index=X.index)

    evaluation_times = generate_evaluation_time_grid(
        y_test, settings.max_time, settings.fixed_time_bins
    )
    evaluation_times, predictions, risk_scores = evaluate_model_predictions(
        model, X, evaluation_times
    )

    results['c_index'] = compute_time_dependent_c_index(
        predictions, y_test['duration'], y_test['event'], evaluation_times
    )

    results['calibration_error'] = compute_integrated_brier_score(
        y_train, y_test, predictions, evaluation_times
    )

    auc_times, mean_auc = compute_time_dependent_auc(
        y_train, y_test, risk_scores, evaluation_times
    )
    results['auc'] = mean_auc

    tau = settings.evaluation_days
    y_test_df = pd.DataFrame({"duration": y_test["duration"], "event": y_test["event"]}, index=X.index)
    alive_tau = (y_test_df["duration"] > tau) & (~(y_test_df["event"].astype(bool) & (y_test_df["duration"] <= tau)))

    # Counterfactual S_tau per treatment for everyone
    S_tau = counterfactual_S_tau_by_treatment(
        model=model,
        X=X,
        feature_columns=feature_names,
        treatment_groups=settings.treatment_groups,
        tau_days=tau,
    )

    # Build matching features (baseline only) once
    match_cols = [c for c in feature_names if c not in {
        "systemicTreatmentPlan_5-FU","systemicTreatmentPlan_oxaliplatin","systemicTreatmentPlan_irinotecan",
        "systemicTreatmentPlan_bevacizumab","systemicTreatmentPlan_panitumumab",
        "systemicTreatmentPlan_pembrolizumab","systemicTreatmentPlan_nivolumab",
        "hasTreatment","treatment_group_idx",
    }]
    X_match = X[match_cols + ["treatment_group_idx"]].copy()

    # For CFB mean aggregation
    cfb_values = []

    for treat in settings.treatment_groups:
        if treat == "No Treatment":
            continue
        key = re.sub(r'[^a-z0-9]+', '_', treat.lower()).strip('_')

        # Delegate CFB computation to cfb.py
        cfb_result = compute_cfb_for_treatment_pair(
            X_match=X_match,
            S_tau=S_tau,
            alive_tau=alive_tau,
            treatment_groups=settings.treatment_groups,
            treat_A="No Treatment",
            treat_B=treat,
            match_cols=match_cols,
            y_test_df=y_test_df,
            tau=tau,
            settings=settings
        )

        results.update(cfb_result)
        if np.isfinite(cfb_result.get(f"cfb_{key}", float('nan'))):
            cfb_values.append(cfb_result[f"cfb_{key}"])

    results["cfb_mean"] = float(np.mean(cfb_values)) if len(cfb_values) else float('nan')

    return results
