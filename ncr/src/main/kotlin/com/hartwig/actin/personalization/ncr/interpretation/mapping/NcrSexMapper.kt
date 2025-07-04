package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.Sex

object NcrSexMapper : NcrIntCodeMapper<Sex> {

    override fun resolve(code: Int): Sex {
        return when (code) {
            1 -> Sex.MALE
            2 -> Sex.FEMALE
            else -> throw IllegalArgumentException("Unknown Sex code: $code")
        }
    }
}
