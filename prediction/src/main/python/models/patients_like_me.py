import joblib

from data.data_processing import DataSplitter, DataPreprocessor
from sklearn.preprocessing import StandardScaler
from sklearn.neighbors import NearestNeighbors
import pandas as pd
from utils.settings import Settings
from utils.treatment_combinations import treatment_combinations
import numpy as np



class PatientsLikeMeModel:

    treatment_columns = [
        "systemicTreatmentPlan_5-FU",
        "systemicTreatmentPlan_oxaliplatin",
        "systemicTreatmentPlan_irinotecan",
        "systemicTreatmentPlan_bevacizumab",
        "systemicTreatmentPlan_panitumumab",
        "systemicTreatmentPlan_pembrolizumab",
        "systemicTreatmentPlan_nivolumab"
    ]

    exclude = [
        *treatment_columns,
        'hasTreatment',
        'survivalDaysSinceMetastaticDiagnosis',
        'hadSurvivalEvent'
        # 'investigatedLymphNodesCountPrimaryDiagnosis',
        # 'hasRasMutation'
    ]

    def __init__(self, settings: Settings):
        self.settings = settings

    def get_treatment_distribution_of_neighbors(self, treatment_df, indices):
        neighbors_treatments = treatment_df.loc[indices]
        overall_distribution = treatment_df.value_counts(normalize=True).rename("overallTreatmentProportion")
        neighbors_distribution = neighbors_treatments.value_counts(normalize=True).rename("similarPatientsTreatmentProportion")
        return pd.concat([overall_distribution, neighbors_distribution], axis=1)
        

    def merge_treatment_columns(self, treatment_matrix: pd.DataFrame):
        mapping_df = pd.DataFrame.from_dict(treatment_combinations, orient="index").reset_index().rename(columns={"index": "treatment"})
        treatments = treatment_matrix.merge(
            mapping_df,
            on=self.treatment_columns,
            how="left"
        )
        treatments = treatments["treatment"]
        return treatments

    def _get_original_feature_distribution(self, patient_data, df, indices):
        neighbors_data = df.loc[indices]
        results = []

        for col in patient_data.columns:
            if col in self.exclude:
                continue

            col_results = {}

            # Patient’s own value
            patient_value = patient_data[col].iloc[0]
            if pd.isna(patient_value):
                patient_value = "N/A"
            elif isinstance(patient_value, (bool, np.bool_)):  
                patient_value = "1" if patient_value else "0"
            else:
                patient_value = str(patient_value)
            col_results["patientValue"] = patient_value

            # Neighbors data
            neighbor_counts = (
                neighbors_data[col]
                .value_counts(normalize=True, dropna=False)
                .round(4)
                .rename(f"{col}")
            )
            neighbor_counts.index = neighbor_counts.index.fillna("N/A")
            neighbor_counts = neighbor_counts.reindex(
                sorted(neighbor_counts.index, key=lambda x: (x == "N/A", x))
            )
            col_results["neighborsDistribution"] = neighbor_counts.to_dict()

            # Overall data
            overall_counts = (
                df[col]
                .value_counts(normalize=True, dropna=False)
                .round(4)
                .rename(f"overall_{col}")
            )
            overall_counts.index = overall_counts.index.fillna("N/A")
            overall_counts = overall_counts.reindex(
                sorted(overall_counts.index, key=lambda x: (x == "N/A", x))
            )
            col_results["overallDistribution"] = overall_counts.to_dict()

            # Wrap with feature name and append to list
            results.append({
                "feature": col,
                **col_results
            })

        with open("feature_distribution.json", "w") as f:
            import json
            json.dump(results, f)
        return results

    def find_similar_patients(self, patient_data, processed_patient_df: pd.DataFrame):

        preprocessor = DataPreprocessor(settings=self.settings, fit=True)
        df_original = preprocessor.load_data()
        df, _, _ = preprocessor.preprocess_data()

        treatment_df = self.merge_treatment_columns(df[self.exclude])
        df = df.drop(columns=self.exclude)

        scaler = StandardScaler()
        scaled_array = scaler.fit_transform(df)
        df_scaled = pd.DataFrame(scaled_array, index=df.index, columns=df.columns)

        knn = NearestNeighbors(n_neighbors=25, metric="euclidean")
        knn.fit(df_scaled)

        processed_patient_df.drop(columns=["tumorRegression"], inplace=True)
        processed_patient_df = processed_patient_df.drop(columns=self.exclude)

        scaled_patient_data = scaler.transform(processed_patient_df)
        scaled_patient_data_df = pd.DataFrame(scaled_patient_data, index=processed_patient_df.index, columns=processed_patient_df.columns)
        distances, indices = knn.kneighbors(scaled_patient_data_df)

        treatment_distribution = self.get_treatment_distribution_of_neighbors(treatment_df, indices[0])
        treatment_distribution = treatment_distribution.fillna(0)

        self._get_original_feature_distribution(patient_data, df_original, indices[0])
        return treatment_distribution
