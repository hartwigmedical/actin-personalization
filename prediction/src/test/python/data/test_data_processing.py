import pytest
import pandas as pd
import numpy as np
import json
import os

from data.data_processing import DataPreprocessor
from python.utils.testSettings import test_settings

class TestDataPreprocessor:
    @pytest.fixture(scope="class")
    def test_df(self):
        path = os.path.join(os.path.dirname(__file__), "testReferenceObject.json")
        with open(path) as f:
            data = json.load(f)
        return pd.DataFrame([data])

    @pytest.fixture(scope="class")
    def dp(self):
        return DataPreprocessor(dp_settings=test_settings, fit=False)

    @pytest.mark.parametrize("treatment,expected", [
        ("FOLFOX", {"systemicTreatmentPlan_5-FU": 1, "systemicTreatmentPlan_oxaliplatin": 1, "systemicTreatmentPlan_irinotecan": 0, "systemicTreatmentPlan_bevacizumab": 0, "systemicTreatmentPlan_panitumumab": 0, "systemicTreatmentPlan_pembrolizumab": 0, "systemicTreatmentPlan_nivolumab": 0}),
        ("FOLFIRI_B", {"systemicTreatmentPlan_5-FU": 1, "systemicTreatmentPlan_oxaliplatin": 0, "systemicTreatmentPlan_irinotecan": 1, "systemicTreatmentPlan_bevacizumab": 1, "systemicTreatmentPlan_panitumumab": 0, "systemicTreatmentPlan_pembrolizumab": 0, "systemicTreatmentPlan_nivolumab": 0}),
        ("", {"systemicTreatmentPlan_5-FU": 0, "systemicTreatmentPlan_oxaliplatin": 0, "systemicTreatmentPlan_irinotecan": 0, "systemicTreatmentPlan_bevacizumab": 0, "systemicTreatmentPlan_panitumumab": 0, "systemicTreatmentPlan_pembrolizumab": 0, "systemicTreatmentPlan_nivolumab": 0}),
    ])
    def test_parse_treatment(self, dp, treatment, expected):
        result = dp.parse_treatment(treatment)
        assert result == expected

    def test_group_treatments(self, dp):
        df = pd.DataFrame({"firstSystemicTreatmentAfterMetastaticDiagnosis": ["FOLFOX", "", None]})
        df = dp.group_treatments(df)
        assert "treatment" in df.columns
        assert list(df["treatment"]) == [1, 0, 0]

    def test_handle_missing_values_sets_and_uses_median(self, dp):
        df = pd.DataFrame({
            "foo": [1.0, 2.0, np.nan, 4.0],
            test_settings.duration_col: [1, 2, 3, 4],
            test_settings.event_col: [1, 1, 0, 1]
        })
        dp.fit = True
        df = dp.handle_missing_values(df)
        assert not df["foo"].isnull().any()
        assert "foo" in dp.medians

    def test_knn_imputation_fills_na(self, dp):
        df = pd.DataFrame({"whoAssessmentAtMetastaticDiagnosis": [1.0, None, 3.0, None]})
        df = dp.impute_knn(df, ["whoAssessmentAtMetastaticDiagnosis"], k=2)
        assert not df["whoAssessmentAtMetastaticDiagnosis"].isnull().any()


    def test_encode_categorical_creates_dummies(self, dp):
        df = pd.DataFrame({"color": ["red", "blue", "green", "red"]})
        df[test_settings.duration_col] = [1, 2, 3, 4]
        df[test_settings.event_col] = [0, 1, 1, 0]
        print(test_settings.save_models)
        df_encoded = dp.encode_categorical(df)
        assert any("color_" in col for col in df_encoded.columns)

    def test_auto_cast_object_columns(self, dp):
        df = pd.DataFrame({
            "bool_as_object": ["1", "0", "1"],
            "float_as_str": ["1.0", "2.0", "3.0"]
        })
        df_cast = dp.auto_cast_object_columns(df)
        assert df_cast["bool_as_object"].dtype in [np.float64, np.int64]
        assert df_cast["float_as_str"].dtype in [np.float64, np.int64]