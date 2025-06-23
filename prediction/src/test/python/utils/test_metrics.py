import pytest
import numpy as np
import pandas as pd

from utils.metrics import (
    calculate_time_dependent_c_index,
    calculate_time_dependent_auc,
    calculate_brier_score,
    calibration_assessment
)

from sksurv.metrics import cumulative_dynamic_auc, integrated_brier_score, brier_score
from pycox.evaluation import EvalSurv


@pytest.fixture
def survival_data():
    y_train = np.array([
        (True, 10),
        (False, 20),
        (True, 30),
        (False, 40),
        (True, 50),
        (False, 60),
    ], dtype=[("event", bool), ("duration", int)])

    y_test = np.array([
        (True, 12),
        (False, 25),
        (True, 35),
        (False, 45),
        (True, 55),
        (False, 65),
    ], dtype=[("event", bool), ("duration", int)])

    times = np.array([15, 35, 55])

    return y_train, y_test, times


@pytest.fixture
def survival_predictions():
    return np.array([
        [0.8, 0.6, 0.4],
        [0.85, 0.65, 0.45],
        [0.75, 0.55, 0.35],
        [0.9, 0.7, 0.5],
        [0.7, 0.5, 0.3],
        [0.95, 0.8, 0.6],
    ])


@pytest.fixture
def risk_scores():
    return np.array([0.3, 0.6, 0.4, 0.2, 0.5, 0.1])


@pytest.fixture
def y_test_df(survival_data):
    y_test = survival_data[1]
    return pd.DataFrame({
        "event": y_test["event"],
        "duration": y_test["duration"]
    })


def test_time_dependent_c_index(survival_data, survival_predictions):
    _, y_test, times = survival_data
    score = calculate_time_dependent_c_index(survival_predictions, y_test["duration"], y_test["event"], times)
    assert np.isclose(score, 0.61075, atol=1e-6)

def test_time_dependent_auc(survival_data, risk_scores):
    y_train, y_test, times = survival_data
    aucs, mean_auc = calculate_time_dependent_auc(y_train, y_test, risk_scores, times)

    assert np.allclose(aucs, [0.4, 0.66666666, 1.0], atol=1e-6)
    assert np.isclose(mean_auc, 0.753535353535, atol=1e-6)

def test_brier_score(survival_data, survival_predictions):
    y_train, y_test, times = survival_data
    ibs = calculate_brier_score(y_train, y_test, survival_predictions, times)

    assert np.isclose(ibs, 0.1681510416666, atol=1e-10)

def test_brier_score_ipcw(survival_data, risk_scores):
    y_train, y_test, times = survival_data
    ibs = calculate_brier_score(y_train, y_test, risk_scores, times, ipcw=True)

    assert np.isclose(ibs, 0.39239583333, atol=1e-8)

def test_calibration_error(survival_predictions, y_test_df, survival_data):
    _, _, times = survival_data
    err = calibration_assessment(survival_predictions, y_test_df, times)

    assert np.isclose(err, 0.3694444444, atol=1e-4)
