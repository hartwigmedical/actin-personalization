import pandas as pd
from predictor import load_patient_df, MALIGNANCY_ICD_CODES


class TestPredictor:
    EXAMPLE_TNM_MEDIANS = {
        "clinical": {
            "IV": {
                "clinicalTnmT": 3.0,
                "clinicalTnmN": 1.5,
                "clinicalTnmM": 1.25,
            }
        },
        "pathological": {
            "IV": {
                "pathologicalTnmT": 3.0,
                "pathologicalTnmN": 2.25,
                "pathologicalTnmM": 1.25,
            }
        }
    }

    @classmethod
    def example_patient(cls):
        return {
            "patient": {"gender": "MALE", "birthYear": 1965},
            "tumor": {
                "stage": "IV",
                "priorPrimaries": [{"status": "INACTIVE"}, {"status": "ACTIVE"}],
                "hasLiverLesions": True,
                "hasLymphNodeLesions": False,
                "hasLungLesions": False,
                "hasBrainLesions": False,
                "otherLesions": [],
            },
            "clinicalStatus": {"who": 2},
            "comorbidities": [
                {"icdCodes": [
                    {"mainCode": "I50", "extensionCode": None},
                    {"mainCode": "J44", "extensionCode": None},
                    {"mainCode": "K25", "extensionCode": None}
                ]},
                {"icdCodes": [{"mainCode": "E11.2", "extensionCode": None}]}
            ],
            "molecularHistory": {"molecularTests": [
                {"drivers": {"variants": [{"gene": "KRAS", "event": "G12C"}]},
                 "characteristics": {"microsatelliteStability": {"isUnstable": True}}}
            ]},
            "labValues": [
                {"measurement": "LACTATE_DEHYDROGENASE", "value": 300},
                {"measurement": "ALKALINE_PHOSPHATASE", "value": 100},
                {"measurement": "LEUKOCYTES_ABS", "value": 7.0},
                {"measurement": "CARCINOEMBRYONIC_ANTIGEN", "value": 15.5},
                {"measurement": "ALBUMIN", "value": 36.0},
                {"measurement": "NEUTROPHILS_ABS", "value": 4.0}
            ]
        }

    @classmethod
    def setup_class(cls):
        from utils.settings import Settings
        cls.settings = Settings(outcome="OS", save_models=False)

    def test_tnm_filling(self):
        df = load_patient_df(self.example_patient(), self.EXAMPLE_TNM_MEDIANS, self.settings)
        row = df.iloc[0]
        assert row['clinicalTnmT'] == 3.0
        assert row['clinicalTnmN'] == 1.5
        assert row['clinicalTnmM'] == 1.25
        assert row['pathologicalTnmT'] == 3.0
        assert row['pathologicalTnmN'] == 2.25
        assert row['pathologicalTnmM'] == 1.25

    def test_comorbidity_filling(self):
        df = load_patient_df(self.example_patient(), self.EXAMPLE_TNM_MEDIANS, self.settings)
        row = df.iloc[0]
        assert bool(row['hasCongestiveHeartFailure']) is True
        assert bool(row['hasCopd']) is True
        assert bool(row['hasUlcerDisease']) is True
        assert bool(row['hasDiabetesMellitusWithEndOrganDamage']) is True
        assert bool(row['hasDiabetesMellitus']) is True

        assert bool(row['hasKrasG12CMutation']) is True
        assert bool(row['hasRasMutation']) is True
        assert bool(row['hasMsi']) is True

        assert row['sex'] == "MALE"
        assert row['ageAtMetastaticDiagnosis'] == 2025 - 1965
        assert row['numberOfPriorTumors'] == 2
        assert bool(row['hasDoublePrimaryTumor']) is True

        assert row['lactateDehydrogenaseAtMetastaticDiagnosis'] == 300
        assert row['alkalinePhosphataseAtMetastaticDiagnosis'] == 100
        assert row['leukocytesAbsoluteAtMetastaticDiagnosis'] == 7.0
        assert row['albumineAtMetastaticDiagnosis'] == 36.0
        assert row['neutrophilsAbsoluteAtMetastaticDiagnosis'] == 4.0

        assert row['whoAssessmentAtMetastaticDiagnosis'] == 2

        assert bool(row['hasLiverOrIntrahepaticBileDuctMetastases']) is True
        assert bool(row['hasLymphNodeMetastases']) is False
        assert bool(row['hasBronchusOrLungMetastases']) is False
        assert bool(row['hasOtherMetastases']) is False

    def test_other_malignancy_flag(self):
        patient = self.example_patient().copy()
        patient["comorbidities"] = [{"icdCodes": [{"mainCode": "Z99", "extensionCode": None}]}]
        df = load_patient_df(patient, self.EXAMPLE_TNM_MEDIANS, self.settings)
        assert bool(df.iloc[0]['hasOtherMalignancy']) is True

    def test_icd_startswith_matching(self):
        patient = self.example_patient().copy()
        patient["comorbidities"] = [{"icdCodes": [{"mainCode": "I50999", "extensionCode": None}, {"mainCode": "X999", "extensionCode": None}]}]
        df = load_patient_df(patient, self.EXAMPLE_TNM_MEDIANS, self.settings)
        assert bool(df.iloc[0]['hasCongestiveHeartFailure']) is True

    def test_mutation_flags_negative(self):
        patient = self.example_patient().copy()
        patient['molecularHistory'] = {"molecularTests": []}
        df = load_patient_df(patient, self.EXAMPLE_TNM_MEDIANS, self.settings)
        assert not bool(df.iloc[0]['hasKrasG12CMutation'])
        assert not bool(df.iloc[0]['hasRasMutation'])
        assert not bool(df.iloc[0]['hasMsi'])

    def test_empty_patient_fills_none(self):
        empty_patient = {}
        df = load_patient_df(empty_patient, self.EXAMPLE_TNM_MEDIANS, self.settings)
        assert df.iloc[0]['ageAtMetastaticDiagnosis'] == 0
        assert pd.isna(df.iloc[0]['clinicalTnmT'])
        assert pd.isna(df.iloc[0]['sex'])
