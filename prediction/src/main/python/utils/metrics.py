import numpy as np
import pandas as pd
from sksurv.metrics import integrated_brier_score, brier_score, cumulative_dynamic_auc
from typing import Tuple
from sksurv.metrics import concordance_index_ipcw
from .cfb import _CFB_MATCHER

from typing import Dict, Any, Optional, List

def calculate_time_dependent_c_index(survival_predictions: np.ndarray, event_times: np.ndarray, event_observed: np.ndarray, times: np.ndarray) -> float:
    y_struct = np.array(
        [(bool(e), t) for e, t in zip(event_observed, event_times)],
        dtype=[("event", "bool"), ("duration", "float64")]
    )

    cindex_scores = []
    for i, t in enumerate(times):
        if i >= survival_predictions.shape[1]:
            break
        risk_scores = 1 - survival_predictions[:, i]
        c_index = concordance_index_ipcw(y_struct, y_struct, risk_scores, tau=t)
        cindex_scores.append(c_index[0])

    return float(np.mean(cindex_scores)) if cindex_scores else float("nan")

def calculate_time_dependent_auc(y_train: np.ndarray, y_test: np.ndarray, risk_scores: np.ndarray, times: np.ndarray) -> Tuple[
    np.ndarray, float]:
    auc_times, mean_auc = cumulative_dynamic_auc(y_train, y_test, risk_scores, times)
    return auc_times, mean_auc


def calculate_brier_score(y_train: np.ndarray, y_test: np.ndarray, prediction: np.ndarray, times: np.ndarray, ipcw: bool = False) -> float:
    if ipcw:
        bs_scores = []
        for t in times:
            bs = brier_score(y_train, y_test, prediction, t)[1][0]
            bs_scores.append(bs)
        ibs = np.trapz(bs_scores, times) / (times[-1] - times[0])
    else:
        ibs = integrated_brier_score(y_train, y_test, prediction, times)

    return ibs


def calibration_assessment(predictions: np.ndarray, y_test: pd.DataFrame, times: np.ndarray, n_bins: int = 10) -> float:
    calibration_errors = []
    for t in times:
        # Binarize event occurrence at time t
        events_at_t = (y_test['event'] & (y_test['duration'] <= t)).astype(int)
        if len(predictions.shape) == 2:
            t_index = np.where(times == t)[0][0]
            preds_at_t = 1 - predictions[:, t_index]
        else:  # Risk scores
            preds_at_t = predictions

            # Bin predictions
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

# ---------------- C-for-benefit helpers ----------------
#TODO: clean this up

_TREATMENT_COMPONENT_COLS = [
    "systemicTreatmentPlan_5-FU",
    "systemicTreatmentPlan_oxaliplatin",
    "systemicTreatmentPlan_irinotecan",
    "systemicTreatmentPlan_bevacizumab",
    "systemicTreatmentPlan_panitumumab",
    "systemicTreatmentPlan_pembrolizumab",
    "systemicTreatmentPlan_nivolumab",
]

_TREATMENT_LABEL_TO_COMPONENTS: Dict[str, Tuple[int,int,int,int,int,int,int]] = {
    "No Treatment":                                         (0,0,0,0,0,0,0),
    "5-FU":                                                 (1,0,0,0,0,0,0),
    "5-FU + bevacizumab":                                   (1,0,0,1,0,0,0),
    "5-FU + oxaliplatin":                                   (1,1,0,0,0,0,0),
    "5-FU + oxaliplatin + bevacizumab":                     (1,1,0,1,0,0,0),
    "5-FU + oxaliplatin + panitumumab":                     (1,1,0,0,1,0,0),
    "5-FU + irinotecan":                                    (1,0,1,0,0,0,0),
    "5-FU + irinotecan + bevacizumab":                      (1,0,1,1,0,0,0),
    "5-FU + oxaliplatin + irinotecan":                      (1,1,1,0,0,0,0),
    "5-FU + oxaliplatin + irinotecan + bevacizumab":        (1,1,1,1,0,0,0),
    "PEMBROLIZUMAB":                                        (0,0,0,0,0,1,0),
}

def _ensure_component_cols(X: pd.DataFrame) -> pd.DataFrame:
    """Make sure all systemicTreatmentPlan_* columns exist; if missing, add zeros."""
    add = {c: 0 for c in _TREATMENT_COMPONENT_COLS if c not in X.columns}
    if add:
        X = X.copy()
        for c, v in add.items():
            X[c] = v
    return X

def _set_treatment_components(X: pd.DataFrame, treatment_label: str) -> pd.DataFrame:
    """Return a copy of X with the 7 treatment component columns set according to the label."""
    if treatment_label not in _TREATMENT_LABEL_TO_COMPONENTS:
        raise ValueError(f"Unknown treatment label in mapping: {treatment_label}")
    X = _ensure_component_cols(X)
    vals = _TREATMENT_LABEL_TO_COMPONENTS[treatment_label]
    X_cf = X.copy()
    for col, v in zip(_TREATMENT_COMPONENT_COLS, vals):
        X_cf[col] = v
    # Optional: update hasTreatment if present
    if "hasTreatment" in X_cf.columns:
        X_cf["hasTreatment"] = int(any(vals))
    return X_cf

def _interp_cols_at_days(surv_df: pd.DataFrame, days: float) -> np.ndarray:
    """surv_df shape [n_times, n_samples] with index=times; return S(days) per sample."""
    t = surv_df.index.to_numpy(dtype=float)
    arr = surv_df.to_numpy(dtype=float)
    out = np.empty(arr.shape[1], dtype=float)
    for i in range(arr.shape[1]):
        out[i] = np.interp(days, t, arr[:, i])
    return out

def _predict_1yr_prob(
    model,
    X_input: pd.DataFrame,
    days: int,
    task_idx: Optional[np.ndarray] = None
) -> pd.Series:
    """
    Uniformly get 1-year survival probabilities S(days) for a batch.
    - Multi-task models: call predict_survival_function(X, task_idx) if provided.
    - Single-task models: call predict_survival_function(X) and handle list-of-callables or DataFrame.
    Returns a pd.Series indexed by X_input.index.
    """
    try:
        if task_idx is not None:
            sf = model.predict_survival_function(X_input, task_idx)
        else:
            sf = model.predict_survival_function(X_input)
    except TypeError:
        sf = model.predict_survival_function(X_input)

    if isinstance(sf, pd.DataFrame):
        probs = _interp_cols_at_days(sf, days)
        return pd.Series(probs, index=X_input.index)

    if isinstance(sf, (list, tuple)):
        probs = np.array([float(func(days)) for func in sf], dtype=float)
        return pd.Series(probs, index=X_input.index)

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
        sf_df = pd.DataFrame(arr, index=tgrid)
        probs = _interp_cols_at_days(sf_df, days)
        return pd.Series(probs, index=X_input.index)

    raise ValueError("Unsupported output from predict_survival_function.")

def compute_c_for_benefit_per_treatment(
    model,
    X_val_with_tgi: pd.DataFrame,       
    y_df: pd.DataFrame,                 
    treatment_groups: List[str],
    feature_cols: List[str],
    days: int = 365,
) -> pd.DataFrame:
    """
    For each treatment vs 'No Treatment', compute C-for-benefit using covariate-matched pairs.

    - Multi-task models: use (X with counterfactual treatment components, task_idx).
    - Single-task models: toggle treatment component columns in X and call the single head.
    - Matching is done on baseline covariates ONLY (treatment flags excluded).
    """
    if "No Treatment" not in treatment_groups:
        raise ValueError("'No Treatment' must be in treatment_groups")
    if "treatment_group_idx" not in X_val_with_tgi.columns:
        raise ValueError("'treatment_group_idx' must be present in X_val_with_tgi")

    clean_feature_cols = [c for c in feature_cols if c != "treatment_group_idx"]
    X_feat_base = X_val_with_tgi[clean_feature_cols].copy()
    n = len(X_feat_base)

    TREATMENT_COMPONENT_COLS = [
        "systemicTreatmentPlan_5-FU",
        "systemicTreatmentPlan_oxaliplatin",
        "systemicTreatmentPlan_irinotecan",
        "systemicTreatmentPlan_bevacizumab",
        "systemicTreatmentPlan_panitumumab",
        "systemicTreatmentPlan_pembrolizumab",
        "systemicTreatmentPlan_nivolumab",
    ]
    match_cols = [
        c for c in clean_feature_cols
        if c not in TREATMENT_COMPONENT_COLS + ["hasTreatment"]
    ]

    is_multitask = False
    try:
        _ = model.predict_survival_function(X_feat_base, np.zeros(n, dtype=int))
        is_multitask = True
    except TypeError:
        is_multitask = False
    except Exception:
        is_multitask = False

    X_no = _set_treatment_components(X_feat_base, "No Treatment")
    if is_multitask:
        no_idx = treatment_groups.index("No Treatment")
        prob_no = _predict_1yr_prob(model, X_no, days, task_idx=np.full(n, no_idx, dtype=int))
    else:
        prob_no = _predict_1yr_prob(model, X_no, days, task_idx=None)

    results: List[Dict[str, float]] = []

    for treat in treatment_groups:
        if treat == "No Treatment":
            continue
        if treat not in _TREATMENT_LABEL_TO_COMPONENTS:
            results.append({"treatment": treat, "c_for_benefit": float("nan")})

    for treat in treatment_groups:
        if treat == "No Treatment":
            continue

        try:
            X_t = _set_treatment_components(X_feat_base, treat)
        except ValueError:
            results.append({"treatment": treat, "c_for_benefit": float("nan")})
            continue

        if is_multitask:
            t_idx = treatment_groups.index(treat)
            prob_t = _predict_1yr_prob(model, X_t, days, task_idx=np.full(n, t_idx, dtype=int))
        else:
            prob_t = _predict_1yr_prob(model, X_t, days, task_idx=None)

        pairs = _CFB_MATCHER.build_pairs(
            X_val=X_val_with_tgi[match_cols + ["treatment_group_idx"]],
            treatment_groups=treatment_groups,
            treat_A="No Treatment",
            treat_B=treat,
            feature_cols=match_cols,
        )

        cfb = _CFB_MATCHER.score_from_pairs(
            pairs=pairs,
            pred_prob_A=prob_no,         
            pred_prob_B=prob_t,             
            observed_survival=y_df["duration"],
            observed_event=y_df["event"],
            days=days,
        )
        results.append({"treatment": treat, "c_for_benefit": float(cfb) if cfb == cfb else float("nan")})

    return pd.DataFrame(results)

