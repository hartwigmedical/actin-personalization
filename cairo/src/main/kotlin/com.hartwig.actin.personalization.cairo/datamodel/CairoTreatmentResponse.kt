package com.hartwig.actin.personalization.cairo.datamodel

import java.util.Date

data class CairoTreatmentResponse(
    val pfsDate: Date? = null,
    val pfsMonths: Double? = null,
    val pfsEvent: Int? = null,
    val ProgressionMethod: Int? = null,
    val ProgressionCtDate: Date? = null,

    val osDate: Date? = null,
    val osMonths: Double? = null,
    val osEvent: Int? = null,
    val deathCause: Int? = null,
    val deathCauseSpecification: String? = null,

    val nextTreatment: Int? = null,
    val nextTreatmentSpecification: String? = null,

    val bestOverallResponse: Int? = null,
    val bestOverallResponseSpecification: String? = null,
    val responseAssessment: CairoResponseAssessment //only in CAIRO 3
)
