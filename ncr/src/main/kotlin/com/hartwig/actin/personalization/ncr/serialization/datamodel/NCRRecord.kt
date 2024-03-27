package com.hartwig.actin.personalization.ncr.serialization.datamodel

data class NCRRecord(
    val identification : NCRIdentification,
    val patientCharacteristics : NCRPatientCharacteristics,
    val clinicalCharacteristics : NCRClinicalCharacteristics,
    val molecularCharacteristics : NCRMolecularCharacteristics,
    val priorMalignancies: NCRPriorMalignancies,
    val primaryDiagnosis: NCRPrimaryDiagnosis,
    val metastaticDiagnosis: NCRMetastaticDiagnosis,
    val comorbidities: NCRCharlsonComorbidities,
    val labValues: NCRLabValues,
    val treatment : NCRTreatment,
    val treatmentResponse: NCRTreatmentResponse
)
