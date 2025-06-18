import torch
import pandas as pd
import os
import json
import importlib
import dill

from data.data_processing import DataPreprocessor
from utils.settings import Settings
from models import *

def load_model(trained_path: str) -> any:

    config_path = os.path.join(trained_path, "model_config.json")
    with open(config_path, "r") as f:
        model_config = json.load(f)

    class_path = model_config["class"] 
    kwargs = model_config["kwargs"]

    module_name, class_name = class_path.rsplit(".", 1)
    module = importlib.import_module(module_name)
    model_class = getattr(module, class_name)
    model = model_class(**kwargs)

    state_path = os.path.join(trained_path, "model.pt")
    state = torch.load(state_path, map_location=torch.device("cpu"))

    model.model.net.load_state_dict(state["net_state"])

    if "labtrans" in state:
        model.labtrans = state["labtrans"]
        model.model.duration_index = model.labtrans.cuts

    if "baseline_hazards" in state:
        model.model.baseline_hazards_ = state["baseline_hazards"]
        model.model.baseline_cumulative_hazards_ = state["baseline_cumulative_hazards"]

    model.model.net.eval()

    return model

def predict_treatment_scenarios(patient_data: dict, trained_path: str, valid_treatment_combinations: dict, settings=Settings) -> dict:
    
    patient_df = pd.DataFrame([patient_data])

    preprocessor = DataPreprocessor(settings, fit=False, preprocessor_path=False)
    processed_df, updated_features, _ = preprocessor.preprocess_data(df=patient_df)
    
    model = load_model(trained_path)
 
    X_base = processed_df.drop(columns=[c for c in [settings.event_col, settings.duration_col, "sourceId"] if c in processed_df.columns], errors="ignore")

    treatment_cols = [c for c in X_base.columns if c.startswith("systemicTreatmentPlan")]

    survival_dict = {}
    for idx, (label, mapping) in enumerate(valid_treatment_combinations.items()):
        for col, val in mapping.items():
            if col in X_base.columns:
                X_base[col] = val

        X_base["hasTreatment"] = (X_base[treatment_cols].sum(axis=1) > 0).astype(int)
    
        surv_fns = model.predict_survival_function(X_base)
        sf = surv_fns[0] 
        
        survival_dict[label] = {
            "survival_probs": sf.y.astype(float).tolist()
        }

    return survival_dict
