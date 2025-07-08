package com.hartwig.actin.personalization.cairo.datamodel

import java.util.Date

data class CairoResponseAssessment(
    val protocolDiscontinuationReason: Int? = null,
    val protocolDiscontinuationReasonSpecification: String? = null,

    val reasonStopObservationTreatment: Int? = null,
    val reasonStopObservationTreatmentSpecification: String? = null,

    val responseAssessmentDateBeforeTreatment: Date? = null,
    val responseAssessmentDateAfterThreeCycles: Date? = null,
    val responseAssessmentSiteAfterThreeCycles: Int? = null,
    val responseAssessmentDateAfterSixCycles: Date? = null,
    val responseAssessmentSiteAfterSixCycles: Int? = null,

    val crDateFirstDocumented: Date? = null,
    val crDateConfirmed: Date? = null,
    val prDateFirstDocumented: Date? = null,
    val prDateConfirmed: Date? = null,
    val sdDateFirstDocumented: Date? = null,
    val progressionDateFirstDocumented: Date? = null,
)
