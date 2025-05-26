package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence

object NcrMetastaticPresenceMapper : NcrIntCodeMapper<MetastaticPresence>{

    override fun resolve(code: Int): MetastaticPresence {
        return when (code) {
            0 -> MetastaticPresence.ABSENT
            1 -> MetastaticPresence.AT_START
            2 -> MetastaticPresence.AT_PROGRESSION
            else -> throw IllegalArgumentException("Unknown DistantMetastasesStatus code: $code")
        }
    }
}