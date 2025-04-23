import pandas as pd
import numpy as np

import json
from sqlalchemy import create_engine

import argparse

from distant_metastasis_aggregator import DistantMetastasisAggregator as Agg

class DistantMetastasesOverviewBuilder:
    def __init__(self, engine):
        self.engine = engine
        self.dfs = self._load_data()
        
    def _load_data(self):
        tables = pd.read_sql("SHOW TABLES", con=self.engine)
        return {
            table_name[0]: pd.read_sql(f"SELECT * FROM {table_name[0]}", con=self.engine)
            for table_name in tables.values
        }
    
    def _save_dataframe_to_database(self, df, table_name="distantMetastasesOverview"):
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
        
        return cleaned_df
        
    
    def _prepare_tumor_metadata(self):
        patient = self.dfs['patient'].rename(columns={'id': 'patientId'})
        tumor   = self.dfs['tumor'].rename(columns={'id': 'tumorId'})
        
        surv = self.dfs['survivalMeasurement'].rename(columns={'daysSinceDiagnosis': 'survivalDaysSinceDiagnosis'})
        prim = self.dfs['primaryDiagnosis'].rename(columns={'daysSinceDiagnosis': 'daysSincePrimaryDiagnosis'})
        meta = self.dfs['metastaticDiagnosis'].rename(columns={'daysSinceDiagnosis': 'metastasisDaysSinceDiagnosis'})

        df = (tumor
            .merge(patient, on='patientId', how='inner')
            .merge(surv,    on='tumorId',  how='inner')
            .merge(prim,    on='tumorId',  how='inner')
            .merge(meta,    on='tumorId',  how='left')
        )

        prior_counts = (self.dfs['priorTumor'].groupby('tumorId').size().rename('numberOfPriorTumors').reset_index())
        
        df = df.merge(prior_counts, on='tumorId', how='left')

        return df


    def _prepare_thresholds_and_aggregates(self):
        metastasis_agg = Agg.metastasis_aggregations(
            self.dfs['metastaticDiagnosis'], self.dfs['metastasis']
        )

        systemic_agg = Agg.systemic_treatment(
            self.dfs['treatmentEpisode'],
            self.dfs['systemicTreatment'],
            self.dfs['systemicTreatmentScheme'],
            self.dfs['systemicTreatmentDrug'],
            metastasis_agg
        )

        thresholds = systemic_agg.set_index('tumorId')[['firstTreatmentStartAfterMetastasis']].merge(
            metastasis_agg.set_index('tumorId')[['earliestDistantMetastasisDetectionDays']],
            left_index=True, right_index=True, how='outer'
        )

        who_agg = Agg.who_aggregations(self.dfs['whoAssessment'], thresholds)
        asa_agg = Agg.asa_aggregations(self.dfs['asaAssessment'], thresholds)
        lab_agg = Agg.lab_aggregations(self.dfs['labMeasurement'], thresholds)
        surgeries = Agg.surgeries(
            self.dfs['treatmentEpisode'], self.dfs['primarySurgery'],
            self.dfs['metastaticSurgery'], self.dfs['gastroenterologyResection'],
            self.dfs['hipecTreatment']
        )
        radiotherapy = Agg.radiotherapies(
            self.dfs['treatmentEpisode'], self.dfs['primaryRadiotherapy'], self.dfs['metastaticRadiotherapy']
        )

        return thresholds, {
            'metastasis': metastasis_agg,
            'systemic': systemic_agg,
            'who': who_agg,
            'asa': asa_agg,
            'labs': lab_agg,
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
            .merge(aggregates['systemic'], on='tumorId', how='left')

        df['hadSurvivalEvent'] = (df['isAlive'] == 0).astype(int)
        df['hasHadPriorTumor'] = (~df['numberOfPriorTumors'].isna()).astype(int)
        df['observedOsFromTumorIncidenceDays'] = df['survivalDaysSinceDiagnosis']
        df['observedOsFromMetastasisDetectionDays'] = df['survivalDaysSinceDiagnosis'] - df['earliestDistantMetastasisDetectionDays']
        df['systemicTreatmentPlanDuration'] = df['treatmentStop'] - df['firstTreatmentStartAfterMetastasis']
        
        return df
    
    def build_thresholds(self, first_treatment, metastasis_agg):
        thresholds = first_treatment.set_index('tumorId')[['firstTreatmentStartAfterMetastasis']].merge(
            metastasis_agg.set_index('tumorId')[['earliestDistantMetastasisDetectionDays']],
            left_index=True, right_index=True, how='outer'
        )
        return thresholds 
    
    def clean_final_df(self, df):
        id_cols = [col for col in df.columns if col.startswith('id') and col not in ['tumorId', 'patientId']]
        df = df.drop(columns=id_cols)
        days_cols = [col for col in df.columns
                        if 'daysSinceDiagnosis' in col and col not in ['observedOsFromTumorIncidenceDays', 'firstTreatmentStartAfterMetastasis']]
        df = df.drop(columns=days_cols)
    
        df = df.rename(columns={"investigatedLymphNodesCount_x" : "investigatedLymphNodesCountPrimaryDiagnosis","positiveLymphNodesCount_x": "positiveLymphNodesCountPrimaryDiagnosis","investigatedLymphNodesCount_y":"investigatedLymphNodesCountMetastaticDiagnosis","positiveLymphNodesCount_y": "positiveLymphNodesCountMetastaticDiagnosis"})
        
        df['observedOsFromTreatmentStartDays'] = df['observedOsFromTumorIncidenceDays'] - df['firstTreatmentStartAfterMetastasis']
        
        for col in df.select_dtypes(include='float').columns:
            ser = df[col]
            if ser.dropna().map(float.is_integer).all():
                df[col] = ser.astype('Int64')

        list_cols = [col for col in df.columns if df[col].apply(lambda x: isinstance(x, list)).any()]
        
        def _cast_list(lst):
            if not isinstance(lst, list):
                return lst
            out = []
            for x in lst:
                if isinstance(x, float) and x.is_integer():
                    out.append(int(x))
                else:
                    out.append(x)
            return out

        for col in list_cols:
            df[col] = df[col].apply(_cast_list)
        
        ordered_cols = [
            # Patient & Diagnosis
            'tumorId', 'patientId', 'diagnosisYear', 'ageAtDiagnosis',
            'sex', 'source', 'sourceId', 'daysSinceTumorIncidence',
            'isAlive', 'basisOfDiagnosis', 'numberOfPriorTumors',
            'hasDoublePrimaryTumor', 'primaryTumorType',
            'primaryTumorLocation', 'sidedness',
            'anorectalVergeDistanceCategory', 'mesorectalFasciaIsClear',
            'distanceToMesorectalFasciaMm', 'differentiationGrade',
            'clinicalTnmT', 'clinicalTnmN', 'clinicalTnmM',
            'pathologicalTnmT', 'pathologicalTnmN', 'pathologicalTnmM',
            'clinicalTumorStage', 'pathologicalTumorStage',
            'investigatedLymphNodesCountPrimaryDiagnosis',
            'positiveLymphNodesCountPrimaryDiagnosis',
            'presentedWithIleus', 'presentedWithPerforation',
            'venousInvasionDescription', 'lymphaticInvasionCategory',
            'extraMuralInvasionCategory', 'tumorRegression',

            # Metastatic Diagnosis
            'isMetachronous', 'numberOfLiverMetastases',
            'maximumSizeOfLiverMetastasisMm',
            'investigatedLymphNodesCountMetastaticDiagnosis',
            'positiveLymphNodesCountMetastaticDiagnosis',
            'metastasisLocationGroups', 'metastasisLocationGroupsDays',
            'earliestDistantMetastasisDetectionDays',

            # Performance / Comorbidity
            'AllWhoAssessments', 'WhoAssessmentsDates',
            'WhoAssessmentBeforeMetastasisTreatment',
            'WhoAssessmentDateBeforeMetastasisTreatment',
            'WhoAssessmentAtMetastasisDetection',
            'WhoAssessmentDateAtMetastasisDetection',
            'AllAsaAssessments', 'AsaAssessmentsDates',
            'AsaAssessmentBeforeMetastasisTreatment',
            'AsaAssessmentDateBeforeMetastasisTreatment',
            'AsaAssessmentAtMetastasisDetection',
            'AsaAssessmentDateAtMetastasisDetection',

            # Labs
            'lactateDehydrogenasePreTreatment',
            'alkalinePhosphatasePreTreatment',
            'leukocytesAbsolutePreTreatment',
            'carcinoembryonicAntigenPreTreatment',
            'albuminePreTreatment',
            'neutrophilsAbsolutePreTreatment',
            'lactateDehydrogenaseMetastasisDetection',
            'alkalinePhosphataseMetastasisDetection',
            'leukocytesAbsoluteMetastasisDetection',
            'carcinoembryonicAntigenMetastasisDetection',
            'albumineMetastasisDetection',
            'neutrophilsAbsoluteMetastasisDetection',
            'closestLabValueDatePreTreatment',
            'closestLabValueDateMetastasisDetection',

            # Surgeries
            'surgeriesPrimary', 'surgeriesPrimaryDates',
            'surgeriesMetastatic', 'surgeriesMetastaticDates',
            'surgeriesGastroenterology', 'surgeriesGastroenterologyDates',
            'hadHipec', 'hadHipecDate',

            # Radiotherapy
            'radiotherapiesPrimary', 'radiotherapiesPrimaryDates',
            'radiotherapiesMetastatic', 'radiotherapiesMetastaticDates',

            # Systemic
            'treatment', 'firstTreatmentStartAfterMetastasis',
            'treatmentStop', 'numberOfCycles',
            'hasHadPriorSystemicTherapy',

            # Outcomes
            'hadSurvivalEvent', 'hasHadPriorTumor',
            'observedOsFromTumorIncidenceDays',
            'observedOsFromMetastasisDetectionDays',
            'observedOsFromTreatmentStartDays',
            'systemicTreatmentPlanDuration',
        ]

        ordered = [c for c in ordered_cols if c in df.columns]
        
        return df[ordered]

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate distant metastasis overview table")
    parser.add_argument("--db_host", required=True, help="Database host")
    parser.add_argument("--db_port", required=True, type=int, help="Database port")
    parser.add_argument("--db_user", required=True, help="Database user")
    parser.add_argument("--db_password", required=True, help="Database password")
    parser.add_argument("--db_name", required=True, help="Database name")
    parser.add_argument("--table_name", default="distantMetastasesOverview", help="Output table name")
    args = parser.parse_args()

    engine = create_engine(
        f"mysql+pymysql://{args.db_user}:{args.db_password}@{args.db_host}:{args.db_port}/{args.db_name}"
    )
    
    print("Starting distant metastasis overview generation...")
    builder = DistantMetastasesOverviewBuilder(engine)
    
    builder.create_distant_metastasis_overview()