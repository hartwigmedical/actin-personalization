package com.hartwig.actin.personalization.ncr.interpretation.conversion

object MetastaticRtIntervalConversion {

    fun convert(metastaticRtIntervalCode: String?) : Int? {
        return metastaticRtIntervalCode?.takeIf { it != "." }?.toInt()
    }
}