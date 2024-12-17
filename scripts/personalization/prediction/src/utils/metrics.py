import numpy as np
from lifelines.utils import concordance_index
from sksurv.metrics import concordance_index_censored, integrated_brier_score, brier_score, cumulative_dynamic_auc
import pandas as pd
from sklearn.calibration import calibration_curve

def calculate_c_index(event_times, predicted_scores, event_observed):
    c_index = concordance_index_censored(event_observed, event_times, predicted_scores)[0]
    return c_index

def calculate_time_dependent_auc(y_train, y_test, risk_scores, times):
    auc_times, auc_scores = cumulative_dynamic_auc(y_train, y_test, risk_scores, times)
    return auc_times, auc_scores

def calculate_brier_score(y_train, y_test, prediction, times, ipcw = False):
    """
    Calculate the Integrated Brier Score (IBS) for survival predictions.
    """

    if ipcw:
        bs_scores = []
        for t in times:
            bs = brier_score(y_train, y_test, prediction, t)[1][0]
            bs_scores.append(bs)
        ibs = np.trapz(bs_scores, times) / (times[-1] - times[0])
    else:
        ibs = integrated_brier_score(y_train, y_test, prediction, times)
        
    return ibs

def calibration_assessment(predictions, y_test, times, n_bins=10):
    """
    Assess calibration for all models (survival probabilities or risk scores).
    Parameters:
    - predictions: Risk scores or survival probabilities (1 - survival probability for event probabilities).
    - y_test: Structured array with 'event' and 'duration'.
    - times: Array of time points to assess calibration.
    - n_bins: Number of bins for grouping predictions.

    Returns:
    - Mean calibration error across time points.
    """
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

