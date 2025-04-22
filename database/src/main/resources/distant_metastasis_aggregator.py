import pandas as pd
import numpy as np

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
                'metastasisLocationGroupsDays': list(sorted_group['daysSinceDiagnosis']),
                'earliestDistantMetastasisDetectionDays': sorted_group['daysSinceDiagnosis'].min()
            })
        return merged.groupby('tumorId').apply(aggregate).reset_index()
    
    @staticmethod
    def aggregate_assessment(group, thresholds, assessment_col, prefix):
        sorted_group = group.sort_values('daysSinceDiagnosis')
        assessments = list(sorted_group[assessment_col])
        dates = list(sorted_group['daysSinceDiagnosis'])
        tumor_id = group['tumorId'].iloc[0]

        if tumor_id in thresholds.index:
            treatment_start = thresholds.loc[tumor_id, 'firstTreatmentStartAfterMetastasis']
            metastasis_threshold = thresholds.loc[tumor_id, 'earliestDistantMetastasisDetectionDays']
        else:
            treatment_start, metastasis_threshold = None, None

        pre_value, pre_date = None, None
        if pd.notna(treatment_start):
            valid = sorted_group[
                (sorted_group['daysSinceDiagnosis'] >= treatment_start - 28) &
                (sorted_group['daysSinceDiagnosis'] <= treatment_start)
            ]
            if not valid.empty:
                row = valid.iloc[-1]
                pre_value = row[assessment_col]
                pre_date = row['daysSinceDiagnosis']

        met_value, met_date = None, None
        if pd.notna(metastasis_threshold):
            valid = sorted_group[
                (sorted_group['daysSinceDiagnosis'] >= metastasis_threshold - 28) &
                (sorted_group['daysSinceDiagnosis'] <= metastasis_threshold + 28)
            ]
            if not valid.empty:
                row = valid.iloc[-1]
                met_value = row[assessment_col]
                met_date = row['daysSinceDiagnosis']

        return pd.Series({
            f'All{prefix}Assessments': assessments,
            f'{prefix}AssessmentsDates': dates,
            f'{prefix}AssessmentBeforeMetastasisTreatment': pre_value,
            f'{prefix}AssessmentDateBeforeMetastasisTreatment': pre_date,
            f'{prefix}AssessmentAtMetastasisDetection': met_value,
            f'{prefix}AssessmentDateAtMetastasisDetection': met_date
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
            treatment_start = group['firstTreatmentStartAfterMetastasis'].iloc[0]
            metastasis_threshold = group['earliestDistantMetastasisDetectionDays'].iloc[0]

            results = {
                f'{camel_case(name)}PreMetastaticTreatment': None for name in lab_names
            }
            results.update({
                f'{camel_case(name)}MetastasisDetection': None for name in lab_names
            })

            for lab_name in lab_names:
                camel_name = camel_case(lab_name)
                lab_group = sorted_group[sorted_group['name'] == lab_name]

                pre = lab_group[
                    (lab_group['daysSinceDiagnosis'] >= treatment_start - 28) &
                    (lab_group['daysSinceDiagnosis'] <= treatment_start)
                ]
                if not pre.empty:
                    results[f'{camel_name}PreMetastaticTreatment'] = pre.iloc[-1]['value']

                met = lab_group[
                    (lab_group['daysSinceDiagnosis'] >= metastasis_threshold - 28) &
                    (lab_group['daysSinceDiagnosis'] <= metastasis_threshold + 28)
                ]
                if not met.empty:
                    results[f'{camel_name}MetastasisDetection'] = met.iloc[-1]['value']

            results['closestLabValueDatePreMetastaticTreatment'] = sorted_group[sorted_group['daysSinceDiagnosis'] <= treatment_start]['daysSinceDiagnosis'].max()
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
    def systemic_treatment(treatment_episode, systemic_treatment, systemic_scheme, systemic_drug, metastasis_agg):
       
        sys_tx = systemic_treatment.rename(columns={'id': 'systemicTreatmentId'})
        sys_scheme = systemic_scheme.rename(columns={'id': 'systemicSchemeId'})

        j1 = (
            treatment_episode
              .merge(sys_tx,      left_on='id', right_on='treatmentEpisodeId', how='inner')
              .merge(sys_scheme,  on='systemicTreatmentId',               how='inner', suffixes=('', '_scheme'))
              .merge(systemic_drug,left_on='systemicSchemeId', right_on='systemicTreatmentSchemeId', how='left')
        )
        j1 = j1.rename(columns={
            'daysBetweenDiagnosisAndStart_x': 'daysBetweenDiagnosisAndStart',
            'daysBetweenDiagnosisAndStop_x':  'daysBetweenDiagnosisAndStop',
        })
        j1 = j1.drop(columns=['daysBetweenDiagnosisAndStart_y',
                              'daysBetweenDiagnosisAndStop_y'])

        agg = (j1.groupby('systemicTreatmentId')
                 .apply(lambda g: pd.Series({
                     'tumorId': g['tumorId'].iat[0],
                     'treatment': g['treatment'].iat[0],
                     'daysBetweenDiagnosisAndStart': g['daysBetweenDiagnosisAndStart'].iat[0],
                     'daysBetweenDiagnosisAndStop':  g['daysBetweenDiagnosisAndStop'].iat[0],
                     'numberOfCycles': (g['numberOfCycles'].dropna().iat[0]
                                        if g['numberOfCycles'].notna().any()
                                        else None)
                 }))
                 .reset_index(drop=True))

        full = agg.merge(
            metastasis_agg[['tumorId', 'earliestDistantMetastasisDetectionDays']],
            on='tumorId', how='right'
        )

        def summarize(group):
            detect_day = group['earliestDistantMetastasisDetectionDays'].iat[0]

            post = group[group['daysBetweenDiagnosisAndStart'] >= detect_day]
            if not post.empty:
                sel = post.sort_values('daysBetweenDiagnosisAndStart').iloc[0]
                start = sel['daysBetweenDiagnosisAndStart']
                stop  = sel['daysBetweenDiagnosisAndStop']
                name  = sel['treatment']
                cycles= sel['numberOfCycles']
            else:
                start, stop, name, cycles = (np.nan, np.nan, None, None)

            had_prior = int((group['daysBetweenDiagnosisAndStart'] < detect_day).any())

            return pd.Series({
                'tumorId': group['tumorId'].iat[0],
                'treatment': name,
                'firstTreatmentStartAfterMetastasis': start,
                'treatmentStop': stop,
                'numberOfCycles': cycles,
                'hasHadPriorSystemicTherapy': had_prior
            })

        result = full.groupby('tumorId').apply(summarize).reset_index(drop=True)
        return result


