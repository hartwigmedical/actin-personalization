CREATE OR REPLACE VIEW metastaticTreatmentEpisode AS (

    SELECT * FROM treatmentEpisode WHERE metastaticPresence IN ('AT_START', 'AT_PROGRESSION')
);

CREATE OR REPLACE VIEW distantMetastasesOverview AS (

    SELECT
        patient.sourceId,
        patient.sex,
        tumor.ageAtDiagnosis,
        survivalMeasurement.isAlive,
        survivalMeasurement.isAlive = 0 as hadSurvivalEvent,
        survivalMeasurement.daysSinceDiagnosis,
        primaryDiagnosis.primaryTumorType,
        primaryDiagnosis.primaryTumorLocation,
        primaryDiagnosis.sidedness,
        primaryDiagnosis.presentedWithIleus,
        primaryDiagnosis.presentedWithPerforation,
        primaryDiagnosis.anorectalVergeDistanceCategory,
        not(isnull(numberOfPriorTumors)) AS hasHadPriorTumor,
        metastaticDiagnosis.isMetachronous,
        comorbidityAssessment.charlsonComorbidityIndex,
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
        molecularResult.hasKrasG12CMutation
    --    surgeryOverview.surgeries,
    --    metastasisOverview.metastasisLocationGroupsPriorToSystemicTreatment,
    --    episode.intervalTumorIncidenceTreatmentPlanStopDays - episode.intervalTumorIncidenceTreatmentPlanStartDays AS systemicTreatmentPlanDuration
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

    --    INNER JOIN episode ON diagnosis.id = episode.diagnosisId AND episode.order = diagnosis.orderOfFirstDistantMetastasesEpisode
    --    LEFT JOIN (
    --        SELECT episodeId, GROUP_CONCAT(type) AS surgeries FROM surgery GROUP BY episodeId
    --    ) surgeryOverview ON episode.id = surgeryOverview.episodeId
    --    LEFT JOIN (
    --        SELECT episodeId, GROUP_CONCAT(DISTINCT locationGroup ORDER BY locationGroup) AS metastasisLocationGroupsPriorToSystemicTreatment
    --        FROM metastasis INNER JOIN episode ON metastasis.episodeId = episode.id
    --        WHERE intervalTumorIncidenceMetastasisDetectionDays < intervalTumorIncidenceTreatmentPlanStartDays
    --        GROUP BY episodeId
    --    ) metastasisOverview ON episode.id = metastasisOverview.episodeId
);
--
--CREATE OR REPLACE VIEW palliativeIntents AS (
--
--SELECT *
--FROM distantMetastasesOverview
--WHERE distantMetastasesDetectionStatus = 'AT_START'
--    AND (tnmCM LIKE 'M1%' OR tnmPM LIKE 'M1%' OR stageTNM LIKE 'IV%')
--    AND hasHadPreSurgerySystemicChemotherapy = 0
--    AND hasHadPostSurgerySystemicChemotherapy = 0
--    AND hasHadPreSurgerySystemicTargetedTherapy = 0
--    AND hasHadPostSurgerySystemicTargetedTherapy = 0
--    AND surgeries IS NULL
--    AND gastroenterologyResections = JSON_ARRAY()
--    AND metastasesSurgeries = JSON_ARRAY()
--    AND radiotherapies = JSON_ARRAY()
--    AND metastasesRadiotherapies = JSON_ARRAY()
--    AND hasHadHipecTreatment = 0
--    AND (hasReceivedTumorDirectedTreatment = 0 OR systemicTreatmentPlan IS NOT NULL)
--);
--
--CREATE OR REPLACE VIEW knownPalliativeTreatments AS (
--
--SELECT *
--FROM palliativeIntents
--WHERE systemicTreatmentPlan IS NOT NULL AND systemicTreatmentPlan != 'OTHER'
--);