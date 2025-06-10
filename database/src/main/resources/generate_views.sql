CREATE OR REPLACE VIEW palliativeReference AS (

    SELECT *
    FROM reference
    WHERE hasHadSystemicTreatmentPriorToMetastaticTreatment= 0
        AND isMetastaticPriorToMetastaticTreatmentDecision = 1
        AND (clinicalTnmM LIKE 'M1%' OR pathologicalTnmM LIKE 'M1%' OR clinicalTumorStage LIKE 'IV%' OR pathologicalTumorStage LIKE 'IV%')
        AND hasHadPrimarySurgeryDuringMetastaticTreatment = 0
        AND hasHadPrimaryRadiotherapyDuringMetastaticTreatment = 0
        AND hasHadGastroenterologySurgeryDuringMetastaticTreatment = 0
        AND hasHadHipecDuringMetastaticTreatment = 0
        AND hasHadMetastaticSurgery = 0
        AND hasHadMetastaticRadiotherapy = 0
        AND (systemicTreatmentsAfterMetastaticDiagnosis = 0 OR firstSystemicTreatmentAfterMetastaticDiagnosis IS NOT NULL)
);

CREATE OR REPLACE VIEW knownPalliativeTreatedReference AS (

    SELECT *
    FROM palliativeReference
    WHERE firstSystemicTreatmentAfterMetastaticDiagnosis IS NOT NULL AND firstSystemicTreatmentAfterMetastaticDiagnosis != 'OTHER'
);