CREATE OR REPLACE VIEW diagnosisTreatments
AS (
SELECT
    ncrId,
    d.patientId,
    e.diagnosisId,
    e.id AS episodeId,
    e.order,
    distantMetastasesStatus,
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
    metastasesSurgeries,
    radiotherapies,
    metastasesRadiotherapies,
    hasHadHipecTreatment,
    surgeries,
	metastasisLocationGroupsPriorToSystemicTreatment,
    systemicTreatmentPlan,
    intervalTumorIncidenceTreatmentPlanStart,
    pfs AS systemicTreatmentPlanPfs,
    response,
    intervalTreatmentPlanStartResponseDate
FROM
    patient p
        INNER JOIN
    diagnosis d ON p.id = d.patientId
        INNER JOIN
    episode e ON d.id = e.diagnosisId
        LEFT JOIN
    (SELECT episodeId, GROUP_CONCAT(surgeryType) AS surgeries FROM surgery GROUP BY 1) AS s ON e.id = s.episodeId
		LEFT JOIN
	(SELECT episodeId, GROUP_CONCAT(locationGroup) AS metastasisLocationGroupsPriorToSystemicTreatment FROM metastasis mm INNER JOIN episode ee ON mm.episodeId=ee.id WHERE intervalTumorIncidenceMetastasisDetection < intervalTumorIncidenceTreatmentPlanStart GROUP BY 1) AS m ON e.id=m.episodeId
WHERE e.order=1
);