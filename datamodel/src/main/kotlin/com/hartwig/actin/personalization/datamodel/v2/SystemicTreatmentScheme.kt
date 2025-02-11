package com.hartwig.actin.personalization.datamodel.v2

data class SystemicTreatmentScheme (
    val schemeNumber: Int?,
    val treatmentComponents: List<SystemicTreatmentSchemeDrug>,
    val minDaysBetweenDiagnosisAndStart: Int?,
    val maxDaysBetweenDiagnosisAndStart: Int?,
    val minDaysBetweenDiagnosisAndStop: Int?,
    val maxDaysBetweenDiagnosisAndStop: Int?
)
