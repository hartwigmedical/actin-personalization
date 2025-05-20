CREATE OR REPLACE VIEW palliativeReference AS (

    SELECT *
    FROM reference
    WHERE hasHadSystemicTreatmentPriorToMetastaticDiagnosis = 0 
        AND (clinicalTnmM LIKE 'M1%' OR pathologicalTnmM LIKE 'M1%' OR clinicalTumorStage LIKE 'IV%' OR pathologicalTumorStage LIKE 'IV%')
        AND hasHadPrimarySurgeryAfterMetastaticDiagnosis = 0
        AND hasHadGastroenterologySurgeryAfterMetastaticDiagnosis = 0
        AND hasHadHipecAfterMetastaticDiagnosis = 0 
        AND hasHadPrimaryRadiotherapyAfterMetastaticDiagnosis = 0
        AND hasHadMetastaticSurgery = 0
        AND hasHadMetastaticRadiotherapy = 0
);

CREATE OR REPLACE VIEW knownPalliativeTreatedReference AS (

    SELECT *
    FROM palliativeReference
    WHERE firstSystemicTreatmentAfterMetastaticDiagnosis IS NOT NULL AND firstSystemicTreatmentAfterMetastaticDiagnosis != 'OTHER' 
);