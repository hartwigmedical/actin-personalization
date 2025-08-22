# utils/cfb.py

from __future__ import annotations

import numpy as np
import pandas as pd
from typing import Dict, List, Optional, Tuple
from sklearn.preprocessing import StandardScaler
from scipy.spatial.distance import cdist


class CForBenefitCovariateMatcher:
    def __init__(self):
        self._pair_cache: Dict[Tuple, pd.DataFrame] = {}

    @staticmethod
    def _greedy_mahalanobis_pairs(
        X: pd.DataFrame,
        idx_A: np.ndarray,
        idx_B: np.ndarray,
        feature_cols: List[str],
        one_to_one: bool = False,
    ) -> pd.DataFrame:
        Xa = X.loc[idx_A, feature_cols].astype(float).values
        Xb = X.loc[idx_B, feature_cols].astype(float).values

        scaler = StandardScaler()
        X_all = np.vstack([Xa, Xb])
        scaler.fit(X_all)
        Xa_s = scaler.transform(Xa)
        Xb_s = scaler.transform(Xb)

        cov = np.cov(X_all, rowvar=False)
        VI = np.linalg.pinv(cov)
        D = cdist(Xa_s, Xb_s, metric="mahalanobis", VI=VI)
    
        rows = []
        if one_to_one:
            used_B = set()
            for i in range(D.shape[0]):                
                for j in np.argsort(D[i]):             
                    if j in used_B:
                        continue
                    rows.append((idx_A[i], idx_B[j], float(D[i, j])))
                    used_B.add(j)
                    break                                
        else:
            js = np.argmin(D, axis=1)
            rows = [(idx_A[i], idx_B[j], float(D[i, j])) for i, j in enumerate(js)]

        return pd.DataFrame(rows, columns=["A_idx", "B_idx", "distance"])

    def build_pairs(
        self,
        X_val: pd.DataFrame,
        treatment_groups: List[str],
        treat_A: str,
        treat_B: str,
        feature_cols: List[str],
        cache_key: Optional[Tuple] = None,
    ) -> pd.DataFrame:
        if cache_key is None:
            key = (
                tuple(X_val.index.tolist()),
                treat_A,
                treat_B,
                tuple(feature_cols),
            )
        else:
            key = cache_key

        if key in self._pair_cache:
            return self._pair_cache[key]

        assert "treatment_group_idx" in X_val.columns, "'treatment_group_idx' must be present in X_val"

        A_idx = treatment_groups.index(treat_A)
        B_idx = treatment_groups.index(treat_B)

        idx_A = X_val.index[X_val["treatment_group_idx"].astype(int).values == A_idx].to_numpy()
        idx_B = X_val.index[X_val["treatment_group_idx"].astype(int).values == B_idx].to_numpy()

        if len(idx_A) == 0 or len(idx_B) == 0:
            pairs = pd.DataFrame(columns=["A_idx", "B_idx", "distance"])
            self._pair_cache[key] = pairs
            return pairs

        pairs = self._greedy_mahalanobis_pairs(X_val, idx_A, idx_B, feature_cols, one_to_one=False)
        self._pair_cache[key] = pairs
        return pairs

    @staticmethod
    def score_from_pairs(
        pairs: pd.DataFrame,
        pred_prob_A: pd.Series,             # 1-year S under A (index = patients)
        pred_prob_B: pd.Series,             # 1-year S under B (index = patients)
        observed_survival,                  # vector or Series; days
        observed_event,                     # vector or Series; 0/1
        days: int = 365,
    ) -> float:
        """
        C-for-benefit from fixed A<->B pairs and predicted survival per patient.

        Observed effect per pair:
          +1 if (B survives & A doesn't)
          -1 if (A survives & B doesn't)
           0 otherwise (both survive or both don't by the horizon)

        Predicted pair effect:
          mean of individual treatment effects (ITE = S_B - S_A) across the two paired patients.
          This stabilizes rank comparisons vs. using a difference of ITEs.
        """
        if pairs.empty:
            return float("nan")

        # Ensure alignment
        if not isinstance(observed_survival, pd.Series):
            observed_survival = pd.Series(np.asarray(observed_survival), index=pred_prob_A.index)
        if not isinstance(observed_event, pd.Series):
            observed_event = pd.Series(np.asarray(observed_event), index=pred_prob_A.index)

        # Binary "survived by days" indicator (1 if alive/no event by horizon)
        surv = pd.Series(np.asarray(observed_survival), index=pred_prob_A.index)
        evt  = pd.Series(np.asarray(observed_event),   index=pred_prob_A.index)
        obs_bin = pd.Series(index=surv.index, dtype="float")
        obs_bin[(evt == 1) & (surv < days)] = 0
        obs_bin[(evt == 0) | (surv >= days)] = 1

        # Individual ITE = S_B - S_A
        te = pred_prob_B - pred_prob_A

        df = pairs.copy()
        df["A_surv"] = obs_bin.loc[df["A_idx"]].values
        df["B_surv"] = obs_bin.loc[df["B_idx"]].values
        df = df.dropna(subset=["A_surv", "B_surv"])

        df["obs_effect"] = np.select(
            [
                (df["B_surv"] == 1) & (df["A_surv"] == 0),
                (df["B_surv"] == 0) & (df["A_surv"] == 1),
            ],
            [1, -1],
            default=0,
        )

        df["A_te"] = te.loc[df["A_idx"]].values
        df["B_te"] = te.loc[df["B_idx"]].values
        df["pred_pair_te"] = 0.5 * (df["A_te"] + df["B_te"])

        idxs = df.index.to_list()
        concordant = discordant = 0
        for i in range(len(idxs)):
            for j in range(i + 1, len(idxs)):
                ei = df.loc[idxs[i], "obs_effect"]
                ej = df.loc[idxs[j], "obs_effect"]
                if ei == ej:      
                    continue
                pi = df.loc[idxs[i], "pred_pair_te"]
                pj = df.loc[idxs[j], "pred_pair_te"]
                win_obs  = np.sign(ei - ej)
                win_pred = np.sign(pi - pj)
                if win_pred == 0:
                    continue
                if win_obs == win_pred:
                    concordant += 1
                else:
                    discordant += 1

        informative = concordant + discordant
        return (concordant / informative) if informative > 0 else float("nan")

    def clear_cache(self):
        self._pair_cache.clear()

        
_CFB_MATCHER = CForBenefitCovariateMatcher()