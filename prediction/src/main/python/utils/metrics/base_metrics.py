# utils/base_metrics.py 
import numpy as np
import pandas as pd
from sksurv.metrics import integrated_brier_score, brier_score, cumulative_dynamic_auc
from typing import Tuple, Dict, Any, Optional, List

from sksurv.metrics import concordance_index_ipcw
from .counterfactuals import (
    _TREATMENT_COMPONENT_COLUMNS,
    apply_treatment_components,
    predict_survival_probabilities,
)
from .calibration_benefit import build_and_calibrate_benefit, BenefitCalibrationResult
from .cfb import _CFB_MATCHER, _coerce_pair_label_dtype


def filter_treatment_columns(feature_columns: List[str]) -> List[str]:
    excluded_columns = {
        "systemicTreatmentPlan_5-FU",
        "systemicTreatmentPlan_oxaliplatin",
        "systemicTreatmentPlan_irinotecan",
        "systemicTreatmentPlan_bevacizumab",
        "systemicTreatmentPlan_panitumumab",
        "systemicTreatmentPlan_pembrolizumab",
        "systemicTreatmentPlan_nivolumab",
        "hasTreatment",
        "treatment_group_idx",
    }
    return [column for column in feature_columns if column not in excluded_columns]

def compute_time_dependent_c_index(
    survival_predictions: np.ndarray,
    event_times: np.ndarray,
    event_observed: np.ndarray,
    evaluation_times: np.ndarray
) -> float:
    structured_outcomes = np.array(
        [(bool(event), time) for event, time in zip(event_observed, event_times)],
        dtype=[("event", "bool"), ("duration", "float64")]
    )

    c_index_scores = []
    for time_index, evaluation_time in enumerate(evaluation_times):
        if time_index >= survival_predictions.shape[1]:
            break
        risk_scores = 1 - survival_predictions[:, time_index]
        c_index = concordance_index_ipcw(
            structured_outcomes, structured_outcomes, risk_scores, tau=evaluation_time
        )
        c_index_scores.append(c_index[0])

    return float(np.mean(c_index_scores)) if c_index_scores else float("nan")

def compute_time_dependent_auc(
    y_train: np.ndarray,
    y_test: np.ndarray,
    risk_scores: np.ndarray,
    evaluation_times: np.ndarray
) -> Tuple[np.ndarray, float]:
    auc_times, mean_auc = cumulative_dynamic_auc(
        y_train, y_test, risk_scores, evaluation_times
    )
    return auc_times, mean_auc

def compute_integrated_brier_score(
    y_train: np.ndarray,
    y_test: np.ndarray,
    survival_predictions: np.ndarray,
    evaluation_times: np.ndarray,
    use_ipcw: bool = False
) -> float:
    if use_ipcw:
        brier_scores = [
            brier_score(y_train, y_test, survival_predictions[:, time_index], evaluation_time)[1][0]
            for time_index, evaluation_time in enumerate(evaluation_times)
        ]
        return np.trapz(brier_scores, evaluation_times) / (evaluation_times[-1] - evaluation_times[0])
    else:
        return integrated_brier_score(
            y_train, y_test, survival_predictions, evaluation_times
        )

def calibration_assessment(predictions: np.ndarray, y_test: pd.DataFrame, times: np.ndarray, n_bins: int = 10) -> float:
    calibration_errors = []
    for t in times:
        events_at_t = (y_test['event'] & (y_test['duration'] <= t)).astype(int)
        if len(predictions.shape) == 2:
            t_index = np.where(times == t)[0][0]
            preds_at_t = 1 - predictions[:, t_index]
        else: 
            preds_at_t = predictions

        bins = pd.qcut(preds_at_t, q=n_bins, duplicates="drop", labels=False)
        observed, expected = [], []

        for i in range(n_bins):
            idx = bins == i
            if idx.sum() > 0:
                observed.append(events_at_t[idx].mean())
                expected.append(preds_at_t[idx].mean())

        calibration_error = np.abs(np.array(observed) - np.array(expected)).mean()
        calibration_errors.append(calibration_error)

    return np.mean(calibration_errors)

def compute_calibration_for_benefit(
    X_val_with_tgi: pd.DataFrame,
    treatment_groups: List[str],
    treat_A: str,
    treat_B: str,
    feature_cols: List[str],
    pred_prob_A: pd.Series,
    pred_prob_B: pd.Series,
    durations: pd.Series,
    events: pd.Series,
    days: int = 365,
    n_bins: int = 10,
    bin_strategy: str = "quantile"
) -> BenefitCalibrationResult:

    return build_and_calibrate_benefit(
        matcher=_CFB_MATCHER,
        X_val_with_tgi=X_val_with_tgi,
        treatment_groups=treatment_groups,
        treat_A=treat_A,
        treat_B=treat_B,
        feature_cols=feature_cols,
        pred_prob_A=pred_prob_A,
        pred_prob_B=pred_prob_B,
        durations=durations,
        events=events,
        days=days,
        n_bins=n_bins,
        bin_strategy=bin_strategy
    )

def compute_c_for_benefit(
    model,
    X: pd.DataFrame,
    y: pd.DataFrame,
    treatment_groups: List[str],
    feature_columns: List[str],
    evaluation_days: int = 365
) -> pd.DataFrame:
    """
    Strict ITE-style C-for-benefit:
      - Build baseline-matched pairs A<->B (A=No Treatment, B=each active treatment).
      - For each person i: Delta_i = S_treat(i, τ) - S_no(i, τ).
      - For each pair p: s_hat(p) = Delta_B - Delta_A (continuous).
      - Observed pair label at τ: y(p) = I[B alive at τ] - I[A alive at τ] ∈ {-1,0,1}.
      - CFB = concordance over *pairs of pairs* on (y, s_hat); predicted ties get 0.5.
    """
    if "No Treatment" not in treatment_groups:
        raise ValueError("'No Treatment' must be in treatment_groups")
    if "treatment_group_idx" not in X.columns:
        raise ValueError("'treatment_group_idx' must be in X")

    # match only on baseline (no treatment columns, no tgi)
    match_cols = [c for c in feature_columns if c not in {
        "systemicTreatmentPlan_5-FU",
        "systemicTreatmentPlan_oxaliplatin",
        "systemicTreatmentPlan_irinotecan",
        "systemicTreatmentPlan_bevacizumab",
        "systemicTreatmentPlan_panitumumab",
        "systemicTreatmentPlan_pembrolizumab",
        "systemicTreatmentPlan_nivolumab",
        "hasTreatment",
        "treatment_group_idx",
    }]
    X_feat = X[[c for c in feature_columns if c != "treatment_group_idx"]].copy()
    X_match = X[match_cols + ["treatment_group_idx"]].copy()
    n = len(X_feat)

    # observed alive at τ
    durations = y["duration"]
    events = y["event"].astype(bool)
    alive_tau = (durations > evaluation_days) & (~(events & (durations <= evaluation_days)))

    # multi-task?
    is_multitask = hasattr(model, "predict_survival_function") and hasattr(model, "num_tasks")

    # S_no(τ) for everyone
    X_no = apply_treatment_components(X_feat.copy(), "No Treatment")
    if is_multitask:
        no_idx = treatment_groups.index("No Treatment")
        S_no = predict_survival_probabilities(model, X_no, evaluation_days,
                                              task_indices=np.full(n, no_idx, dtype=int))
    else:
        S_no = predict_survival_probabilities(model, X_no, evaluation_days, task_indices=None)

    out_rows = []
    for treat in treatment_groups:
        if treat == "No Treatment":
            continue

        # counterfactual S_treat(τ) & Delta
        try:
            X_t = apply_treatment_components(X_feat.copy(), treat)
        except ValueError:
            out_rows.append({"treatment": treat, "c_for_benefit": float("nan")})
            continue

        if is_multitask:
            t_idx = treatment_groups.index(treat)
            S_t = predict_survival_probabilities(model, X_t, evaluation_days,
                                                 task_indices=np.full(n, t_idx, dtype=int))
        else:
            S_t = predict_survival_probabilities(model, X_t, evaluation_days, task_indices=None)

        Delta = (S_t - S_no)

        # baseline matching: NoTx (A) vs current treatment (B)
        pairs = _CFB_MATCHER.build_pairs(
            X_val=X_match,
            treatment_groups=treatment_groups,
            treat_A="No Treatment",
            treat_B=treat,
            feature_cols=match_cols,
        )
        pairs = _coerce_pair_label_dtype(pairs, target_index=X_feat.index)
        if pairs.empty:
            out_rows.append({"treatment": treat, "c_for_benefit": float("nan")})
            continue

        # pair-level observed label & predicted score
        obs_a = alive_tau.reindex(pairs["A_idx"]).to_numpy(dtype=int)
        obs_b = alive_tau.reindex(pairs["B_idx"]).to_numpy(dtype=int)
        y_pair = obs_b - obs_a  # {-1,0,1}

        d_a = Delta.reindex(pairs["A_idx"]).to_numpy(dtype=float)
        d_b = Delta.reindex(pairs["B_idx"]).to_numpy(dtype=float)
        s_pair = d_b - d_a      # continuous

        # exclude observed ties; concordance over pairs of pairs w/ 0.5 for tied predictions
        keep = (y_pair != 0) & np.isfinite(s_pair)
        y_pair = y_pair[keep]
        s_pair = s_pair[keep]
        m = len(y_pair)
        if m < 2:
            out_rows.append({"treatment": treat, "c_for_benefit": float("nan")})
            continue

        concordant = 0.0
        informative = 0
        for i in range(m):
            for j in range(i + 1, m):
                dy = y_pair[i] - y_pair[j]
                if dy == 0:
                    continue
                ds = s_pair[i] - s_pair[j]
                informative += 1
                if ds == 0:
                    concordant += 0.5
                elif np.sign(ds) == np.sign(dy):
                    concordant += 1.0

        cfb = (concordant / informative) if informative > 0 else float("nan")
        out_rows.append({"treatment": treat, "c_for_benefit": float(cfb)})

    return pd.DataFrame(out_rows)
