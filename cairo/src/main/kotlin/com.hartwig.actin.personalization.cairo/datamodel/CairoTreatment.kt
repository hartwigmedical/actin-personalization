package com.hartwig.actin.personalization.cairo.datamodel

import java.util.Date

data class CairoTreatment(
    val treatmentArm: String,

    val dateStartTreatment: Date? = null,
    val dateStartTreatmentCycleSix: Date? = null,
    val dateEndTreatment: Date? = null,

    val dateEndTreatmentOxaliplatin: Date? = null,
    val dateEndTreatmentBevacizumab: Date? = null,
    val dateEndTreatmentCapecitabine: Date? = null,

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
// TODO: for CAIRO3 calculate dateEndTreatment (= last end date)
function dateEndTreatment() {
    return max(this.dateEndTreatmentOxaliplatin || this.dateEndTreatmentBevacizumab || this.dateEndTreatmentCapecitabine);
}
