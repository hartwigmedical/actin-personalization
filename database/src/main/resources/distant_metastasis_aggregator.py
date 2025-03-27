import pandas as pd

class DistantMetastasisAggregator:
    """Handles all aggregation logic used for building the distant metastasis overview."""

    @staticmethod
    def metastasis_aggregations(metastatic_diagnosis, metastasis):
        merged = metastasis.merge(
            metastatic_diagnosis, left_on='metastaticDiagnosisId', right_on='id', how='inner'
        )
        def aggregate(group):
            sorted_group = group.sort_values('daysSinceDiagnosis')
            return pd.Series({
                'metastasisLocationGroups': list(sorted_group['location']),
                'metastasisLocationGroupsDates': list(sorted_group['daysSinceDiagnosis']),
                'earliestMetastasisDetectionDays': sorted_group['daysSinceDiagnosis'].min()
            })
        return merged.groupby('tumorId').apply(aggregate).reset_index()

    @staticmethod
    def aggregate_assessment(group, thresholds, assessment_col, prefix):
        sorted_group = group.sort_values('daysSinceDiagnosis')
        assessments = list(sorted_group[assessment_col])
        dates = list(sorted_group['daysSinceDiagnosis'])
        tumor_id = group['tumorId'].iloc[0]

        if tumor_id in thresholds.index:
            treatment_start = thresholds.loc[tumor_id, 'firstTreatmentStart']
            metastasis_threshold = thresholds.loc[tumor_id, 'earliestMetastasisDetectionDays']
        else:
            treatment_start, metastasis_threshold = None, None

        pre_value, pre_date = None, None
        if pd.notna(treatment_start):
            valid = sorted_group[sorted_group['daysSinceDiagnosis'] <= treatment_start]
            if not valid.empty:
                row = valid.iloc[-1]
                pre_value = row[assessment_col]
                pre_date = row['daysSinceDiagnosis']

        met_value, met_date = None, None
        if pd.notna(metastasis_threshold):
            valid = sorted_group[sorted_group['daysSinceDiagnosis'] <= metastasis_threshold]
            if not valid.empty:
                row = valid.iloc[-1]
                met_value = row[assessment_col]
                met_date = row['daysSinceDiagnosis']

        return pd.Series({
            f'All{prefix}Assessments': assessments,
            f'{prefix}AssessmentsDates': dates,
            f'{prefix}AssessmentPreTreatment': pre_value,
            f'{prefix}AssessmentAssessmentDatePreTreatment': pre_date,
            f'{prefix}AssessmentMetastasisDetection': met_value,
            f'{prefix}AssessmentDateMetastasisDetection': met_date
        })

    @staticmethod
    def who_aggregations(who_assessment, thresholds):
        return who_assessment.groupby('tumorId').apply(
            lambda g: DistantMetastasisAggregator.aggregate_assessment(g, thresholds, 'whoStatus', 'Who')
        ).reset_index()

    @staticmethod
    def asa_aggregations(asa_assessment, thresholds):
        return asa_assessment.groupby('tumorId').apply(
            lambda g: DistantMetastasisAggregator.aggregate_assessment(g, thresholds, 'asaClassification', 'Asa')
        ).reset_index()

    @staticmethod
    def lab_aggregations(lab, thresholds):
        merged_labs = lab.merge(thresholds, on='tumorId', how='inner')
        lab_names = lab['name'].dropna().unique()

        def camel_case(s):
            parts = s.split('_')
            return parts[0].lower() + ''.join(word.capitalize() for word in parts[1:])

        def aggregate(group):
            sorted_group = group.sort_values('daysSinceDiagnosis')
            tumor_id = group['tumorId'].iloc[0]
            treatment_start = group['firstTreatmentStart'].iloc[0]
            metastasis_threshold = group['earliestMetastasisDetectionDays'].iloc[0]

            results = {
                f'{camel_case(name)}PreTreatment': None for name in lab_names
            }
            results.update({
                f'{camel_case(name)}MetastasisDetection': None for name in lab_names
            })

            for lab_name in lab_names:
                camel_name = camel_case(lab_name)
                lab_group = sorted_group[sorted_group['name'] == lab_name]

                pre = lab_group[lab_group['daysSinceDiagnosis'] <= treatment_start]
                if not pre.empty:
                    results[f'{camel_name}PreTreatment'] = pre.iloc[-1]['value']

                met = lab_group[lab_group['daysSinceDiagnosis'] <= metastasis_threshold]
                if not met.empty:
                    results[f'{camel_name}MetastasisDetection'] = met.iloc[-1]['value']

            results['closestLabValueDatePreTreatment'] = sorted_group[sorted_group['daysSinceDiagnosis'] <= treatment_start]['daysSinceDiagnosis'].max()
            results['closestLabValueDateMetastasisDetection'] = sorted_group[sorted_group['daysSinceDiagnosis'] <= metastasis_threshold]['daysSinceDiagnosis'].max()

            return pd.Series(results)

        return merged_labs.groupby('tumorId').apply(aggregate).reset_index()


    @staticmethod
    def surgeries(treatment_episode, primary, metastatic, gastro, hipec):
        merged = treatment_episode.merge(primary, left_on='id', right_on='treatmentEpisodeId', how='left', suffixes=('', '_ps')) \
            .merge(metastatic, left_on='id', right_on='treatmentEpisodeId', how='left', suffixes=('', '_ms')) \
            .merge(gastro, left_on='id', right_on='treatmentEpisodeId', how='left', suffixes=('', '_gr')) \
            .merge(hipec, left_on='id', right_on='treatmentEpisodeId', how='left', suffixes=('', '_hipec'))

        def aggregate(group):
            return pd.Series({
                'surgeriesPrimary': list(group.dropna(subset=['type']).sort_values('daysSinceDiagnosis')['type']) or None,
                'surgeriesPrimaryDates': list(group.dropna(subset=['daysSinceDiagnosis']).sort_values('daysSinceDiagnosis')['daysSinceDiagnosis']) or None,
                'surgeriesMetastatic': list(group.dropna(subset=['type_ms']).sort_values('daysSinceDiagnosis_ms')['type_ms']) or None,
                'surgeriesMetastaticDates': list(group.dropna(subset=['daysSinceDiagnosis_ms']).sort_values('daysSinceDiagnosis_ms')['daysSinceDiagnosis_ms']) or None,
                'surgeriesGastroenterology': list(group.dropna(subset=['resectionType']).sort_values('daysSinceDiagnosis_gr')['resectionType']) or None,
                'surgeriesGastroenterologyDates': list(group.dropna(subset=['daysSinceDiagnosis_gr']).sort_values('daysSinceDiagnosis_gr')['daysSinceDiagnosis_gr']) or None,
                'hadHipec': 1 if group['id_hipec'].notna().any() else 0,
                'hadHipecDate': group['daysSinceDiagnosis_hipec'].min() if group['id_hipec'].notna().any() else None
            })

        return merged.groupby('tumorId').apply(aggregate).reset_index()

    @staticmethod
    def radiotherapies(treatment_episode, primary, metastatic):
        merged = treatment_episode.merge(primary, left_on='id', right_on='treatmentEpisodeId', how='left', suffixes=('', '_pr')) \
            .merge(metastatic, left_on='id', right_on='treatmentEpisodeId', how='left', suffixes=('', '_mr'))

        def aggregate(group):
            return pd.Series({
                'radiotherapiesPrimary': list(group.dropna(subset=['type']).sort_values('daysBetweenDiagnosisAndStart')['type']) or None,
                'radiotherapiesPrimaryDates': list(group.dropna(subset=['daysBetweenDiagnosisAndStart']).sort_values('daysBetweenDiagnosisAndStart')['daysBetweenDiagnosisAndStart']) or None,
                'radiotherapiesMetastatic': list(group.dropna(subset=['type_mr']).sort_values('daysBetweenDiagnosisAndStart_mr')['type_mr']) or None,
                'radiotherapiesMetastaticDates': list(group.dropna(subset=['daysBetweenDiagnosisAndStart_mr']).sort_values('daysBetweenDiagnosisAndStart_mr')['daysBetweenDiagnosisAndStart_mr']) or None
            })

        return merged.groupby('tumorId').apply(aggregate).reset_index()
    @staticmethod
    def systemic_treatment(treatment_episode, systemic_treatment, systemic_scheme, systemic_drug):
        systemic_treatment = systemic_treatment.rename(columns={'id': 'systemicTreatmentId'})
        systemic_scheme = systemic_scheme.rename(columns={'id': 'systemicSchemeId'})
        join1 = treatment_episode.merge(systemic_treatment, left_on='id', right_on='treatmentEpisodeId', how='inner')
        join2 = join1.merge(systemic_scheme, on='systemicTreatmentId', how='inner', suffixes=('', '_scheme'))
        join3 = join2.merge(systemic_drug, left_on='systemicSchemeId', right_on='systemicTreatmentSchemeId', how='left', suffixes=('', '_drug'))
        aggregated_treatment = join3.groupby('systemicTreatmentId').apply(
            lambda group: pd.Series({
                'tumorId': group['tumorId'].iloc[0],
                'treatment': group['treatment'].iloc[0],
                'daysBetweenDiagnosisAndStart': group['daysBetweenDiagnosisAndStart'].iloc[0],
                'daysBetweenDiagnosisAndStop': group['daysBetweenDiagnosisAndStop'].iloc[0],
                'numberOfCycles': group['numberOfCycles'].dropna().iloc[0] if not group['numberOfCycles'].dropna().empty else None
            })
        ).reset_index()
        def aggregate(group):
            sorted_group = group.sort_values('daysBetweenDiagnosisAndStart')
            return pd.Series({
                'systemicTreatmentPlan': list(sorted_group['treatment']),
                'firstTreatmentStart': sorted_group['daysBetweenDiagnosisAndStart'].min(),
                'numberOfCycles': list(sorted_group['numberOfCycles']),
                'treatmentStop': sorted_group['daysBetweenDiagnosisAndStop'].max() if not sorted_group['daysBetweenDiagnosisAndStop'].isna().all() else None
            })
        return aggregated_treatment.groupby('tumorId').apply(aggregate).reset_index()

