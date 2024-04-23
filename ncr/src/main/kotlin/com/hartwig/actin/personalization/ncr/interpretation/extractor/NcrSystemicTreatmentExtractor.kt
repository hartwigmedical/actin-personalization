package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.datamodel.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.ResponseMeasureType
import com.hartwig.actin.personalization.datamodel.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentScheme
import com.hartwig.actin.personalization.ncr.datamodel.NcrSystemicTreatment
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolveCyclesAndDetails
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolvePreAndPostSurgery
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolveTreatmentName
import kotlin.math.max
import kotlin.math.min

fun extractSystemicTreatmentSchemes(
    systemicTreatment: NcrSystemicTreatment, responseMeasure: ResponseMeasure?, pfsMeasures: List<PfsMeasure>
): List<SystemicTreatmentScheme> {
    val rawTreatmentSchemesDescending = extractRawSystemicTreatmentSchemesDescending(systemicTreatment)
    return augmentTreatmentSchemesWithResponseAndPfs(
        rawTreatmentSchemesDescending,
        responseMeasure?.responseMeasureType,
        responseMeasure?.intervalTumorIncidenceResponseDate ?: 0,
        pfsMeasures
    ).reversed()
}

private tailrec fun augmentTreatmentSchemesWithResponseAndPfs(
    rawTreatmentSchemes: List<SystemicTreatmentScheme>,
    response: ResponseMeasureType?,
    responseInterval: Int,
    pfsMeasures: List<PfsMeasure>,
    augmentedTreatmentSchemes: List<SystemicTreatmentScheme> = emptyList()
): List<SystemicTreatmentScheme> {
    return if (rawTreatmentSchemes.isEmpty()) {
        augmentedTreatmentSchemes
    } else {
        val current = rawTreatmentSchemes.first()
        val lineStartIntervalMin = current.intervalTumorIncidenceTreatmentLineStartMin

        val currentResponse = if (response != null && lineStartIntervalMin != null && lineStartIntervalMin < responseInterval) {
            ResponseMeasure(response, responseInterval)
        } else null

        val (treatmentRawPfs, remainingPfs) = pfsMeasures.partition {
            lineStartIntervalMin != null && lineStartIntervalMin < (it.intervalTumorIncidencePfsMeasureDate ?: 0)
        }

        val treatmentPfs = treatmentRawPfs.map(PfsMeasure::pfsMeasureType)
            .filterNot { it == PfsMeasureType.CENSOR }
            .maxOrNull()

        val augmentedTreatmentScheme = current.copy(
            treatmentResponse = currentResponse, treatmentPfs = treatmentPfs, treatmentRawPfs = treatmentRawPfs
        )

        augmentTreatmentSchemesWithResponseAndPfs(
            rawTreatmentSchemes.drop(1),
            if (currentResponse != null) null else response,
            responseInterval,
            remainingPfs,
            augmentedTreatmentSchemes + augmentedTreatmentScheme
        )
    }
}

private fun extractRawSystemicTreatmentSchemesDescending(
    ncrSystemicTreatment: NcrSystemicTreatment
): List<SystemicTreatmentScheme> {
    return with(ncrSystemicTreatment) {
        listOfNotNull(
            systCode1?.let { extractSystemic(it, systSchemanum1, systKuren1, systStartInt1, systStopInt1, systPrepost1) },
            systCode2?.let { extractSystemic(it, systSchemanum2, systKuren2, systStartInt2, systStopInt2, systPrepost2) },
            systCode3?.let { extractSystemic(it, systSchemanum3, systKuren3, systStartInt3, systStopInt3, systPrepost3) },
            systCode4?.let { extractSystemic(it, systSchemanum4, systKuren4, systStartInt4, systStopInt4, systPrepost4) },
            systCode5?.let { extractSystemic(it, systSchemanum5, systKuren5, systStartInt5, systStopInt5, systPrepost5) },
            systCode6?.let { extractSystemic(it, systSchemanum6, systKuren6, systStartInt6, systStopInt6, systPrepost6) },
            systCode7?.let { extractSystemic(it, systSchemanum7, systKuren7, systStartInt7, systStopInt7, systPrepost7) },
            systCode8?.let { extractSystemic(it, systSchemanum8, systKuren8, systStartInt8, systStopInt8, systPrepost8) },
            systCode9?.let { extractSystemic(it, systSchemanum9, systKuren9, systStartInt9, systStopInt9, systPrepost9) },
            systCode10?.let { extractSystemic(it, systSchemanum10, systKuren10, systStartInt10, systStopInt10, systPrepost10) },
            systCode11?.let { extractSystemic(it, systSchemanum11, systKuren11, systStartInt11, systStopInt11, systPrepost11) },
            systCode12?.let { extractSystemic(it, systSchemanum12, systKuren12, systStartInt12, systStopInt12, systPrepost12) },
            systCode13?.let { extractSystemic(it, systSchemanum13, systKuren13, systStartInt13, systStopInt13, systPrepost13) },
            systCode14?.let { extractSystemic(it, systSchemanum14, systKuren14, systStartInt14, systStopInt14, systPrepost14) }
        )
            .groupBy(SystemicTreatment::treatmentSchemeNumber)
            .map { (_, treatments) ->
                val (startMin, startMax, stopMin, stopMax) = treatments.map {
                    with(it) {
                        StartAndStopMinAndMax(
                            intervalTumorIncidenceTreatmentStart,
                            intervalTumorIncidenceTreatmentStart,
                            intervalTumorIncidenceTreatmentStop,
                            intervalTumorIncidenceTreatmentStop
                        )
                    }
                }.reduce(StartAndStopMinAndMax::plus)

                SystemicTreatmentScheme(treatments, startMin, startMax, stopMin, stopMax, null, null, emptyList())
            }
            .sortedByDescending(SystemicTreatmentScheme::intervalTumorIncidenceTreatmentLineStartMin)
    }
}

private fun extractSystemic(
    code: String, schemaNum: Int?, cycleCode: Int?, startInterval: Int?, stopInterval: Int?, prePostCode: Int?
): SystemicTreatment {
    val (preSurgery, postSurgery) = resolvePreAndPostSurgery(prePostCode)
    val (cycles, cycleDetails) = resolveCyclesAndDetails(cycleCode)
    return SystemicTreatment(
        resolveTreatmentName(code), schemaNum, cycles, cycleDetails, startInterval, stopInterval, preSurgery, postSurgery
    )
}

private data class StartAndStopMinAndMax(val startMin: Int?, val startMax: Int?, val stopMin: Int?, val stopMax: Int?) {

    operator fun plus(other: StartAndStopMinAndMax): StartAndStopMinAndMax {
        return StartAndStopMinAndMax(
            startMin = minOfNullables(startMin, other.startMin),
            startMax = maxOfNullables(startMax, other.startMax),
            stopMin = minOfNullables(stopMin, other.stopMin),
            stopMax = maxOfNullables(stopMax, other.stopMax)
        )
    }

    private fun maxOfNullables(a: Int?, b: Int?): Int? {
        return when {
            a == null -> b
            b == null -> a
            else -> max(a, b)
        }
    }

    private fun minOfNullables(a: Int?, b: Int?): Int? {
        return when {
            a == null -> b
            b == null -> a
            else -> min(a, b)
        }
    }
}
