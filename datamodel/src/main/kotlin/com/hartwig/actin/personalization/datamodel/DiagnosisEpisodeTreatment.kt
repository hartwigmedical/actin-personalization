package com.hartwig.actin.personalization.datamodel

data class DiagnosisEpisodeTreatment(
    val diagnosis: Diagnosis,
    val episode: Episode,
    val systemicTreatmentPlan: SystemicTreatmentPlan? = null
)
