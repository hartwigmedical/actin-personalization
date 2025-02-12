package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.treatment.TreatmentIntent

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

fun resolveCyclesAndDetails(code: Int?): Triple<Int?, TreatmentIntent?, Boolean?> {
    return when (code) {
        in 0..48, 60 -> Triple(code, null, false)
        66 -> Triple(null, TreatmentIntent.SENSITIZER, null)
        77 -> Triple(null, TreatmentIntent.MAINTENANCE, null)
        98 -> Triple(null, null, true)
        99, null -> Triple(null, null, null)
        else -> throw IllegalArgumentException("Unknown CyclesAndDetails code: $code")
    }
}
