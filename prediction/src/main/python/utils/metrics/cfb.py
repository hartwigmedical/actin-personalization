# utils/cfb.py

from __future__ import annotations

import numpy as np
import pandas as pd
import re
from typing import Any, Dict, List, Optional, Tuple
from sklearn.preprocessing import StandardScaler
from scipy.spatial.distance import cdist

  
def _coerce_pair_label_dtype(pairs: pd.DataFrame, target_index: pd.Index) -> pd.DataFrame:
    """
    Ensure A_idx/B_idx have the same dtype and canonical form as target_index.
    """
    pairs = pairs.copy()

    def _coerce_col(col: pd.Series) -> pd.Series:
        if pd.api.types.is_integer_dtype(target_index.dtype):
            c = pd.to_numeric(col, errors="coerce")
            c = c.astype("Int64") 
            if c.isna().any():
                c2 = pd.to_numeric(col.astype(str).str.replace(r"\.0$", "", regex=True), errors="coerce").astype("Int64")
                c = c.fillna(c2)
            return c.astype("int64", errors="ignore")
        if pd.api.types.is_string_dtype(target_index.dtype):
            return col.astype(str)
        try:
            return col.astype(target_index.dtype)
        except Exception:
            return col

    pairs["A_idx"] = _coerce_col(pairs["A_idx"])
    pairs["B_idx"] = _coerce_col(pairs["B_idx"])

    return pairs
    
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
        VI = np.linalg.pinv(cov + 1e-8*np.eye(cov.shape[0]))
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
        cache_key: Optional[Tuple] = None
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
            pairs = self._pair_cache[key]
          
            return pairs

        assert "treatment_group_idx" in X_val.columns, "'treatment_group_idx' must be present in X_val"

        A_idx = treatment_groups.index(treat_A)
        B_idx = treatment_groups.index(treat_B)

        tg = X_val["treatment_group_idx"].astype(int).values
        idx_A = X_val.loc[tg == A_idx].index.to_numpy()
        idx_B = X_val.loc[tg == B_idx].index.to_numpy()

        if len(idx_A) == 0 or len(idx_B) == 0:
            pairs = pd.DataFrame(columns=["A_idx", "B_idx", "distance"])
            self._pair_cache[key] = pairs
        
            return pairs
        
        raw = self._greedy_mahalanobis_pairs(X_val, idx_B, idx_A, feature_cols, one_to_one=False)
        pairs = raw.rename(columns={"A_idx": "B_idx", "B_idx": "A_idx"})[["A_idx", "B_idx", "distance"]]

        self._pair_cache[key] = pairs
        return pairs

    @staticmethod
    def score_from_pairs(
        pairs: pd.DataFrame,
        pred_prob_A: pd.Series | None = None,
        pred_prob_B: pd.Series | None = None,
        observed_survival: pd.Series | np.ndarray | None = None,
        observed_event: pd.Series | np.ndarray | None = None,
        days: int = 365,
        obs_pairwise_effect: pd.Series | None = None,
        pred_pairwise_effect: pd.Series | None = None,
    ) -> float:
        if pairs.empty:
            return float("nan")

        # ----- CONTINUOUS mode -----
        if (obs_pairwise_effect is not None) and (pred_pairwise_effect is not None):
            df = pairs.copy()
            df["obs"] = pd.Series(obs_pairwise_effect).values
            df["pred"] = pd.Series(pred_pairwise_effect).values

            df["obs"] = pd.to_numeric(df["obs"], errors="coerce")
            df["pred"] = pd.to_numeric(df["pred"], errors="coerce")

            df = df.dropna(subset=["obs", "pred"])

            idxs = df.index.to_list()
            concordant = discordant = 0
            for i in range(len(idxs)):
                oi = df.loc[idxs[i], "obs"]
                pi = df.loc[idxs[i], "pred"]
                for j in range(i + 1, len(idxs)):
                    oj = df.loc[idxs[j], "obs"]
                    pj = df.loc[idxs[j], "pred"]

                    if not all(isinstance(val, (int, float)) for val in [oi, oj, pi, pj]):
                        continue

                    obs_diff = oi - oj
                    if obs_diff == 0:
                        continue
                    pred_diff = pi - pj
                    if pred_diff == 0:
                        continue
                    if np.sign(obs_diff) == np.sign(pred_diff):
                        concordant += 1
                    else:
                        discordant += 1

            informative = concordant + discordant
            return (concordant / informative) if informative > 0 else float("nan")

        # ----- DISCRETE mode (Harrell-style) -----
        if (pred_prob_A is None) or (pred_prob_B is None) or \
           (observed_survival is None) or (observed_event is None):
            return float("nan")

        prA = pd.Series(pred_prob_A)
        prB = pd.Series(pred_prob_B)
        dur = observed_survival if isinstance(observed_survival, pd.Series) else pd.Series(observed_survival, index=prA.index)
        evt = observed_event    if isinstance(observed_event,    pd.Series) else pd.Series(observed_event,    index=prA.index)
        evt = evt.astype(bool)
        surv = (dur > days) & (~(evt & (dur <= days))) 

        df = pairs.copy()
        surv_d = surv.to_dict()
        prA_d  = prA.to_dict()
        prB_d  = prB.to_dict()

        df["obs_a"] = df["A_idx"].map(surv_d)
        df["obs_b"] = df["B_idx"].map(surv_d)
        df["pr_a"]  = df["A_idx"].map(prA_d)
        df["pr_b"]  = df["B_idx"].map(prB_d)

        mask = df[["obs_a", "obs_b", "pr_a", "pr_b"]].notna().all(axis=1)
        if not mask.any():
            return float("nan")

        y_obs  = (df.loc[mask, "obs_b"].astype(int) - df.loc[mask, "obs_a"].astype(int)).to_numpy(dtype=int)
        y_pred = np.sign((df.loc[mask, "pr_b"] - df.loc[mask, "pr_a"]).to_numpy(dtype=float)).astype(int)

        obs_non_tie = (y_obs != 0)
        denom = int(obs_non_tie.sum())
        if denom == 0:
            return float("nan")

        signs_match = (np.sign(y_pred[obs_non_tie]) == np.sign(y_obs[obs_non_tie]))
        pred_tie_nt = (y_pred[obs_non_tie] == 0)
        concordant = int(signs_match.sum())
        half_credit = 0.5 * float(pred_tie_nt.sum())
        return float((concordant + half_credit) / denom)

_CFB_MATCHER = CForBenefitCovariateMatcher()
