CREATE OR REPLACE VIEW diagnosisTreatments
AS (
SELECT
    ncrId,
    d.patientId,
    e.diagnosisId,
    e.order,
    consolidatedTumorType,
    tumorLocations,
    ageAtDiagnosis,
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
    systemicTreatmentPlan,
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
WHERE e.order=1
);