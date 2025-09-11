import joblib

from data.data_processing import DataSplitter, DataPreprocessor
from sklearn.preprocessing import StandardScaler
from sklearn.neighbors import NearestNeighbors
import pandas as pd
from utils.settings import Settings
from utils.treatment_combinations import treatment_combinations



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
        neighbors_distribution = neighbors_treatments.value_counts(normalize=True).rename("neighborTreatmentProportion")
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

    def find_similar_patients(self, patient_data):

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

        # TODO: incorperate tumor regression into the data preprocessing, and remove this condition.
        patient_data.drop(columns=["tumorRegression"], inplace=True)
        patient_data = patient_data.drop(columns=self.exclude)

        scaled_patient_data = scaler.transform(patient_data)
        scaled_patient_data_df = pd.DataFrame(scaled_patient_data, index=patient_data.index, columns=patient_data.columns)
        distances, indices = knn.kneighbors(scaled_patient_data_df)

        treatment_distribution = self.get_treatment_distribution_of_neighbors(treatment_df, indices[0])
        treatment_distribution = treatment_distribution.fillna(0)
        return treatment_distribution
