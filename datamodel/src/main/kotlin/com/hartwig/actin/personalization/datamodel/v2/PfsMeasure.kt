package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.PfsMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.PfsMeasureType

data class PfsMeasure (
    val daysSinceDiagnosis: Int?,
    val type: PfsMeasureType,
    val followUpEvent: PfsMeasureFollowUpEvent?
)