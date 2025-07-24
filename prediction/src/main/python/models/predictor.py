import importlib
import json
import os
import pandas as pd
import numpy as np
import torch
import re


from data.data_processing import DataPreprocessor
from data.lookups import lookup_manager
from models import *
from utils.settings import Settings

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

    def has_icd(feature_name) -> bool:
        icd_codes = MALIGNANCY_ICD_CODES.get(feature_name, [])
        for c in comorbidities:
            for icd in c.get("icdCodes", []):
                if any(icd.startswith(code) for code in icd_codes):
                    return True
        return False
    
   
    def get_stage_median(stage_type: str, stage: str, key: str):
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


    birth_year = patient.get("patient", {}).get("birthYear")
    
    stage = tumor.get("stage")
    tnm_values = {}
    for tnm in ["T", "N", "M"]:
        clinical_key = f"clinicalTnm{tnm}"
        pathological_key = f"pathologicalTnm{tnm}"

        tnm_values[f"clinicalTnm{tnm}"] = get_stage_median("clinical", stage, clinical_key)
        tnm_values[f"pathologicalTnm{tnm}"] = get_stage_median("pathological", stage, pathological_key)
        
    has_other_malignancy = any(
            icd not in ALL_SPECIFIED_ICD_CODES
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

        # "primaryTumorType": not available (no specific doids for CRC variations),
        # "primaryTumorTypeLocation": not available,
        # "sidedness": not available,

        # "anorectalVergeDistanceCategory": not available,
        # "mesorectalFasciaIsClear": not available
        # "distanceToMesorectalFasciaMm": not available,

        # "differentiationGrade": not available,

        **tnm_values,
        "clinicalTumorStage": stage,
        "pathologicalTumorStage": stage,

        # "investigatedLymphNodesCountPrimaryDiagnosis": not available,
        # "positiveLymphNodesCountPrimaryDiagnosis": not available
        # "presentedWithIleus": not available,
        # "presentedWithPerforation": not available,
        # "extraMuralInvasionCategory": not available,
        
        # "daysBetweenPrimaryAndMetastaticDiagnosis": not available,
        "hasLiverOrIntrahepaticBileDuctMetastases": tumor.get("hasLiverLesions"),
        # "numberOfLiverMetastases": not available,
        # "maximumSizeOfLiverMetastasisMm": not available,
        "hasLymphNodeMetastases": tumor.get("hasLymphNodeLesions"),
        # "investigatedLymphNodesCountMetastaticDiagnosis": not available,
        # "positiveLymphNodesCountMetastaticDiagnosis": not available,
        # "hasPeritonealMetastases": not available,
        "hasBronchusOrLungMetastases": tumor.get("hasLungLesions"),
        "hasBrainMetastases": tumor.get("hasBrainLesions"),
        "hasOtherMetastases": bool(tumor.get("otherLesions")),

        "whoAssessmentAtMetastaticDiagnosis": clinical_status.get("who"),
        # "asaAssessmentAtMetastaticDiagnosis": not available,
        "lactateDehydrogenaseAtMetastaticDiagnosis": lab_values.get("LACTATE_DEHYDROGENASE", {}).get("value"),
        "alkalinePhosphataseAtMetastaticDiagnosis": lab_values.get("ALKALINE_PHOSPHATASE", {}).get("value"),
        "leukocytesAbsoluteAtMetastaticDiagnosis": lab_values.get("LEUKOCYTES_ABS", {}).get("value"),
        "carcinoembryonicAntigenAtMetastaticDiagnosis": lab_values.get("CARCINOEMBRYONIC_ANTIGEN", {}).get("value"),
        "albumineAtMetastaticDiagnosis": lab_values.get("ALBUMIN", {}).get("value"),
        "neutrophilsAbsoluteAtMetastaticDiagnosis": lab_values.get("NEUTROPHILS_ABS", {}).get("value"),

        # "hasHadPrimarySurgeryPriorToMetastaticTreatment": not available,
        # "hasHadPrimarySurgeryDuringMetastaticTreatment": not available,
        # "hasHadGastroenterologySurgeryPriorToMetastaticTreatment": not available,
        # "hasHadGastroenterologySurgeryDuringMetastaticTreatment":  not available,
        # "hasHadHipecPriorToMetastaticTreatment": not available,
        # "hasHadHipecDuringMetastaticTreatment":  not available,
        # "hasHadPrimaryRadiotherapyPriorToMetastaticTreatment":  not available,
        # "hasHadPrimaryRadiotherapyDuringMetastaticTreatment":  not available,
        # "hasHadMetastaticSurgery": : not available,
        # "hasHadMetastaticRadiotherapy": not available,

        # "charlsonComorbidityIndex": not available,
        "hasAids": has_icd("hasAids"),
        "hasCongestiveHeartFailure": has_icd("hasCongestiveHeartFailure"),
        "hasCollagenosis": has_icd("hasCollagenosis"),
        "hasCopd": has_icd("hasCopd"),
        "hasCerebrovascularDisease": has_icd("hasCerebrovascularDisease"),
        "hasDementia": has_icd("hasDementia"),
        "hasDiabetesMellitus": has_icd("hasDiabetesMellitus"),
        "hasDiabetesMellitusWithEndOrganDamage": has_icd("hasDiabetesMellitusWithEndOrganDamage"),
        "hasOtherMetastaticSolidTumor": has_icd("hasOtherMetastaticSolidTumor"),
        
        "hasOtherMalignancy": has_other_malignancy,
        "hasMyocardialInfarct": has_icd("hasMyocardialInfarct"),
        "hasMildLiverDisease": has_icd("hasMildLiverDisease"),
        "hasHemiplegiaOrParaplegia": has_icd("hasHemiplegiaOrParaplegia"),
        "hasPeripheralVascularDisease": has_icd("hasPeripheralVascularDisease"),
        "hasRenalDisease": has_icd("hasRenalDisease"),
        "hasLiverDisease": has_icd("hasLiverDisease"),
        "hasUlcerDisease": has_icd("hasUlcerDisease"),

        "hasMsi": has_msi,
        "hasBrafMutation": "BRAF" in variant_genes,
        "hasBrafV600EMutation": "BRAF" in variant_genes and "V600E" in variant_genes["BRAF"].get("event", ""),
        "hasRasMutation": any(gene in variant_genes for gene in ["KRAS", "NRAS", "HRAS"]),
        "hasKrasG12CMutation": "KRAS" in variant_genes and "G12C" in variant_genes["KRAS"].get("event", ""),
    }
    
    features = lookup_manager.features + [settings.event_col, settings.duration_col]

    patient_dict = {key: patient_dict.get(key, None) for key in features}

    return pd.DataFrame([patient_dict])


def predict_treatment_scenarios(patient_data: dict, trained_path: str, valid_treatment_combinations: dict, settings: Settings) -> dict:
    
    with open(f"{trained_path}/tnm_stage_medians.json", "r") as f:
        tnm_stage_medians = json.load(f)
        
    patient_df = load_patient_df(patient_data, tnm_stage_medians, settings)

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
