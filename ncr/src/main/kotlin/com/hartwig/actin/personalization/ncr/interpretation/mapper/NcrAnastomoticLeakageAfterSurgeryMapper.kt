package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.treatment.AnastomoticLeakageAfterSurgery

object NcrAnastomoticLeakageAfterSurgeryMapper : NcrIntCodeMapper<AnastomoticLeakageAfterSurgery?> {

    override fun resolve(code: Int): AnastomoticLeakageAfterSurgery? {
        return when (code) {
            0 -> AnastomoticLeakageAfterSurgery.ABSENT
            1 -> AnastomoticLeakageAfterSurgery.ANASTOMOTIC_LEAKAGE_BUT_NO_ABSCESS
            2 -> AnastomoticLeakageAfterSurgery.ABSCESS_BUT_NO_ANASTOMOTIC_LEAKAGE
            3 -> AnastomoticLeakageAfterSurgery.COMBINATION_OF_ANASTOMOTIC_LEAKAGE_AND_ABSCESS
            8 -> AnastomoticLeakageAfterSurgery.NA
            9 -> null
            else -> throw IllegalArgumentException("Unknown AnastomoticLeakageAfterSurgery code: $code")
        }
    }
}
