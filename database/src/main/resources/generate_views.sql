CREATE OR REPLACE VIEW diagnosisTreatments
AS (
SELECT
    ncrId,
    d.patientId,
    e.diagnosisId,
    e.id AS episodeId,
    e.order,
    distantMetastasesDetectionStatus,
    consolidatedTumorType,
    tumorLocations,
    ageAtDiagnosis,
    whoStatusPreTreatmentStart,
    hasHadPriorTumor,
    hasMsi,
    hasBrafMutation,
    hasBrafV600EMutation,
    hasRasMutation,
    hasKrasG12CMutation,
    hasReceivedTumorDirectedTreatment,
    reasonRefrainmentFromTumorDirectedTreatment,
    gastroenterologyResections,
	surgeries,
    metastasesSurgeries,
    radiotherapies,
    metastasesRadiotherapies,
    hasHadHipecTreatment,
    hasHadPreSurgerySystemicChemotherapy,
    hasHadPostSurgerySystemicChemotherapy,
    hasHadPreSurgerySystemicTargetedTherapy,
    hasHadPostSurgerySystemicTargetedTherapy,
	metastasisLocationGroupsPriorToSystemicTreatment,
    systemicTreatmentPlan,
    intervalTumorIncidenceTreatmentPlanStartDays,
    intervalTumorIncidenceTreatmentPlanStopDays,
    intervalTumorIncidenceTreatmentPlanStopDays-intervalTumorIncidenceTreatmentPlanStartDays AS systemicTreatmentPlanDuration,
    observedPfsDays AS systemicTreatmentPlanPfs,
    response,
    intervalTreatmentPlanStartResponseDays
FROM
    patient p
        INNER JOIN
    diagnosis d ON p.id = d.patientId
        INNER JOIN
    episode e ON d.id = e.diagnosisId
        LEFT JOIN
    (SELECT episodeId, GROUP_CONCAT(type) AS surgeries FROM surgery GROUP BY 1) AS s ON e.id = s.episodeId
		LEFT JOIN
	(SELECT episodeId, GROUP_CONCAT(locationGroup ORDER BY locationGroup) AS metastasisLocationGroupsPriorToSystemicTreatment FROM metastasis mm INNER JOIN episode ee ON mm.episodeId=ee.id WHERE intervalTumorIncidenceMetastasisDetectionDays < intervalTumorIncidenceTreatmentPlanStartDays GROUP BY 1) AS m ON e.id=m.episodeId
WHERE e.order=1
);