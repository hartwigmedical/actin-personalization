import numpy as np
import pandas as pd
from pycox.evaluation import EvalSurv
from sksurv.metrics import integrated_brier_score, brier_score, cumulative_dynamic_auc
from typing import Tuple


def calculate_time_dependent_c_index(survival_predictions: np.ndarray, event_times: np.ndarray, event_observed: np.ndarray, times: np.ndarray) -> float:

    survival_predictions = pd.DataFrame(survival_predictions.T, index=times)
    
    ev = EvalSurv(survival_predictions, event_times, event_observed, censor_surv=None)

    time_dependent_c_index = ev.concordance_td()
    return time_dependent_c_index

def calculate_time_dependent_auc(y_train: np.ndarray, y_test: np.ndarray, risk_scores: np.ndarray, times: np.ndarray) -> Tuple[np.ndarray, float]:
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
        else: # Risk scores
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

