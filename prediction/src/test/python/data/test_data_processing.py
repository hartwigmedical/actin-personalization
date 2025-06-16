import pytest
import pandas as pd
import numpy as np
import json
import os

from data.data_processing import DataPreprocessor
from utils.settings import Settings

test_settings = Settings(outcome="OS", save_models=False)

@pytest.fixture(scope="function")
def dp() -> DataPreprocessor:
    return DataPreprocessor(settings=test_settings, fit=False)

class TestDataPreprocessor:
    @pytest.mark.parametrize("treatment,expected", [
        ("FOLFOX", {"systemicTreatmentPlan_5-FU": 1, "systemicTreatmentPlan_oxaliplatin": 1, "systemicTreatmentPlan_irinotecan": 0, "systemicTreatmentPlan_bevacizumab": 0, "systemicTreatmentPlan_panitumumab": 0, "systemicTreatmentPlan_pembrolizumab": 0, "systemicTreatmentPlan_nivolumab": 0}),
        ("FOLFIRI-B", {"systemicTreatmentPlan_5-FU": 1, "systemicTreatmentPlan_oxaliplatin": 0, "systemicTreatmentPlan_irinotecan": 1, "systemicTreatmentPlan_bevacizumab": 1, "systemicTreatmentPlan_panitumumab": 0, "systemicTreatmentPlan_pembrolizumab": 0, "systemicTreatmentPlan_nivolumab": 0}),
        ("", {"systemicTreatmentPlan_5-FU": 0, "systemicTreatmentPlan_oxaliplatin": 0, "systemicTreatmentPlan_irinotecan": 0, "systemicTreatmentPlan_bevacizumab": 0, "systemicTreatmentPlan_panitumumab": 0, "systemicTreatmentPlan_pembrolizumab": 0, "systemicTreatmentPlan_nivolumab": 0}),
    ])

    def test_parse_treatment(self, dp, treatment, expected):
        assert dp.parse_treatment(treatment) == expected

    def test_group_treatments(self, dp: DataPreprocessor):
        df = pd.DataFrame({
            "firstSystemicTreatmentAfterMetastaticDiagnosis": ["FOLFOX", "", None]
        })
        result = dp.group_treatments(df)
        assert "treatment" in result.columns
        assert result["treatment"].tolist() == [1, 0, 0]

    def test_handle_missing_values(self):
        dp = DataPreprocessor(settings=test_settings, fit=True)
        df = pd.DataFrame({
            "ageAtMetastaticDiagnosis": [65, 70, np.nan, 60],
            test_settings.duration_col: [100, 200, 300, 400],
            test_settings.event_col: [1, 0, 1, 1]
        })
        result = dp.handle_missing_values(df)
        assert not result["ageAtMetastaticDiagnosis"].isnull().any()
        assert "ageAtMetastaticDiagnosis" in dp.medians

    def test_knn_imputation_fills_na(self, dp):
        df = pd.DataFrame({"whoAssessmentAtMetastaticDiagnosis": [1.0, None, 3.0, None]})
        df = dp.impute_knn(df, ["whoAssessmentAtMetastaticDiagnosis"], k=2)
        assert not df["whoAssessmentAtMetastaticDiagnosis"].isnull().any()

    def test_encode_categorical_creates_dummies(self, dp):
        df = pd.DataFrame({
            "primaryTumorLocation": ["RECTUM", "SIGMOID_COLON", "RECTUM", "COECUM"],
            test_settings.duration_col: [120, 300, 250, 190],
            test_settings.event_col: [1, 0, 1, 1]
        })
        result = dp.encode_categorical(df)
        assert any(col.startswith("primaryTumorLocation_") for col in result.columns)

    def test_auto_cast_object_columns(self, dp: DataPreprocessor):
        df = pd.DataFrame({
            "hasDiabetesMellitus": ["1", "0", "1"],
            "lactateDehydrogenaseAtMetastaticDiagnosis": ["180.0", "200.0", "190.0"]
        })
        result = dp.auto_cast_object_columns(df)
        assert pd.api.types.is_numeric_dtype(result["hasDiabetesMellitus"])
        assert pd.api.types.is_numeric_dtype(result["lactateDehydrogenaseAtMetastaticDiagnosis"])