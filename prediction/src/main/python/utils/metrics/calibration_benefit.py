# utils/calibration_benefit.py

from __future__ import annotations
import numpy as np
import pandas as pd
from dataclasses import dataclass
from typing import List

@dataclass
class BenefitCalibrationResult:
    calibration_curve: pd.DataFrame  # Columns: predicted_bin, count, predicted_mean, observed_mean
    expected_calibration_error: float
    regression_slope: float
    regression_intercept: float
    evaluation_days: int
    treatment_A: str
    treatment_B: str

def weighted_linear_regression(y: np.ndarray, x: np.ndarray, weights: np.ndarray) -> tuple[float, float]:
    """
    Robust weighted least squares fit of y ~ intercept + slope * x.
    Returns (slope, intercept).
    Handles degenerate/ill-conditioned cases gracefully.
    """
    # coerce and mask finites
    x = np.asarray(x, dtype=float)
    y = np.asarray(y, dtype=float)
    w = np.asarray(weights, dtype=float)

    mask = np.isfinite(x) & np.isfinite(y) & np.isfinite(w) & (w > 0)
    if mask.sum() < 2:
        return 0.0, float(np.nanmean(y))  

    x, y, w = x[mask], y[mask], w[mask]

    # normalize weights to avoid scale issues
    w_sum = w.sum()
    if w_sum == 0:
        return 0.0, float(np.nanmean(y))
    w = w / w_sum

    xbar = np.sum(w * x)
    ybar = np.sum(w * y)

    x_c = x - xbar
    y_c = y - ybar
    Sxx = np.sum(w * x_c * x_c)
    Sxy = np.sum(w * x_c * y_c)

    # if x has (almost) no spread, slope is undefined → set slope 0
    eps = 1e-12
    if not np.isfinite(Sxx) or Sxx < eps:
        slope = 0.0
        intercept = ybar
        return float(slope), float(intercept)

    slope = Sxy / Sxx
    if not np.isfinite(slope):
        slope = 0.0
    intercept = ybar - slope * xbar
    return float(slope), float(intercept)


def align_pair_labels_with_index(pairs: pd.DataFrame, target_index: pd.Index) -> pd.DataFrame:
    """
    Ensure pair labels (A_idx, B_idx) match the dtype of the target index.
    Handles issues like float-to-int conversion or string mismatches.
    """
    def coerce_dtype(column: pd.Series, reference_dtype) -> pd.Series:
        if pd.api.types.is_integer_dtype(reference_dtype):
            coerced = pd.to_numeric(column, errors="coerce")
            if coerced.isna().any():
                coerced = pd.to_numeric(column.astype(str).str.replace(r"\.0$", "", regex=True), errors="coerce")
            return coerced.astype("Int64").astype("int64", errors="ignore")
        if pd.api.types.is_string_dtype(reference_dtype):
            return column.astype(str)
        try:
            return column.astype(reference_dtype)
        except Exception:
            return column

    pairs["A_idx"] = coerce_dtype(pairs["A_idx"], target_index.dtype)
    pairs["B_idx"] = coerce_dtype(pairs["B_idx"], target_index.dtype)
    return pairs

def compute_benefit_calibration(
    pairs: pd.DataFrame,
    predicted_prob_A: pd.Series,
    predicted_prob_B: pd.Series,
    survival_durations: pd.Series | np.ndarray,
    survival_events: pd.Series | np.ndarray,
    evaluation_days: int = 365,
    num_bins: int = 10,
    binning_strategy: str = "quantile",
    treatment_A: str = "No Treatment",
    treatment_B: str = "5-FU + oxaliplatin",
) -> BenefitCalibrationResult:
    """
    Compute calibration-for-benefit by comparing predicted and observed pairwise benefits.
    """
    if pairs.empty:
        return BenefitCalibrationResult(
            calibration_curve=pd.DataFrame(columns=["predicted_bin", "count", "predicted_mean", "observed_mean"]),
            expected_calibration_error=float("nan"),
            regression_slope=float("nan"),
            regression_intercept=float("nan"),
            evaluation_days=evaluation_days,
            treatment_A=treatment_A,
            treatment_B=treatment_B,
        )

    predicted_prob_A = pd.Series(predicted_prob_A)
    predicted_prob_B = pd.Series(predicted_prob_B)

    survival_durations = pd.Series(survival_durations, index=predicted_prob_A.index)
    survival_events = pd.Series(survival_events, index=predicted_prob_A.index).astype(bool)

    survived_to_days = (survival_durations > evaluation_days) & (~(survival_events & (survival_durations <= evaluation_days)))

    pairs = align_pair_labels_with_index(pairs, predicted_prob_A.index)

    pairs["predicted_benefit"] = pairs["B_idx"].map(predicted_prob_B) - pairs["A_idx"].map(predicted_prob_A)
    pairs["observed_benefit"] = pairs["B_idx"].map(survived_to_days).astype(float) - pairs["A_idx"].map(survived_to_days).astype(float)

    pairs = pairs.dropna(subset=["predicted_benefit", "observed_benefit"])

    if pairs.empty:
        return BenefitCalibrationResult(
            calibration_curve=pd.DataFrame(columns=["predicted_bin", "count", "predicted_mean", "observed_mean"]),
            expected_calibration_error=float("nan"),
            regression_slope=float("nan"),
            regression_intercept=float("nan"),
            evaluation_days=evaluation_days,
            treatment_A=treatment_A,
            treatment_B=treatment_B,
        )

    if binning_strategy == "quantile":
        pairs["predicted_bin"] = pd.qcut(pairs["predicted_benefit"], q=num_bins, duplicates="drop")
    elif binning_strategy == "uniform":
        pairs["predicted_bin"] = pd.cut(pairs["predicted_benefit"], bins=num_bins)
    else:
        raise ValueError("binning_strategy must be 'quantile' or 'uniform'")

    calibration_curve = (
        pairs.groupby("predicted_bin")
        .agg(
            count=("predicted_benefit", "size"),
            predicted_mean=("predicted_benefit", "mean"),
            observed_mean=("observed_benefit", "mean"),
        )
        .reset_index()
    )

    weights = calibration_curve["count"].to_numpy(dtype=float)
    predicted_means = calibration_curve["predicted_mean"].to_numpy(dtype=float)
    observed_means = calibration_curve["observed_mean"].to_numpy(dtype=float)

    if weights.sum() > 0:
        normalized_weights = weights / weights.sum()
        slope, intercept = weighted_linear_regression(observed_means, predicted_means, weights)
    else:
        normalized_weights = np.ones_like(weights) / max(len(weights), 1)
        slope, intercept = weighted_linear_regression(observed_means, predicted_means, np.ones_like(weights))

    expected_calibration_error = float(np.sum(normalized_weights * np.abs(observed_means - predicted_means)))

    calibration_curve["predicted_bin"] = calibration_curve["predicted_bin"].astype(str)

    return BenefitCalibrationResult(
        calibration_curve=calibration_curve[["predicted_bin", "count", "predicted_mean", "observed_mean"]],
        expected_calibration_error=expected_calibration_error,
        regression_slope=float(slope),
        regression_intercept=float(intercept),
        evaluation_days=evaluation_days,
        treatment_A=treatment_A,
        treatment_B=treatment_B,
    )

def build_and_calibrate_benefit(
    validation_data: pd.DataFrame,
    treatment_groups: List[str],
    treatment_A: str,
    treatment_B: str,
    feature_columns: List[str],
    predicted_prob_A: pd.Series,
    predicted_prob_B: pd.Series,
    survival_durations: pd.Series,
    survival_events: pd.Series,
    evaluation_days: int = 365,
    num_bins: int = 10,
    binning_strategy: str = "quantile",
) -> BenefitCalibrationResult:
    """
    Build matched pairs for treatments A and B, then compute calibration-for-benefit.
    """
    from .cfb import CForBenefitCovariateMatcher  # Moved import to avoid circular dependency

    matcher = CForBenefitCovariateMatcher()

    pairs = matcher.build_pairs(
        X_val=validation_data[feature_columns + ["treatment_group_idx"]],
        treatment_groups=treatment_groups,
        treat_A=treatment_A,
        treat_B=treatment_B,
        feature_cols=feature_columns,
    )

    pairs = align_pair_labels_with_index(pairs, pd.Series(predicted_prob_A).index)

    return compute_benefit_calibration(
        pairs=pairs,
        predicted_prob_A=predicted_prob_A,
        predicted_prob_B=predicted_prob_B,
        survival_durations=survival_durations,
        survival_events=survival_events,
        evaluation_days=evaluation_days,
        num_bins=num_bins,
        binning_strategy=binning_strategy,
        treatment_A=treatment_A,
        treatment_B=treatment_B,
    )
