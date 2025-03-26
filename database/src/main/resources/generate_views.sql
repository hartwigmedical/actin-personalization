CREATE OR REPLACE VIEW palliativeIntents AS (

SELECT *
FROM distantMetastasesOverview
WHERE metastasisBeforeTreatment = 1
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