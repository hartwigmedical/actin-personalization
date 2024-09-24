CREATE OR REPLACE VIEW diagnosisTreatments
AS (
SELECT
    p.ncrId,
    p.sex,
    p.isAlive,
    d.patientId,
    d.consolidatedTumorType,
    d.tumorLocations,
    d.ageAtDiagnosis,
    d.observedOverallSurvivalFromTumorIncidenceDays,
    d.hadSurvivalEvent,
    d.hasHadPriorTumor,
    d.cci,
    d.cciNumberOfCategories,
    d.cciHasAids,
    d.cciHasCongestiveHeartFailure,
    d.cciHasCollagenosis,
    d.cciHasCopd,
    d.cciHasCerebrovascularDisease,
    d.cciHasDementia,
    d.cciHasDiabetesMellitus,
    d.cciHasDiabetesMellitusWithEndOrganDamage,
    d.cciHasOtherMalignancy,
    d.cciHasOtherMetastaticSolidTumor,
    d.cciHasMyocardialInfarct,
    d.cciHasMildLiverDisease,
    d.cciHasHemiplegiaOrParaplegia,
    d.cciHasPeripheralVascularDisease,
    d.cciHasRenalDisease,
    d.cciHasLiverDisease,
    d.cciHasUlcerDisease,
    d.presentedWithIleus,
    d.presentedWithPerforation,
    d.anorectalVergeDistanceCategory,
    d.hasMsi,
    d.hasBrafMutation,
    d.hasBrafV600EMutation,
    d.hasRasMutation,
    d.hasKrasG12CMutation,
    e.*,
	surgeries,
	metastasisLocationGroupsPriorToSystemicTreatment,
    intervalTumorIncidenceTreatmentPlanStopDays-intervalTumorIncidenceTreatmentPlanStartDays AS systemicTreatmentPlanDuration
FROM patient p
JOIN diagnosis d ON p.id = d.patientId
JOIN episode e ON d.id = e.diagnosisId AND e.order=1
LEFT JOIN (
    SELECT episodeId, GROUP_CONCAT(type) AS surgeries FROM surgery GROUP BY id
) s ON e.id = s.episodeId
LEFT JOIN (
    SELECT episodeId, GROUP_CONCAT(locationGroup ORDER BY locationGroup) AS metastasisLocationGroupsPriorToSystemicTreatment
    FROM metastasis mm
    JOIN episode ee ON mm.episodeId=ee.id
    WHERE intervalTumorIncidenceMetastasisDetectionDays < intervalTumorIncidenceTreatmentPlanStartDays
    GROUP BY episodeId
) m ON e.id=m.episodeId
);

CREATE OR REPLACE VIEW filteredTreatments
AS (
SELECT *
FROM diagnosisTreatments
WHERE distantMetastasesDetectionStatus = 'AT_START'
AND hasHadPreSurgerySystemicChemotherapy = 0
AND hasHadPostSurgerySystemicChemotherapy = 0
AND hasHadPreSurgerySystemicTargetedTherapy = 0
AND hasHadPostSurgerySystemicTargetedTherapy = 0
AND surgeries IS NULL
);