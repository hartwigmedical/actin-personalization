CREATE OR REPLACE VIEW metastaticTreatmentEpisode AS (

    SELECT * FROM treatmentEpisode WHERE metastaticPresence IN ('AT_START', 'AT_PROGRESSION')
);

CREATE OR REPLACE VIEW distantMetastasesOverview AS (

    SELECT
        patient.sourceId,
        patient.sex,
        tumor.ageAtDiagnosis,
        treatmentEpisode.metastaticPresence,
        survivalMeasurement.isAlive,
        survivalMeasurement.isAlive = 0 as hadSurvivalEvent,
        survivalMeasurement.daysSinceDiagnosis as observedOsFromTumorIncidenceDays,

        primaryDiagnosis.hasDoublePrimaryTumor,
        primaryDiagnosis.primaryTumorType,
        primaryDiagnosis.primaryTumorLocation,
        primaryDiagnosis.sidedness,
        primaryDiagnosis.anorectalVergeDistanceCategory,
        primaryDiagnosis.mesorectalFasciaIsClear,
        primaryDiagnosis.distanceToMesorectalFasciaMm,

        primaryDiagnosis.differentiationGrade,

        primaryDiagnosis.clinicalTnmT,
        primaryDiagnosis.clinicalTnmN,
        primaryDiagnosis.clinicalTnmM,
        primaryDiagnosis.pathologicalTnmT,
        primaryDiagnosis.pathologicalTnmN,
        primaryDiagnosis.pathologicalTnmM,
        primaryDiagnosis.clinicalTumorStage,
        primaryDiagnosis.pathologicalTumorStage,

        primaryDiagnosis.presentedWithIleus,
        primaryDiagnosis.presentedWithPerforation,
        primaryDiagnosis.venousInvasionDescription,
        primaryDiagnosis.lymphaticInvasionCategory,
        primaryDiagnosis.extraMuralInvasionCategory,

        not(isnull(numberOfPriorTumors)) AS hasHadPriorTumor,

        metastaticDiagnosis.isMetachronous,
        metastaticDiagnosis.numberOfLiverMetastases,
        metastaticDiagnosis.maximumSizeOfLiverMetastasisMm,
        metastaticDiagnosis.investigatedLymphNodesCount,
        metastaticDiagnosis.positiveLymphNodesCount,

        metastasisAggregated.metastasisLocationGroups, -- list/array of metastasis locations [], can be taken from metastasis table locations
        metastasisAggregated.metastasisLocationGroupsDates, -- list/array of dates (days from tumor incidence/diagnosis) in same order as metastasis locations []

        comorbidityAssessment.charlsonComorbidityIndex AS cci,
        comorbidityAssessment.hasAids,
        comorbidityAssessment.hasCongestiveHeartFailure,
        comorbidityAssessment.hasCollagenosis,
        comorbidityAssessment.hasCopd,
        comorbidityAssessment.hasCerebrovascularDisease,
        comorbidityAssessment.hasDementia,
        comorbidityAssessment.hasDiabetesMellitus,
        comorbidityAssessment.hasDiabetesMellitusWithEndOrganDamage,
        comorbidityAssessment.hasOtherMalignancy,
        comorbidityAssessment.hasOtherMetastaticSolidTumor,
        comorbidityAssessment.hasMyocardialInfarct,
        comorbidityAssessment.hasMildLiverDisease,
        comorbidityAssessment.hasHemiplegiaOrParaplegia,
        comorbidityAssessment.hasPeripheralVascularDisease,
        comorbidityAssessment.hasRenalDisease,
        comorbidityAssessment.hasLiverDisease,
        comorbidityAssessment.hasUlcerDisease,

        molecularResult.hasMsi,
        molecularResult.hasBrafMutation,
        molecularResult.hasBrafV600EMutation,
        molecularResult.hasRasMutation,
        molecularResult.hasKrasG12CMutation,

        whoAggregated.allWhoAssessments, -- list/array of who assessments []
        whoAggregated.whoAssessmentsDates, -- list/array of dates (days from tumor incidence/diagnosis) in same order as who assessments []

        asaAggregated.allAsaAssessments, -- list/array of asa assessments []
        asaAggregated.asaAssessmentsDates, -- list/array of dates (days from tumor incidence/diagnosis) in same order as asa assessments []

        labValuesBeforeTreatment.albumine,
        labValuesBeforeTreatment.alkalinePhosphatase,
        labValuesBeforeTreatment.carcinoEmbryonicAntigen,
        labValuesBeforeTreatment.lactateDehydrogenase,
        labValuesBeforeTreatment.leukocytesAbsolute,
        labValuesBeforeTreatment.neutrophilsAbsolute,

        surgeriesAggregated.surgeriesPrimary,
        surgeriesAggregated.surgeriesPrimaryDates,
        surgeriesAggregated.surgeriesMetastatic,
        surgeriesAggregated.surgeriesMetastaticDates,
        surgeriesAggregated.surgeriesGastroenterology,
        surgeriesAggregated.surgeriesGastroenterologyDates,
        surgeriesAggregated.hadHipec,
        surgeriesAggregated.hadHipecDate,

        radiotherapiesAggregated.radiotherapiesPrimary,
        radiotherapiesAggregated.radiotherapiesPrimaryDates,
        radiotherapiesAggregated.radiotherapiesMetastatic,
        radiotherapiesAggregated.radiotherapiesMetastaticDates,

        firstSystemicTreatment.systemicTreatmentPlan,
        firstSystemicTreatment.firstTreatmentStart,
        firstSystemicTreatment.numberOfCycles,
        (firstSystemicTreatment.treatmentStop - firstSystemicTreatment.firstTreatmentStart) AS systemicTreatmentPlanDuration,
        (survivalMeasurement.daysSinceDiagnosis  - metastasisAggregated.earliestMetastasisDetectionDays) AS observedOsFromMetastasisDetectionDays

   FROM patient
        INNER JOIN tumor on patient.id = tumor.patientId
        INNER JOIN survivalMeasurement on tumor.id = survivalMeasurement.tumorId
        INNER JOIN primaryDiagnosis on tumor.id = primaryDiagnosis.tumorId
        INNER JOIN metastaticDiagnosis on tumor.id = metastaticDiagnosis.tumorId
        LEFT JOIN (
            SELECT tumorId, count(id) as numberOfPriorTumors FROM priorTumor GROUP BY tumorId
        ) priorTumorOverview on tumor.id = priorTumorOverview.tumorId
        LEFT JOIN comorbidityAssessment on tumor.id = comorbidityAssessment.tumorId and comorbidityAssessment.daysSinceDiagnosis = 0
        LEFT JOIN molecularResult on tumor.id = molecularResult.tumorId and molecularResult.daysSinceDiagnosis = 0

        -- Metastasis aggregator
        LEFT JOIN(
            SELECT
                metastaticDiagnosis.tumorId,
                GROUP_CONCAT(metastasis.Location ORDER BY metastasis.daysSinceDiagnosis) AS metastasisLocationGroups,
                GROUP_CONCAT(metastasis.daysSinceDiagnosis ORDER BY metastasis.daysSinceDiagnosis) AS metastasisLocationGroupsDates,
                MIN(metastasis.daysSinceDiagnosis) AS earliestMetastasisDetectionDays
            FROM metastaticDiagnosis
            INNER JOIN metastasis ON metastaticDiagnosis.id = metastasis.metastaticDiagnosisId
            GROUP BY metastaticDiagnosis.tumorId
        ) AS metastasisAggregated ON tumor.id = metastasisAggregated.tumorId

        -- WHO aggregator
        LEFT JOIN(
            SELECT
                whoAssessment.tumorId,
                GROUP_CONCAT(whoAssessment.whoStatus ORDER BY whoAssessment.daysSinceDiagnosis) AS allWhoAssessments,
                GROUP_CONCAT(whoAssessment.daysSinceDiagnosis ORDER BY whoAssessment.daysSinceDiagnosis) AS whoAssessmentsDates
            FROM whoAssessment GROUP BY whoAssessment.tumorId
        ) AS whoAggregated ON tumor.id = whoAggregated.tumorId

        -- ASA aggregator
        LEFT JOIN(
            SELECT
                asaAssessment.tumorId,
                GROUP_CONCAT(asaAssessment.asaClassification ORDER BY asaAssessment.daysSinceDiagnosis) AS allAsaAssessments,
                GROUP_CONCAT(asaAssessment.daysSinceDiagnosis ORDER BY asaAssessment.daysSinceDiagnosis) AS asaAssessmentsDates
            FROM asaAssessment GROUP BY asaAssessment.tumorId
        ) AS asaAggregated ON tumor.id = asaAggregated.tumorId

        -- Lab values: For each labvalue take the last recorded value before the first treatment start
        LEFT JOIN (
            SELECT
                labMeasurement.tumorId,
                MIN(labMeasurement.daysSinceDiagnosis) AS firstLabDate,
                GROUP_CONCAT(CASE WHEN labMeasurement.name = 'ALBUMINE' THEN labMeasurement.value END ORDER BY labMeasurement.daysSinceDiagnosis) AS albumine,
                GROUP_CONCAT(CASE WHEN labMeasurement.name = 'ALKALINE_PHOSPHATASE' THEN labMeasurement.value END ORDER BY labMeasurement.daysSinceDiagnosis) AS alkalinePhosphatase,
                GROUP_CONCAT(CASE WHEN labMeasurement.name = 'CARCINOEMBRYONIC_ANTIGEN' THEN labMeasurement.value END ORDER BY labMeasurement.daysSinceDiagnosis) AS carcinoEmbryonicAntigen,
                GROUP_CONCAT(CASE WHEN labMeasurement.name = 'LACTATE_DEHYDROGENASE' THEN labMeasurement.value END ORDER BY labMeasurement.daysSinceDiagnosis) AS lactateDehydrogenase,
                GROUP_CONCAT(CASE WHEN labMeasurement.name = 'LEUKOCYTES_ABSOLUTE' THEN labMeasurement.value END ORDER BY labMeasurement.daysSinceDiagnosis) AS leukocytesAbsolute,
                GROUP_CONCAT(CASE WHEN labMeasurement.name = 'NEUTROPHILS_ABSOLUTE' THEN labMeasurement.value END ORDER BY labMeasurement.daysSinceDiagnosis) AS neutrophilsAbsolute,
                GROUP_CONCAT(labMeasurement.daysSinceDiagnosis ORDER BY labMeasurement.daysSinceDiagnosis) AS labValuesDates
            FROM labMeasurement
            JOIN (
                SELECT
                    treatmentEpisode.tumorId,
                    MIN(systemicTreatment.daysBetweenDiagnosisAndStart) AS firstTreatmentStart
                FROM treatmentEpisode
                JOIN systemicTreatment
                    ON treatmentEpisode.id = systemicTreatment.treatmentEpisodeId
                GROUP BY treatmentEpisode.tumorId
            ) AS TreatmentStart ON TreatmentStart.tumorId = labMeasurement.tumorId
            WHERE labMeasurement.daysSinceDiagnosis < TreatmentStart.firstTreatmentStart
            GROUP BY labMeasurement.tumorId
        ) AS labValuesBeforeTreatment
            ON tumor.id = labValuesBeforeTreatment.tumorId

        -- Surgeries aggregator
        LEFT JOIN(
            SELECT
                treatmentEpisode.tumorId,
                GROUP_CONCAT(primarySurgery.type ORDER BY primarySurgery.daysSinceDiagnosis) AS surgeriesPrimary,
                GROUP_CONCAT(primarySurgery.daysSinceDiagnosis ORDER BY primarySurgery.daysSinceDiagnosis) AS surgeriesPrimaryDates,

                GROUP_CONCAT(metastaticSurgery.type ORDER BY metastaticSurgery.daysSinceDiagnosis) AS surgeriesMetastatic,
                GROUP_CONCAT(metastaticSurgery.daysSinceDiagnosis ORDER BY metastaticSurgery.daysSinceDiagnosis) AS surgeriesMetastaticDates,

                GROUP_CONCAT(gastroenterologyResection.resectionType ORDER BY gastroenterologyResection.daysSinceDiagnosis) AS surgeriesGastroenterology,
                GROUP_CONCAT(gastroenterologyResection.daysSinceDiagnosis ORDER BY gastroenterologyResection.daysSinceDiagnosis) AS surgeriesGastroenterologyDates,

                MAX(CASE WHEN hipecTreatment.id IS NOT NULL THEN 1 ELSE 0 END) AS hadHipec,
                MIN(hipecTreatment.daysSinceDiagnosis) AS hadHipecDate
            FROM treatmentEpisode
            LEFT JOIN primarySurgery ON treatmentEpisode.id = primarySurgery.treatmentEpisodeId
            LEFT JOIN metastaticSurgery ON treatmentEpisode.id = metastaticSurgery.treatmentEpisodeId
            LEFT JOIN gastroenterologyResection ON treatmentEpisode.id = gastroenterologyResection.treatmentEpisodeId
            LEFT JOIN hipecTreatment ON treatmentEpisode.id = hipecTreatment.treatmentEpisodeId
            GROUP BY treatmentEpisode.tumorId
            ) AS surgeriesAggregated ON tumor.id = surgeriesAggregated.tumorId

        -- radiotherapies aggregator
        LEFT JOIN(
            SELECT
                treatmentEpisode.tumorId,
                GROUP_CONCAT(primaryRadiotherapy.type ORDER BY primaryRadiotherapy.daysBetweenDiagnosisAndStart) AS radiotherapiesPrimary,
                GROUP_CONCAT(primaryRadiotherapy.daysBetweenDiagnosisAndStart ORDER BY primaryRadiotherapy.daysBetweenDiagnosisAndStart) AS radiotherapiesPrimaryDates,

                GROUP_CONCAT(metastaticRadiotherapy.type ORDER BY metastaticRadiotherapy.daysBetweenDiagnosisAndStart) AS radiotherapiesMetastatic,
                GROUP_CONCAT(metastaticRadiotherapy.daysBetweenDiagnosisAndStart ORDER BY metastaticRadiotherapy.daysBetweenDiagnosisAndStart) AS radiotherapiesMetastaticDates
            FROM treatmentEpisode
            LEFT JOIN primaryRadiotherapy ON treatmentEpisode.id = primaryRadiotherapy.treatmentEpisodeId
            LEFT JOIN metastaticRadiotherapy ON treatmentEpisode.id = metastaticRadiotherapy.treatmentEpisodeId
            GROUP BY treatmentEpisode.tumorId
        ) AS radiotherapiesAggregated ON tumor.id = radiotherapiesAggregated.TumorId

        -- first systemic treatment
        LEFT JOIN (
            SELECT
                treatmentEpisode.tumorId,
                MIN(systemicTreatment.daysBetweenDiagnosisAndStart) AS firstTreatmentStart,
                GROUP_CONCAT(systemicTreatment.treatment ORDER BY systemicTreatment.daysBetweenDiagnosisAndStart) AS systemicTreatmentPlan,
                GROUP_CONCAT(systemicTreatmentDrug.numberOfCycles ORDER BY systemicTreatment.daysBetweenDiagnosisAndStart) AS numberOfCycles,
                GROUP_CONCAT(systemicTreatmentDrug.daysBetweenDiagnosisAndStop ORDER BY systemicTreatment.daysBetweenDiagnosisAndStart) AS treatmentStop

            FROM treatmentEpisode
            JOIN systemicTreatment ON treatmentEpisode.id = systemicTreatment.treatmentEpisodeId
            JOIN systemicTreatmentScheme ON systemicTreatment.id = systemicTreatmentScheme.systemicTreatmentId
            JOIN systemicTreatmentDrug ON systemicTreatmentScheme.id = systemicTreatmentDrug.systemicTreatmentSchemeId
            GROUP BY treatmentEpisode.tumorId
        ) firstSystemicTreatment ON tumor.id = firstSystemicTreatment.tumorId

);

CREATE OR REPLACE VIEW palliativeIntents AS (

SELECT *
FROM distantMetastasesOverview
WHERE metastaticPresence = 'AT_START'
   AND (clinicalTnmM LIKE 'M1%' OR pathologicalTnmM LIKE 'M1%' OR clinicalTumorStage LIKE 'IV%' OR pathologicalTumorStage LIKE 'IV%')
   AND surgeriesPrimary IS NULL
   AND surgeriesMetastatic IS NULL
   AND surgeriesGastroenterology IS NULL
   AND hadHipec IS NULL
   AND radiotherapiesPrimary IS NULL
   AND radiotherapiesMetastatic IS NULL
);

CREATE OR REPLACE VIEW knownPalliativeTreatments AS (

SELECT *
FROM palliativeIntents
WHERE systemicTreatmentPlan IS NOT NULL AND systemicTreatmentPlan != 'OTHER'
);