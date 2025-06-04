CREATE OR REPLACE VIEW palliativeReference AS (

    SELECT *
    FROM reference
    WHERE hasHadSystemicTreatmentPriorToMetastaticTreatment= 0
        AND isMetastaticPriorToMetastaticTreatmentDecision = 1
        AND (clinicalTnmM LIKE 'M1%' OR pathologicalTnmM LIKE 'M1%' OR clinicalTumorStage LIKE 'IV%' OR pathologicalTumorStage LIKE 'IV%')
        AND hasHadPrimarySurgeryPriorToMetastaticTreatment = 0
        AND hasHadPrimarySurgeryDuringMetastaticTreatment = 0
        AND hasHadGastroenterologySurgeryPriorToMetastaticTreatment = 0
        AND hasHadGastroenterologySurgeryDuringMetastaticTreatment = 0
        AND hasHadHipecPriorToMetastaticTreatment = 0
        AND hasHadHipecDuringMetastaticTreatment = 0
        AND hasHadPrimaryRadiotherapyPriorToMetastaticTreatment = 0
        AND hasHadPrimaryRadiotherapyDuringMetastaticTreatment = 0
        AND hasHadMetastaticSurgery = 0
        AND hasHadMetastaticRadiotherapy = 0
);

CREATE OR REPLACE VIEW knownPalliativeTreatedReference AS (

    SELECT *
    FROM palliativeReference
     WHERE firstSystemicTreatmentAfterMetastaticDiagnosis IS NOT NULL AND firstSystemicTreatmentAfterMetastaticDiagnosis != 'OTHER'
);