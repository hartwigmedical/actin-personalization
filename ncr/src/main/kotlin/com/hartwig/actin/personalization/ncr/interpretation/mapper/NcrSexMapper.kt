package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.v2.Sex

object NcrSexMapper : NcrIntCodeMapper<Sex> {

    override fun resolve(code: Int): Sex {
        return when (code) {
            1 -> Sex.MALE
            2 -> Sex.FEMALE
            else -> throw IllegalArgumentException("Unknown Sex code: $code")
        }
    }
}
