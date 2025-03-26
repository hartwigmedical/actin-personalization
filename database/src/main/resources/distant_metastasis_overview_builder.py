import pandas as pd
import numpy as np

import json
from sqlalchemy import create_engine

import argparse

from distant_metastasis_aggregator import DistantMetastasisAggregator as Agg

class DistantMetastasisOverviewBuilder:
    def __init__(self, engine):
        self.engine = engine
        self.dfs = self._load_data()
        
    def _load_data(self):
        tables = pd.read_sql("SHOW TABLES", con=self.engine)
        return {
            table_name[0]: pd.read_sql(f"SELECT * FROM {table_name[0]}", con=self.engine)
            for table_name in tables.values
        }
    def _save_dataframe_to_database(self, df, table_name="distantMetastasisOverview"):
        def list_to_json(cell):
            if isinstance(cell, (list, dict)):
                return json.dumps(cell)
            return cell
        
        df = df.applymap(list_to_json)
        df = df.replace({np.nan: None})
        df.to_sql(table_name, engine, if_exists="replace", index=False)
        
        print(f"Data successfully written to {table_name} table.")
               
    def create_distant_metastasis_overview(self):
        print("Start preparing tumor metadata...")
        tumor_metadata = self._prepare_tumor_metadata()
        print("Start aggregating metadata...")
        thresholds, aggregates = self._prepare_thresholds_and_aggregates()
        print("Start merging overview...")
        overview = self._merge_overview_components(tumor_metadata, thresholds, aggregates)
        cleaned_df = self.clean_final_df(overview)
        self._save_dataframe_to_database(cleaned_df)
        
    
    def _prepare_tumor_metadata(self):
        patient = self.dfs['patient'].rename(columns={'id': 'patientId'})
        tumor = self.dfs['tumor'].rename(columns={'id': 'tumorId'})

        merged_tumor_df = tumor.merge(patient, on='patientId', how='inner') \
                      .merge(self.dfs['survivalMeasurement'], on='tumorId', how='inner') \
                      .merge(self.dfs['primaryDiagnosis'], on='tumorId', how='inner') \
                      .merge(self.dfs['metastaticDiagnosis'], on='tumorId', how='left')

        prior_counts = self.dfs['priorTumor'].groupby('tumorId')['id'].count().rename('numberOfPriorTumors').reset_index()
        merged_tumor_df = merged_tumor_df.merge(prior_counts, on='tumorId', how='left')

        comorbidity = self.dfs['comorbidityAssessment']
        molecular = self.dfs['molecularResult']
        merged_tumor_df = merged_tumor_df.merge(comorbidity[comorbidity['daysSinceDiagnosis'] == 0], on='tumorId', how='left') \
                       .merge(molecular[molecular['daysSinceDiagnosis'] == 0], on='tumorId', how='left')

        return merged_tumor_df

    def _prepare_thresholds_and_aggregates(self):
        
        first_systemic = Agg.systemic_treatment(
            self.dfs['treatmentEpisode'], self.dfs['systemicTreatment'],
            self.dfs['systemicTreatmentScheme'], self.dfs['systemicTreatmentDrug']
        )
        metastasis_agg = Agg.metastasis_aggregations(self.dfs['metastaticDiagnosis'], self.dfs['metastasis'])
        thresholds = self.build_thresholds(first_systemic, metastasis_agg)

        who_agg = Agg.who_aggregations(self.dfs['whoAssessment'], thresholds)
        asa_agg = Agg.asa_aggregations(self.dfs['asaAssessment'], thresholds)
        lab_before_treatment = Agg.lab_values_before_treatment(self.dfs['labMeasurement'], self.dfs['treatmentEpisode'], self.dfs['systemicTreatment'])
        surgeries = Agg.surgeries(self.dfs['treatmentEpisode'], self.dfs['primarySurgery'], self.dfs['metastaticSurgery'], self.dfs['gastroenterologyResection'], self.dfs['hipecTreatment'])
        radiotherapy = Agg.radiotherapies(self.dfs['treatmentEpisode'], self.dfs['primaryRadiotherapy'], self.dfs['metastaticRadiotherapy'])

        return thresholds, {
            'metastasis': metastasis_agg,
            'first_systemic': first_systemic,
            'who': who_agg,
            'asa': asa_agg,
            'labs': lab_before_treatment,
            'surgeries': surgeries,
            'radiotherapy': radiotherapy
        }

    def _merge_overview_components(self, tumor_metadata, thresholds, aggregates):
        df = tumor_metadata \
            .merge(aggregates['metastasis'], on='tumorId', how='left') \
            .merge(aggregates['who'], on='tumorId', how='left') \
            .merge(aggregates['asa'], on='tumorId', how='left') \
            .merge(aggregates['labs'], on='tumorId', how='left') \
            .merge(aggregates['surgeries'], on='tumorId', how='left') \
            .merge(aggregates['radiotherapy'], on='tumorId', how='left') \
            .merge(aggregates['first_systemic'], on='tumorId', how='left')

        df = self.add_metastasis_before_treatment(df)

        df['hadSurvivalEvent'] = (df['isAlive'] == 0).astype(int)
        df['hasHadPriorTumor'] = (~df['numberOfPriorTumors'].isna()).astype(int)
        df['observedOsFromTumorIncidenceDays'] = df['daysSinceDiagnosis']
        df['observedOsFromMetastasisDetectionDays'] = df['daysSinceDiagnosis'] - df['earliestMetastasisDetectionDays']
        df['systemicTreatmentPlanDuration'] = df['treatmentStop'] - df['firstTreatmentStart']
        
        return df
    
    def build_thresholds(self, first_treatment, metastasis_agg):
        thresholds = first_treatment.set_index('tumorId')[['firstTreatmentStart']].merge(
            metastasis_agg.set_index('tumorId')[['earliestMetastasisDetectionDays']],
            left_index=True, right_index=True, how='outer'
        )
        return thresholds
    
    def add_metastasis_before_treatment(self, df):
        def set_flag(row):
            if pd.notna(row['earliestMetastasisDetectionDays']):
                no_treatment = pd.isna(row['firstTreatmentStart'])
                metastasis_before_treatment = row['earliestMetastasisDetectionDays'] <= row['firstTreatmentStart'] if not no_treatment else False
                if no_treatment or metastasis_before_treatment:
                    return 1
            return 0
        df['metastasisBeforeTreatment'] = df.apply(set_flag, axis=1)
        return df
    
    
    def clean_final_df(self, df):
        id_cols = [col for col in df.columns if col.startswith('id') and col not in ['tumorId', 'patientId']]
        df = df.drop(columns=id_cols)
        days_cols = [col for col in df.columns
                        if 'daysSinceDiagnosis' in col and col not in ['observedOsFromTumorIncidenceDays', 'firstTreatmentStart']]
        df = df.drop(columns=days_cols)
    
        df = df.rename(columns={"investigatedLymphNodesCount_x" : "investigatedLymphNodesCountPrimaryDiagnosis", "investigatedLymphNodesCount_y":"investigatedLymphNodesCountMetastaticDiagnosis"})
        
        df['observedOsFromTreatmentStartDays'] = df['observedOsFromTumorIncidenceDays'] - df['firstTreatmentStart']
        return df

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate distant metastasis overview table")
    parser.add_argument("--db_host", required=True, help="Database host")
    parser.add_argument("--db_port", required=True, type=int, help="Database port")
    parser.add_argument("--db_user", required=True, help="Database user")
    parser.add_argument("--db_password", required=True, help="Database password")
    parser.add_argument("--db_name", required=True, help="Database name")
    parser.add_argument("--table_name", default="distantMetastasisOverview", help="Output table name")
    args = parser.parse_args()

    engine = create_engine(
        f"mysql+pymysql://{args.db_user}:{args.db_password}@{args.db_host}:{args.db_port}/{args.db_name}"
    )
    
    print("Starting distant metastasis overview generation...")
    builder = DistantMetastasisOverviewBuilder(engine)
    
    builder.create_distant_metastasis_overview()


