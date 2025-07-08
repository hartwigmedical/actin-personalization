package com.hartwig.actin.personalization.cairo.datamodel

data class CairoRecord(
    val identification : CairoIdentification,
    val patientCharacteristics : CairoPatientCharacteristics,
    val molecularCharacteristics : CairoMolecularCharacteristics,
    val primaryDiagnosis: CairoPrimaryDiagnosis,
    val metastaticDiagnosis: CairoMetastaticDiagnosis,
    val labValues: CairoLabValues,
    val treatment : CairoTreatment,
    val treatmentResponse: CairoTreatmentResponse
)
