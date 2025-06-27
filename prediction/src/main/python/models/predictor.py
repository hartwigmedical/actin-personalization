import torch
import pandas as pd
import os
import json
import importlib
import dill
import joblib

from data.data_processing import DataPreprocessor
from utils.settings import Settings
from data.lookups import lookup_manager
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

def load_patient_df(patient, settings: Settings) -> pd.DataFrame:
    tumor = patient.get("tumor", {})
    clinical_status = patient.get("clinicalStatus", {})
    comorbidities = patient.get("comorbidities", [])
    molecular_tests = patient.get("molecularHistory", {}).get("molecularTests", [])
    lab_values = {lab["measurement"]: lab for lab in patient.get("labValues", [])}

    def has_icd(icd_codes: list[str]) -> bool:
        for c in comorbidities:
            for icd in c.get("icdCodes", []):
                if any(icd.startswith(code) for code in icd_codes):
                    return True
        return False


    variant_genes = {v.get("gene"): v for test in molecular_tests for v in test.get("drivers", {}).get("variants", [])}
    birth_year = patient.get("patient", {}).get("birthYear")

    patient_dict = {
        "sex": patient.get("patient", {}).get("gender"),
        "ageAtMetastaticDiagnosis": 2025 - birth_year if birth_year else 0,
        "numberOfPriorTumors": len(tumor.get("priorPrimaries", [])),
        "hasDoublePrimaryTumor": any(p.get("status") == "ACTIVE" for p in tumor.get("priorPrimaries", [])),

        # "primaryTumorType": #TODO #see doids.json (but: no specific code for specific CRC variations?)
        # "primaryTumorTypeLocation": , #TODO
        # "sidedness": , #TODO

        # "anorectalVergeDistanceCategory": #TODO,
        # "mesorectalFasciaIsClear": #TODO
        # "distanceToMesorectalFasciaMm": #TODO,

        # "differentiationGrade": #TODO,
        # "clinicalTnmT": #TODO,
        # "clinicalTnmN": #TODO
        # "clinicalTnmM": #TODO,
        # "pathologicalTnmT": #TODO,
        # "pathologicalTnmN": #TODO,
        # "pathologicalTnmM": #TODO,
        "clinicalTumorStage": tumor.get("stage"),
        "pathologicalTumorStage": tumor.get("stage"),

        # "investigatedLymphNodesCountPrimaryDiagnosis": #TODO,
        # "positiveLymphNodesCountPrimaryDiagnosis": #TODO
        # "presentedWithIleus": #TODO,
        # "presentedWithPerforation": #TODO,
        # "extraMuralInvasionCategory": #TODO,
        # "tumorRegression": #TODO
        # "daysBetweenPrimaryAndMetastaticDiagnosis": #TODO,
        "hasLiverOrIntrahepaticBileDuctMetastases": tumor.get("hasLiverLesions"),
        # "numberOfLiverMetastases": #TODO,
        # "maximumSizeOfLiverMetastasisMm": #TODO,
        "hasLymphNodeMetastases": tumor.get("hasLymphNodeLesions"),
        # "investigatedLymphNodesCountMetastaticDiagnosis": #TODO,
        # "positiveLymphNodesCountMetastaticDiagnosis": #TODO,
        # "hasPeritonealMetastases": #TODO,
        "hasBronchusOrLungMetastases": tumor.get("hasLungLesions"),
        "hasBrainMetastases": tumor.get("hasBrainLesions"),
        "hasOtherMetastases": bool(tumor.get("otherLesions")),

        "whoAssessmentAtMetastaticDiagnosis": clinical_status.get("who"),
        # "asaAssessmentAtMetastaticDiagnosis": #TODO,
        "lactateDehydrogenaseAtMetastaticDiagnosis": lab_values.get("LACTATE_DEHYDROGENASE", {}).get("value"),
        "alkalinePhosphataseAtMetastaticDiagnosis": lab_values.get("ALKALINE_PHOSPHATASE", {}).get("value"),
        "leukocytesAbsoluteAtMetastaticDiagnosis": lab_values.get("LEUKOCYTES_ABS", {}).get("value"),
        "carcinoembryonicAntigenAtMetastaticDiagnosis": lab_values.get("CARCINOEMBRYONIC_ANTIGEN", {}).get("value"),
        "albumineAtMetastaticDiagnosis": lab_values.get("ALBUMIN", {}).get("value"),
        "neutrophilsAbsoluteAtMetastaticDiagnosis": lab_values.get("NEUTROPHILS_ABS", {}).get("value"),

        # "hasHadPrimarySurgeryPriorToMetastaticTreatment": #TODO,
        # "hasHadPrimarySurgeryDuringMetastaticTreatment": #TODO,
        # "hasHadGastroenterologySurgeryPriorToMetastaticTreatment": #TODO,
        # "hasHadGastroenterologySurgeryDuringMetastaticTreatment": #TODO,
        # "hasHadHipecPriorToMetastaticTreatment": #TODO,
        # "hasHadHipecDuringMetastaticTreatment": #TODO,
        # "hasHadPrimaryRadiotherapyPriorToMetastaticTreatment": #TODO,
        # "hasHadPrimaryRadiotherapyDuringMetastaticTreatment": #TODO,
        # "hasHadMetastaticSurgery": #TODO,
        # "hasHadMetastaticRadiotherapy": #TODO,

        # "charlsonComorbidityIndex": #TODO,
        "hasAids": has_icd(["B24"]),
        "hasCongestiveHeartFailure": has_icd(["I50"]),
        "hasCollagenosis": has_icd(["M35.9", "M35.8", "L87.1"]),
        "hasCopd": has_icd(["J40", "J42", "J43" , "J44"]),
        "hasCerebrovascularDisease": has_icd(["I60", "I61", "I62", "I63", "I64", "I65", "I66", "I67", "I68", "I69"]),
        "hasDementia": has_icd(["F00", "F01", "F02", "F03"]),
        "hasDiabetesMellitus": has_icd(["E10", "E11", "E12", "E13", "E14"]),
        "hasDiabetesMellitusWithEndOrganDamage": has_icd(["E10.2", "E11.2", "E12.2", "E13.2", "E14.2"]),
        # "hasOtherMalignancy": ,
        # "hasOtherMetastaticSolidTumor":  ,
        "hasMyocardialInfarct": has_icd(["I21"]),
        "hasMildLiverDisease": has_icd(["K70"]),
        "hasHemiplegiaOrParaplegia": has_icd(["G81", "G82"]),
        "hasPeripheralVascularDisease": has_icd(["I73"]),
        "hasRenalDisease": has_icd(["N10", "N11", "N12", "N13", "N14", "N15", "N16", "N17", "N18", "N19"]),
        "hasLiverDisease": has_icd([ "K71", "K72", "K73", "K74", "K75", "K76"]),
        "hasUlcerDisease": has_icd(["K25", "K26", "K27", "K28"]),

        "hasMsi": any(
            test.get("characteristics", {}).get("microsatelliteStability", {}).get("isUnstable", False)
            for test in molecular_tests
        ),
        "hasBrafMutation": "BRAF" in variant_genes,
        "hasBrafV600EMutation": "BRAF" in variant_genes and "V600E" in variant_genes["BRAF"].get("event", ""),
        "hasRasMutation": any(gene in variant_genes for gene in ["KRAS", "NRAS", "HRAS"]),
        "hasKrasG12CMutation": "KRAS" in variant_genes and "G12C" in variant_genes["KRAS"].get("event", ""),


    }
    features = lookup_manager.features + [settings.event_col, settings.duration_col]

    patient_dict = {key: patient_dict.get(key, None) for key in features}

    return pd.DataFrame([patient_dict])


def predict_treatment_scenarios(patient_data: dict, trained_path: str, valid_treatment_combinations: dict, settings: Settings) -> dict:

    patient_df = load_patient_df(patient_data, settings)

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
