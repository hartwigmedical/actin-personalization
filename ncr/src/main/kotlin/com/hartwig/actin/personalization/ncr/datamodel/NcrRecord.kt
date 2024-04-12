package com.hartwig.actin.personalization.ncr.datamodel

data class NcrRecord(
    val identification : NcrIdentification,
    val patientCharacteristics : NcrPatientCharacteristics,
    val clinicalCharacteristics : NcrClinicalCharacteristics,
    val molecularCharacteristics : NcrMolecularCharacteristics,
    val priorMalignancies: NcrPriorMalignancies,
    val primaryDiagnosis: NcrPrimaryDiagnosis,
    val metastaticDiagnosis: NcrMetastaticDiagnosis,
    val comorbidities: NcrCharlsonComorbidities,
    val labValues: NcrLabValues,
    val treatment : NcrTreatment,
    val treatmentResponse: NcrTreatmentResponse
)
