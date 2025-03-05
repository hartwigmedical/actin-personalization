package com.hartwig.actin.personalization.cairo.datamodel

import java.util.Date

data class CairoTreatment(
    val treatmentArm: String,

    val dateStartTreatment: Date? = null,
    val dateStartTreatmentCycleSix: Date? = null,
    val dateEndTreatment: CairoTreatmentEnd,

    val treatmentCyclesTotal: Int? = null,
    val treatmentCyclesBevacizumab: Int? = null,
    val treatmentCyclesMaintenance: Int? = null,

    val treatmentChanges: CairoTreatmentChanges? = null,
    val commentsTreatmentReintroduction: String? = null,

    val metastaticSurgery: CairoMetastaticSurgery,
    val primarySurgery: CairoPrimarySurgery,
    val adjuvantChemo: CairoAdjuvantChemo,
    val adjuvantRadio: CairoRadiotherapy,
)
