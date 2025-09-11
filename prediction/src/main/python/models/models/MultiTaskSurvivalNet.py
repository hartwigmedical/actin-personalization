import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import torchtuples as tt

from typing import Dict, Any, Optional, List
from scipy.interpolate import interp1d

from pycox.models import CoxPH, LogisticHazard, DeepHitSingle, PCHazard, MTLR

from models.models.survival_models import BaseSurvivalModel
from utils.settings import config_settings

torch.manual_seed(0)

   
class MultiTaskSurvivalNet(nn.Module):
    def __init__(
        self,
        input_size: int,
        num_nodes: list[int],
        out_features: int,
        num_tasks: int,
        activation: type[nn.Module] = nn.ReLU,
        dropout: float = 0.1,
        batch_norm: bool = False,
        use_attention: bool = False,
        attn_kwargs: dict | None = None,
        msi_index: int | None = None, 
        ras_index: int | None = None
    ):
        super().__init__()
        self.use_attention = use_attention
        self.msi_index = msi_index
        self.ras_index = ras_index
        
        if self.use_attention:
            self.attn = FeatureAttention(input_size, **(attn_kwargs or {}))
        else:
            self.attn = None

        layers: list[nn.Module] = []
        prev = input_size
        for h in num_nodes:
            layers.append(nn.Linear(prev, h))
            if batch_norm:
                layers.append(nn.BatchNorm1d(h))
            layers.append(activation())
            if dropout:
                layers.append(nn.Dropout(dropout))
            prev = h
        self.shared = nn.Sequential(*layers)

        self.heads = nn.ModuleList([nn.Linear(prev, out_features) 
                                     for _ in range(num_tasks)])

    def forward(self, x: torch.Tensor, task_idx: torch.Tensor):
        x_raw = x
        if self.use_attention:
            x = self.attn(x)
        h = self.shared(x)
        all_out = torch.stack([head(h) for head in self.heads], dim=1)  

        if config_settings.use_gate:
            panit = config_settings.treatment_idx.get("5-FU + oxaliplatin + panitumumab")
            if panit is not None and hasattr(self, "ras_index"):
                ras_idx = self.ras_index
                if ras_idx is not None:
                    ras = x_raw[:, ras_idx].unsqueeze(1)
                    gate = (1 - ras).clamp(0, 1).view(-1, 1, 1)
                    all_out[:, panit:panit+1, :] *= gate

            immu = config_settings.treatment_idx.get("PEMBROLIZUMAB")
            if immu is not None and hasattr(self, "msi_index"):
                msi_idx = self.msi_index
                if msi_idx is not None:
                    msi = x_raw[:, msi_idx].unsqueeze(1)
                    gate = msi.clamp(0, 1).view(-1, 1, 1)
                    all_out[:, immu:immu+1, :] *= gate

        idx = task_idx.view(-1, 1, 1).expand(-1, 1, all_out.size(-1))
        out = all_out.gather(1, idx).squeeze(1)
        return out
    
class MultiTaskNNSurvivalModel(BaseSurvivalModel):
    def __init__(
        self,
        model_class,
        input_size,
        num_nodes=[128, 64],
        num_tasks=10,
        num_durations=60,
        dropout=0.1,
        lr=1e-3,
        batch_norm=False,
        activation='relu',
        epochs=50,
        use_attention=False,
        attn_kwargs=None,
        msi_index: int | None = None, 
        ras_index: int | None = None,
        **kwargs
    ):
        super().__init__()
        activation_map = {'relu': nn.ReLU, 'elu': nn.ELU, 'swish': lambda: nn.SiLU()}
        activation_fn = activation_map[activation]
        
        self.is_discrete = (model_class in (LogisticHazard, PCHazard, MTLR, DeepHitSingle))
        out_features = num_durations if self.is_discrete else 1 

        self.net = MultiTaskSurvivalNet(
            input_size=input_size,
            num_nodes=num_nodes,
            out_features=out_features,
            num_tasks=num_tasks,
            activation=activation_fn,
            dropout=dropout,
            batch_norm=batch_norm,
            use_attention=use_attention,
            attn_kwargs=attn_kwargs,
            msi_index=msi_index,
            ras_index=ras_index,
        )
        
        self.task_baselines_: dict[int, dict[str, np.ndarray]] = {}  # {k: {'times': ..., 'cumhaz': ...}}
        self.optimizer = torch.optim.Adam(self.net.parameters(), lr=lr)
        self.model_class = model_class
        self.num_tasks = num_tasks
        self.num_durations = num_durations
        self.epochs = epochs
    
        if self.is_discrete:
            self.model = model_class(self.net, self.optimizer, duration_index=None)
        else:
            self.model = model_class(self.net, self.optimizer)
        self.loss_fn = self.model.loss
     
        
        self.kwargs = {
            'model_class': model_class,
            'input_size': input_size,
            'num_nodes': num_nodes,
            'num_tasks': num_tasks,
            'num_durations': num_durations,
            'dropout': dropout,
            'lr': lr,
            'batch_norm': batch_norm,
            'activation': activation,
            'epochs': epochs,
            'msi_index': msi_index,
            'ras_index': ras_index, 
            **kwargs
        }

    def fit(self, X: pd.DataFrame, task_idx: np.ndarray, y: pd.DataFrame, val_data=None):
   
        if isinstance(y, np.ndarray) and y.dtype.names:
            durations = y['survivalDaysSinceMetastaticDiagnosis'].astype('float32') if 'survivalDaysSinceMetastaticDiagnosis' in y.dtype.names else y['duration'].astype('float32')
            events = y['hadSurvivalEvent'].astype('float32') if 'hadSurvivalEvent' in y.dtype.names else y['event'].astype('float32')
        else:
            durations = y['duration'].astype('float32')
            events = y['event'].astype('float32')
            
        X_tensor = torch.from_numpy(X.values.astype('float32'))
        task_idx_tensor = torch.from_numpy(task_idx.astype('int64'))

        if isinstance(self.model, CoxPH):
            # ---- DeepSurv branch (continuous-time) ----
            y_dur_t = torch.from_numpy(durations.astype('float32'))
            y_evt_t = torch.from_numpy(events.astype('float32'))
            dataset = torch.utils.data.TensorDataset(X_tensor, task_idx_tensor, y_dur_t, y_evt_t)
            loader = torch.utils.data.DataLoader(dataset, batch_size=128, shuffle=True)

            self.net.train()
            for _ in range(self.epochs):
                for xb, tb, yb_dur, yb_evt in loader:
                    log_h = self.net(xb, tb).view(-1, 1)   # shape [B, 1]
                    loss = self.loss_fn(log_h, yb_dur, yb_evt)
                    self.optimizer.zero_grad()
                    loss.backward()
                    self.optimizer.step()
        else:
            # ---- Discrete-time branch (LH/PCH/MTLR/DeepHitSingle) ----
            labtrans = LogisticHazard.label_transform(self.num_durations)
            labtrans.cuts = config_settings.fixed_time_bins
            y_bins, y_evt = labtrans.fit_transform(durations, events)
            self.labtrans = labtrans

            y_bins_t = torch.from_numpy(y_bins).long()
            y_evt_t  = torch.from_numpy(y_evt).float()
            dataset = torch.utils.data.TensorDataset(X_tensor, task_idx_tensor, y_bins_t, y_evt_t)
            loader = torch.utils.data.DataLoader(dataset, batch_size=128, shuffle=True)

            self.net.train()
            for _ in range(self.epochs):
                for xb, tb, yb_bins, yb_evt in loader:
                    logits = self.net(xb, tb)             
                    loss = self.loss_fn(logits, yb_bins, yb_evt)
                    self.optimizer.zero_grad()
                    loss.backward()
                    self.optimizer.step()

            self.model.labtrans = labtrans 

    def predict_survival_function(self, X: pd.DataFrame, task_idx: np.ndarray, times: np.ndarray=None):
        self.net.eval()
        with torch.no_grad():
            X_tensor = torch.from_numpy(X.values.astype('float32'))
            task_idx_tensor = torch.from_numpy(task_idx.astype('int64'))
            
            if isinstance(self.model, CoxPH):
                grid = np.asarray(config_settings.fixed_time_bins if times is None else times, dtype=float)
                n = X.shape[0]
                S = np.ones((grid.size, n), dtype=float)
                etas = self.net(X_tensor, task_idx_tensor).squeeze(1).cpu().numpy()

                for k in range(self.num_tasks):
                    cols = np.where(task_idx == k)[0]
                    if cols.size == 0:
                        continue
                    base = self.task_baselines_.get(k, None)
                    if base is None or base['times'].size == 0:
                        continue

                    t0 = base['times']
                    H0 = base['cumhaz']
                    H0_grid = np.interp(grid, t0, H0, left=0.0, right=H0[-1])

                    exp_eta = np.exp(etas[cols])
                    S[:, cols] = np.exp(-np.outer(H0_grid, exp_eta))
                S = np.clip(S, 1e-10, 1.0)
                return pd.DataFrame(S, index=grid)
            
            logits = self.net(X_tensor, task_idx_tensor)
            hazards = torch.sigmoid(logits)
            surv = torch.cumprod(1 - hazards, dim=1).cpu().numpy().T
            time_grid = config_settings.fixed_time_bins
            surv_df = pd.DataFrame(surv, index=time_grid)
            if times is not None:
                surv_df = surv_df.reindex(times, method='nearest', fill_value='extrapolate')
            return surv_df

    def predict(self, X: pd.DataFrame, task_idx: np.ndarray) -> np.ndarray:
        self.net.eval()
        with torch.no_grad():
            X_tensor = torch.from_numpy(X.values.astype('float32'))
            task_idx_tensor = torch.from_numpy(task_idx.astype('int64'))
            if isinstance(self.model, CoxPH):
                log_risk = self.net(X_tensor, task_idx_tensor).squeeze(1).cpu().numpy()
                return log_risk
            surv_df = self.predict_survival_function(X, task_idx)
            last_t = surv_df.index[-1]
            surv_last = np.clip(surv_df.loc[last_t].values, 1e-10, 1.0)
            return -np.log(surv_last)
        

class FeatureAttention(nn.Module):
    def __init__(self, input_size: int):
        super().__init__()
        self.attn = nn.Sequential(
            nn.Linear(input_size, input_size),
            nn.Tanh(),
            nn.Linear(input_size, input_size),
            nn.Sigmoid()
        )

    def forward(self, x):
        weights = self.attn(x)
        return x * weights
