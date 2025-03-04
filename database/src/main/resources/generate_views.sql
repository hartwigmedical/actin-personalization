CREATE OR REPLACE VIEW distantMetastasesOverview AS (

SELECT
    patient.ncrId,
    patient.sex,
    patient.isAlive,
    diagnosis.patientId,
    diagnosis.consolidatedTumorType,
    diagnosis.tumorLocations,
    diagnosis.sidedness,
    diagnosis.ageAtDiagnosis,
    diagnosis.observedOsFromTumorIncidenceDays,
    diagnosis.hadSurvivalEvent,
    diagnosis.hasHadPriorTumor,
    diagnosis.isMetachronous,
    diagnosis.cci,
    diagnosis.cciNumberOfCategories,
    diagnosis.cciHasAids,
    diagnosis.cciHasCongestiveHeartFailure,
    diagnosis.cciHasCollagenosis,
    diagnosis.cciHasCopd,
    diagnosis.cciHasCerebrovascularDisease,
    diagnosis.cciHasDementia,
    diagnosis.cciHasDiabetesMellitus,
    diagnosis.cciHasDiabetesMellitusWithEndOrganDamage,
    diagnosis.cciHasOtherMalignancy,
    diagnosis.cciHasOtherMetastaticSolidTumor,
    diagnosis.cciHasMyocardialInfarct,
    diagnosis.cciHasMildLiverDisease,
    diagnosis.cciHasHemiplegiaOrParaplegia,
    diagnosis.cciHasPeripheralVascularDisease,
    diagnosis.cciHasRenalDisease,
    diagnosis.cciHasLiverDisease,
    diagnosis.cciHasUlcerDisease,
    diagnosis.presentedWithIleus,
    diagnosis.presentedWithPerforation,
    diagnosis.anorectalVergeDistanceCategory,
    diagnosis.hasMsi,
    diagnosis.hasBrafMutation,
    diagnosis.hasBrafV600EMutation,
    diagnosis.hasRasMutation,
    diagnosis.hasKrasG12CMutation,
    episode.id as episodeId,
    episode.*,
    surgeryOverview.surgeries,
    metastasisOverview.metastasisLocationGroupsPriorToSystemicTreatment,
    episode.intervalTumorIncidenceTreatmentPlanStopDays - episode.intervalTumorIncidenceTreatmentPlanStartDays AS systemicTreatmentPlanDuration
    diagnosis.observedOsFromTumorIncidenceDays - metastasisOverview.intervalTumorIncidenceMetastasisDetectionDays AS observedOsFromMetastasisDetectionDays
FROM patient
    INNER JOIN diagnosis ON patient.id = diagnosis.patientId
    INNER JOIN episode ON diagnosis.id = episode.diagnosisId AND episode.order = diagnosis.orderOfFirstDistantMetastasesEpisode
    LEFT JOIN (
        SELECT episodeId, GROUP_CONCAT(type) AS surgeries FROM surgery GROUP BY episodeId
    ) surgeryOverview ON episode.id = surgeryOverview.episodeId
    LEFT JOIN (
        SELECT episodeId, 
               intervalTumorIncidenceMetastasisDetectionDays, 
               GROUP_CONCAT(DISTINCT locationGroup ORDER BY locationGroup) AS metastasisLocationGroupsPriorToSystemicTreatment
        FROM metastasis INNER JOIN episode ON metastasis.episodeId = episode.id
        WHERE (episode.intervalTumorIncidenceTreatmentPlanStartDays IS NULL OR(intervalTumorIncidenceMetastasisDetectionDays < intervalTumorIncidenceTreatmentPlanStartDays))
        GROUP BY episodeId
    ) metastasisOverview ON episode.id = metastasisOverview.episodeId
);

CREATE OR REPLACE VIEW palliativeIntents AS (

SELECT *
FROM distantMetastasesOverview
WHERE distantMetastasesDetectionStatus = 'AT_START'
    AND (tnmCM LIKE 'M1%' OR tnmPM LIKE 'M1%' OR stageTNM LIKE 'IV%')
    AND hasHadPreSurgerySystemicChemotherapy = 0
    AND hasHadPostSurgerySystemicChemotherapy = 0
    AND hasHadPreSurgerySystemicTargetedTherapy = 0
    AND hasHadPostSurgerySystemicTargetedTherapy = 0
    AND surgeries IS NULL
    AND gastroenterologyResections = JSON_ARRAY()
    AND metastasesSurgeries = JSON_ARRAY()
    AND radiotherapies = JSON_ARRAY()
    AND metastasesRadiotherapies = JSON_ARRAY()
    AND hasHadHipecTreatment = 0
    AND (hasReceivedTumorDirectedTreatment = 0 OR systemicTreatmentPlan IS NOT NULL)
);

CREATE OR REPLACE VIEW knownPalliativeTreatments AS (

SELECT *
FROM palliativeIntents
WHERE systemicTreatmentPlan IS NOT NULL AND systemicTreatmentPlan != 'OTHER'
);
