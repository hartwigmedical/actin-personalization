import pandas as pd
import torch
from data.data_processing import DataPreprocessor
from models import *
from utils.settings import config_settings


def load_model(trained_path: str, model_type: str = "DeepSurv_attention", settings=config_settings):
    config_mgr = ExperimentConfig("./prediction/src/main/python/models/configs/model_hyperparams.json")
    loaded_configs = config_mgr.load_model_configs()
    
    model_class, model_kwargs = loaded_configs[model_type]
    
    model = model_class(**model_kwargs)

    state = torch.load(f"{trained_path}/{settings.outcome}_{model_type}.pt", map_location=torch.device("cpu"))
    model.model.net.load_state_dict(state["net_state"])

    if "labtrans" in state:
        model.labtrans = state["labtrans"]
        model.model.duration_index = model.labtrans.cuts

    if "baseline_hazards" in state:
        model.model.baseline_hazards_ = state["baseline_hazards"]
        model.model.baseline_cumulative_hazards_ = state["baseline_cumulative_hazards"]

    model.model.net.eval()
    
    return model 

def predict_treatment_scenarios(patient_data: dict, trained_path: str, valid_treatment_combinations: dict, settings =config_settings) -> dict:
    
    patient_df = pd.DataFrame([patient_data])

    preprocessor = DataPreprocessor(settings, fit=False)
    processed_df, updated_features, _ = preprocessor.preprocess_data(df=patient_df)
    
    model = load_model(trained_path)
 
    X_base = processed_df.drop(columns=[c for c in [settings.event_col, settings.duration_col, "sourceId"] if c in processed_df.columns], errors="ignore")

    treatment_cols = [c for c in X_base.columns if c.startswith("systemicTreatmentPlan")]

    time_grid = None
    survival_dict = {}

    for idx, (label, mapping) in enumerate(valid_treatment_combinations.items()):
        for col, val in mapping.items():
            if col in X_base.columns:
                X_base[col] = val

        X_base["hasTreatment"] = (X_base[treatment_cols].sum(axis=1) > 0).astype(int)
    
        surv_fns = model.predict_survival_function(X_base)
        sf = surv_fns[0] 
        
        survival_dict[label] = {
            "time_grid": sf.x.astype(float).tolist(),
            "survival_probs": sf.y.astype(float).tolist()
        }

    return survival_dict
