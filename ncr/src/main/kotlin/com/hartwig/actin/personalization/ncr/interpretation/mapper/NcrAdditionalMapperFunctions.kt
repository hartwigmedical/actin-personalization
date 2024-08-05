package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.TreatmentCyclesDetails

fun resolvePreAndPostSurgery(code: Int?): Pair<Boolean, Boolean> {
    return when (code) {
        0, 4 -> Pair(false, false)
        1 -> Pair(true, false)
        2 -> Pair(false, true)
        3 -> Pair(true, true)
        null -> Pair(false, false)
        else -> throw IllegalArgumentException("Unknown PreAndPostSurgery code: $code")
    }
}

fun resolveCyclesAndDetails(code: Int?): Pair<Int?, TreatmentCyclesDetails?> {
    return when (code) {
        in 0..48, 60 -> Pair(code, null)
        66 -> Pair(null, TreatmentCyclesDetails.SENSITIZER)
        77 -> Pair(null, TreatmentCyclesDetails.MAINTENANCE)
        98 -> Pair(null, TreatmentCyclesDetails.ONGOING_TREATMENT)
        99, null -> Pair(null, null)
        else -> throw IllegalArgumentException("Unknown CyclesAndDetails code: $code")
    }
}
