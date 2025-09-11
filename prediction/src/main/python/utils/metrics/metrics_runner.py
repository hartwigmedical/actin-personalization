# utils/metric_helpers/metrics_runner.py
from __future__ import annotations
import numpy as np
import pandas as pd
import re  
from typing import Any, Dict, List, Tuple
from sksurv.util import Surv

from .base_metrics import compute_time_dependent_c_index, compute_integrated_brier_score, compute_time_dependent_auc, compute_c_for_benefit, build_and_calibrate_benefit
from .counterfactuals import apply_treatment_components, predict_survival_probabilities

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

    cfb_results = compute_c_for_benefit(
        model=model,
        X=X,
        y=y_test_df,
        treatment_groups=settings.treatment_groups,
        feature_columns=feature_names,
        evaluation_days=settings.evaluation_days
    )
    results['cfb_mean'] = cfb_results['c_for_benefit'].mean() if not cfb_results.empty else float('nan')
    for _, row in cfb_results.iterrows():
        t_name = str(row['treatment'])
        key = re.sub(r'[^a-z0-9]+', '_', t_name.lower()).strip('_')
        results[f"cfb_{key}"] = float(row['c_for_benefit']) if pd.notnull(row['c_for_benefit']) else float('nan')

    for treat in settings.treatment_groups:
        if treat == "No Treatment":
            continue
        key = re.sub(r'[^a-z0-9]+', '_', treat.lower()).strip('_')
        try:
            prob_no = pd.Series(predictions[:, 0], index=X.index)
            prob_t = pd.Series(predictions[:, -1], index=X.index)

            calibration_benefit = build_and_calibrate_benefit(
                validation_data=X,
                treatment_groups=settings.treatment_groups,
                treatment_A="No Treatment",
                treatment_B=treat,
                feature_columns=feature_names,
                predicted_prob_A=prob_no,
                predicted_prob_B=prob_t,
                survival_durations=y_test_df['duration'],
                survival_events=y_test_df['event'],
                evaluation_days=settings.evaluation_days,
                num_bins=settings.bins_calibration_benefit,
                binning_strategy=settings.bin_calibration_strategy
            )

            results[f"cfbcal_ece_{key}"] = float(calibration_benefit.expected_calibration_error)
            results[f"cfbcal_slope_{key}"] = float(calibration_benefit.regression_slope)
            results[f"cfbcal_intercept_{key}"] = float(calibration_benefit.regression_intercept)
        except ValueError:
            results[f"cfbcal_ece_{key}"] = float('nan')
            results[f"cfbcal_slope_{key}"] = float('nan')
            results[f"cfbcal_intercept_{key}"] = float('nan')

    return results
