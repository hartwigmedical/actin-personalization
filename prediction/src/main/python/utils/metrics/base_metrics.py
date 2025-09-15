# utils/base_metrics.py 
import numpy as np
import pandas as pd
import re
from sksurv.metrics import integrated_brier_score, brier_score, cumulative_dynamic_auc
from typing import Tuple, Dict, Any, Optional, List
from sksurv.util import Surv  

from sksurv.metrics import concordance_index_ipcw
from .calibration_benefit import compute_benefit_calibration, BenefitCalibrationResult
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
    pairs: pd.DataFrame,
    prA: pd.Series,
    prB: pd.Series,
    y_test_df: pd.DataFrame,
    tau: int,
    settings,
    treat_A: str,
    treat_B: str
) -> BenefitCalibrationResult:
    """
    Compute calibration-for-benefit for a given treatment pair.
    """
    return compute_benefit_calibration(
        pairs=pairs,
        predicted_prob_A=prA,
        predicted_prob_B=prB,
        survival_durations=y_test_df["duration"],
        survival_events=y_test_df["event"],
        evaluation_days=tau,
        num_bins=settings.bins_calibration_benefit,
        binning_strategy=settings.bin_calibration_strategy,
        treatment_A=treat_A,
        treatment_B=treat_B,
    )

def compute_cfb_for_treatment_pair(
    *,
    X_match: pd.DataFrame,
    S_tau: Dict[str, pd.Series],
    alive_tau: pd.Series,
    treatment_groups: List[str],
    treat_A: str,
    treat_B: str,
    match_cols: List[str],
    y_test_df: pd.DataFrame,
    tau: int,
    settings
) -> Dict[str, Any]:
    """
    Compute C-for-benefit for a given treatment pair.
    """
    key = re.sub(r'[^a-z0-9]+', '_', treat_B.lower()).strip('_')

    pairs = _CFB_MATCHER.build_pairs(
        X_val=X_match,
        treatment_groups=treatment_groups,
        treat_A=treat_A,
        treat_B=treat_B,
        feature_cols=match_cols,
    )
    pairs = _coerce_pair_label_dtype(pairs, target_index=X_match.index)

    if pairs.empty:
        return {
            f"cfb_{key}": float('nan'),
            f"cfbcal_ece_{key}": float('nan'),
            f"cfbcal_slope_{key}": float('nan'),
            f"cfbcal_intercept_{key}": float('nan')
        }

    prA = S_tau[treat_A]
    prB = S_tau[treat_B]

    dfp = pairs[["A_idx", "B_idx"]].copy()

    a_obs = alive_tau.reindex(dfp["A_idx"]).to_numpy(dtype=float)
    b_obs = alive_tau.reindex(dfp["B_idx"]).to_numpy(dtype=float)
    dfp["y_pair"] = b_obs - a_obs

    delta_A = (prB.reindex(dfp["A_idx"]).to_numpy(dtype=float)
            -  prA.reindex(dfp["A_idx"]).to_numpy(dtype=float))
    delta_B = (prB.reindex(dfp["B_idx"]).to_numpy(dtype=float)
            -  prA.reindex(dfp["B_idx"]).to_numpy(dtype=float))

    dfp["s_pair"] = delta_B - delta_A

    dfp = dfp.dropna(subset=["y_pair", "s_pair"])
    y_pair = dfp["y_pair"].to_numpy(dtype=int)
    s_pair = dfp["s_pair"].to_numpy(dtype=float)

    keep = (y_pair != 0) & np.isfinite(s_pair)
    y_pair = y_pair[keep]
    s_pair = s_pair[keep]

    m = len(y_pair)
    if m >= 2:
        conc = 0.0; info = 0
        for i in range(m):
            for j in range(i + 1, m):
                dy = y_pair[i] - y_pair[j]
                if dy == 0:
                    continue
                ds = s_pair[i] - s_pair[j]
                info += 1
                if ds == 0:
                    conc += 0.5
                elif np.sign(ds) == np.sign(dy):
                    conc += 1.0
        cfb = conc / info if info > 0 else float("nan")
    else:
        cfb = float("nan")

    calib = compute_calibration_for_benefit(
        pairs=pairs,
        prA=prA,
        prB=prB,
        y_test_df=y_test_df,
        tau=tau,
        settings=settings,
        treat_A=treat_A,
        treat_B=treat_B
    )

    return {
        f"cfb_{key}": float(cfb),
        f"cfbcal_ece_{key}": float(calib.expected_calibration_error),
        f"cfbcal_slope_{key}": float(calib.regression_slope),
        f"cfbcal_intercept_{key}": float(calib.regression_intercept)
    }
