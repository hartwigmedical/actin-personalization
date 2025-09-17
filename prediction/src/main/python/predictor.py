import importlib
import json
import logging
import os
import pandas as pd
import numpy as np
import torch
import shap
import re

from data.data_processing import DataPreprocessor
from data.lookups import lookup_manager
from models import *
from models.patients_like_me import PatientsLikeMeModel
from utils.settings import Settings
from utils.feature_translation import feature_short_names

logger = logging.getLogger(__name__)

MALIGNANCY_ICD_CODES = {
    "hasAids": ["B24"],
    "hasCongestiveHeartFailure": ["I50"],
    "hasCollagenosis": ["M35.9", "M35.8", "L87.1"],
    "hasCopd": ["J40", "J42", "J43" , "J44"],
    "hasCerebrovascularDisease": ["I60", "I61", "I62", "I63", "I64", "I65", "I66", "I67", "I68", "I69"],
    "hasDementia": ["F00", "F01", "F02", "F03"],
    "hasDiabetesMellitus": ["E10", "E11", "E12", "E13", "E14"],
    "hasDiabetesMellitusWithEndOrganDamage": ["E10.2", "E11.2", "E12.2", "E13.2", "E14.2"],
    "hasOtherMetastaticSolidTumor": ["C76", "C77", "C78", "C79", "C80"],
    "hasMyocardialInfarct":["I21"],
    "hasMildLiverDisease": ["K70"],
    "hasHemiplegiaOrParaplegia": ["G81", "G82"],
    "hasPeripheralVascularDisease": ["I73"],
    "hasRenalDisease": ["N10", "N11", "N12", "N13", "N14", "N15", "N16", "N17", "N18", "N19"],
    "hasLiverDisease": [ "K71", "K72", "K73", "K74", "K75", "K76"],
    "hasUlcerDisease": ["K25", "K26", "K27", "K28"]
}

ALL_SPECIFIED_ICD_CODES = set(code for codes in MALIGNANCY_ICD_CODES.values() for code in codes)

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
    state = torch.load(state_path, map_location=torch.device("cpu"), weights_only=False)

    model.model.net.load_state_dict(state["net_state"])

    if "labtrans" in state:
        model.labtrans = state["labtrans"]
        model.model.duration_index = model.labtrans.cuts

    if "baseline_hazards" in state:
        model.model.baseline_hazards_ = state["baseline_hazards"]
        model.model.baseline_cumulative_hazards_ = state["baseline_cumulative_hazards"]

    model.model.net.eval()

    return model


def load_patient_df(patient, tnm_stage_medians, settings: Settings) -> pd.DataFrame:
    tumor = patient.get("tumor", {})
    clinical_status = patient.get("clinicalStatus", {})
    comorbidities = patient.get("comorbidities", [])
    molecular_tests = patient.get("molecularHistory", {}).get("molecularTests", [])
    lab_values = {lab["measurement"]: lab for lab in patient.get("labValues", [])}
    birth_year = patient.get("patient", {}).get("birthYear")
    stage = tumor.get("stage")

    tnm_values = get_tnm_values(tnm_stage_medians, stage)
    comorbidity_dict = get_comorbidity_dict(comorbidities)

    has_other_malignancy = any(
        icd.get("mainCode", "") not in ALL_SPECIFIED_ICD_CODES
        for c in comorbidities
        for icd in c.get("icdCodes", [])
    )
    variant_genes = {v.get("gene"): v for test in molecular_tests for v in test.get("drivers", {}).get("variants", [])}
    has_msi = any(
        (test.get("characteristics", {}).get("microsatelliteStability") or {}).get("isUnstable", False)
        for test in molecular_tests
    )
    
    patient_dict = {
        "sex": patient.get("patient", {}).get("gender"),
        "ageAtMetastaticDiagnosis": 2025 - birth_year if birth_year else 0,
        "numberOfPriorTumors": len(tumor.get("priorPrimaries", [])),
        "hasDoublePrimaryTumor": any(p.get("status") == "ACTIVE" for p in tumor.get("priorPrimaries", [])),
        **tnm_values,
        "clinicalTumorStage": stage,
        "pathologicalTumorStage": stage,
        "hasLiverOrIntrahepaticBileDuctMetastases": tumor.get("hasLiverLesions"),
        "hasLymphNodeMetastases": tumor.get("hasLymphNodeLesions"),
        "hasBronchusOrLungMetastases": tumor.get("hasLungLesions"),
        "hasBrainMetastases": tumor.get("hasBrainLesions"),
        "hasOtherMetastases": bool(tumor.get("otherLesions")),
        "whoAssessmentAtMetastaticDiagnosis": clinical_status.get("who"),
        "lactateDehydrogenaseAtMetastaticDiagnosis": lab_values.get("LACTATE_DEHYDROGENASE", {}).get("value"),
        "alkalinePhosphataseAtMetastaticDiagnosis": lab_values.get("ALKALINE_PHOSPHATASE", {}).get("value"),
        "leukocytesAbsoluteAtMetastaticDiagnosis": lab_values.get("LEUKOCYTES_ABS", {}).get("value"),
        "carcinoembryonicAntigenAtMetastaticDiagnosis": lab_values.get("CARCINOEMBRYONIC_ANTIGEN", {}).get("value"),
        "albumineAtMetastaticDiagnosis": lab_values.get("ALBUMIN", {}).get("value"),
        "neutrophilsAbsoluteAtMetastaticDiagnosis": lab_values.get("NEUTROPHILS_ABS", {}).get("value"),
        **comorbidity_dict,
        "hasOtherMalignancy": has_other_malignancy,
        "hasMsi": has_msi,
        "hasBrafMutation": "BRAF" in variant_genes,
        "hasBrafV600EMutation": "BRAF" in variant_genes and "V600E" in variant_genes["BRAF"].get("event", ""),
        "hasRasMutation": any(gene in variant_genes for gene in ["KRAS", "NRAS", "HRAS"]),
        "hasKrasG12CMutation": "KRAS" in variant_genes and "G12C" in variant_genes["KRAS"].get("event", ""),
    }
    
    features = lookup_manager.features + [settings.event_col, settings.duration_col]
    patient_dict = {key: patient_dict.get(key, None) for key in features}
    
    return pd.DataFrame([patient_dict])

def build_shap_explainer(model, shap_samples_path) -> shap.Explainer:
    X_samples = pd.read_csv(shap_samples_path)
    X_samples = X_samples.astype(np.float64).fillna(0.0)
    logger.info(f"Loaded SHAP samples from {shap_samples_path} with shape {X_samples.shape}")

    explainer = shap.Explainer(model.predict, X_samples)
    logger.info(f"Created SHAP explainer with {X_samples.shape[0]} samples")

    return explainer

def get_shap_values(explainer: shap.Explainer, X: pd.DataFrame):

    X = X.astype(np.float64).fillna(0.0)

    shap_values = explainer(X)
    logger.info(f"Computed SHAP values with shape {shap_values.shape}")
    shap.plots.bar(shap_values[0])

    result_dict = {
        feature_short_names.get(shap_values.feature_names[j], shap_values.feature_names[j]): {
            "featureValue": shap_values.data[0][j],
            "shapValue": shap_values.values[0][j]
        }
        for j in range(len(shap_values.feature_names))
    }
    return result_dict


def convert_patient_dict_to_processed_df(patient_data: dict, trained_path, settings: Settings):
    with open(f"{trained_path}/tnm_stage_medians.json", "r") as f:
        tnm_stage_medians = json.load(f)
    patient_df = load_patient_df(patient_data, tnm_stage_medians, settings)

    preprocessor = DataPreprocessor(settings, fit=False, preprocessor_path=False)
    processed_df, updated_features, _ = preprocessor.preprocess_data(df=patient_df)

    return processed_df

def get_patient_like_me(patient_data: dict, trained_path, settings: Settings):
    model = PatientsLikeMeModel(settings)
    processed_df = convert_patient_dict_to_processed_df(patient_data, trained_path, settings)
    treatment_distribution_df = model.find_similar_patients(processed_df)

    def process_treatment_distribution(d: dict):
        return [{"treatment": k, "proportion": v} for k, v in d.items()]
    return {
        "overallTreatmentProportion": process_treatment_distribution(treatment_distribution_df["overallTreatmentProportion"].to_dict()),
        "similarPatientsTreatmentProportion": process_treatment_distribution(treatment_distribution_df["similarPatientsTreatmentProportion"].to_dict())
    }


def predict_treatment_scenarios(patient_data: dict, trained_path: str, shap_samples_path: str, valid_treatment_combinations: dict, settings: Settings) -> list:
    
    processed_df = convert_patient_dict_to_processed_df(patient_data, trained_path, settings)

    model = load_model(trained_path)

    X_base = processed_df.drop(columns=[c for c in [settings.event_col, settings.duration_col, "sourceId"] if c in processed_df.columns], errors="ignore")

    treatment_cols = [c for c in X_base.columns if c.startswith("systemicTreatmentPlan")]

    shap_explainer = build_shap_explainer(model, shap_samples_path)

    survival_prediction = []
    for idx, (label, mapping) in enumerate(valid_treatment_combinations.items()):
        for col, val in mapping.items():
            if col in X_base.columns:
                X_base[col] = val

        X_base["hasTreatment"] = (X_base[treatment_cols].sum(axis=1) > 0).astype(int)

        surv_fns = model.predict_survival_function(X_base)

        sf = surv_fns[0]

        survival_prediction.append({
            "treatment": label,
            "survivalProbs": sf.y.astype(float).tolist(),
            "shapValues": get_shap_values(shap_explainer, X_base)
        })

    return survival_prediction


def get_stage_median(tnm_stage_medians, stage_type: str, stage: str, key: str):
    stage_data = tnm_stage_medians.get(stage_type, {})
    norm_stage = str(stage).strip().upper()
    norm_stage_data = {str(s).strip().upper(): v for s, v in stage_data.items()}
    
    if norm_stage in norm_stage_data and key in norm_stage_data[norm_stage]:
        return norm_stage_data[norm_stage][key]
    
    pattern = re.compile(rf"^{re.escape(norm_stage)}[ABC]$")
    matching_substages = [
        v[key]
        for s, v in norm_stage_data.items()
        if isinstance(v, dict) and pattern.match(s) and key in v and v[key] is not None
    ]
    
    if matching_substages:
        return float(np.nanmean(matching_substages))
    
    return np.nan

def get_tnm_values(tnm_stage_medians, stage):
    tnm_values = {}
    for tnm in ["T", "N", "M"]:
        clinical_key = f"clinicalTnm{tnm}"
        pathological_key = f"pathologicalTnm{tnm}"
        tnm_values[clinical_key] = get_stage_median(tnm_stage_medians, "clinical", stage, clinical_key)
        tnm_values[pathological_key] = get_stage_median(tnm_stage_medians, "pathological", stage, pathological_key)
        
    return tnm_values

def get_comorbidity_dict(comorbidities):
    def has_icd(feature_name):
        icd_codes = MALIGNANCY_ICD_CODES.get(feature_name, [])
        for c in comorbidities:
            for icd in c.get("icdCodes", []):
                if any(icd.get("mainCode", "").startswith(code) for code in icd_codes):
                    return True
        return False
    
    keys = [
        "hasAids", "hasCongestiveHeartFailure", "hasCollagenosis", "hasCopd", "hasCerebrovascularDisease", "hasDementia",
        "hasDiabetesMellitus", "hasDiabetesMellitusWithEndOrganDamage", "hasOtherMetastaticSolidTumor", "hasMyocardialInfarct",
        "hasMildLiverDisease", "hasHemiplegiaOrParaplegia", "hasPeripheralVascularDisease", "hasRenalDisease",
        "hasLiverDisease", "hasUlcerDisease"
    ]
    
    return {k: has_icd(k) for k in keys}
