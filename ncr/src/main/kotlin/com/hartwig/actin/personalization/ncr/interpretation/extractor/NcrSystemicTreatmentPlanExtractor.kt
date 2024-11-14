package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentScheme
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentSchemeDrug
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.ncr.datamodel.NcrSystemicTreatment
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTreatmentNameMapper.resolve
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolveCyclesAndDetails
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolvePreAndPostSurgery
import kotlin.math.max
import kotlin.math.min

private val ALLOWED_SUBSTITUTIONS = setOf(Drug.FLUOROURACIL, Drug.CAPECITABINE, Drug.TEGAFUR, Drug.TEGAFUR_OR_GIMERACIL_OR_OTERACIL)

class NcrSystemicTreatmentPlanExtractor {

    fun extractSystemicTreatmentPlan(
        systemicTreatment: NcrSystemicTreatment,
        pfsMeasures: List<PfsMeasure>,
        responseMeasure: ResponseMeasure?,
        intervalTumorIncidenceLatestAliveStatus: Int
    ): SystemicTreatmentPlan {
        val treatmentSchemes = extractSystemicTreatmentSchemes(systemicTreatment)

        if (treatmentSchemes.isEmpty()) {
            return SystemicTreatmentPlan(
                treatment = Treatment.NONE,
                systemicTreatmentSchemes = emptyList(),
                intervalTumorIncidenceTreatmentPlanStartDays = null,
                intervalTumorIncidenceTreatmentPlanStopDays = null,
                intervalTreatmentPlanStartResponseDays = null,
                observedPfsDays = null,
                hadProgressionEvent = null
            )
        }

        val firstScheme = treatmentSchemes.first()
        val treatment = extractTreatmentFromSchemes(treatmentSchemes)
        val daysUntilPlanStart = firstScheme.intervalTumorIncidenceTreatmentLineStartMinDays
        val daysUntilPlanEnd = treatmentSchemes.last().intervalTumorIncidenceTreatmentLineStopMaxDays

        val (observedPfsDays, hadProgressionEvent) = NcrPfsInterpreter.determineObservedPfsAndProgressionEvent(
            daysUntilPlanStart,
            daysUntilPlanEnd,
            pfsMeasures
        )

        return SystemicTreatmentPlan(
            treatment = treatment,
            systemicTreatmentSchemes = treatmentSchemes,
            intervalTumorIncidenceTreatmentPlanStartDays = daysUntilPlanStart,
            intervalTumorIncidenceTreatmentPlanStopDays = daysUntilPlanEnd,
            intervalTreatmentPlanStartResponseDays = responseMeasure?.intervalTumorIncidenceResponseDays
                ?.let { responseInterval -> daysUntilPlanStart?.let { responseInterval - daysUntilPlanStart } },
            observedPfsDays = observedPfsDays,
            hadProgressionEvent = hadProgressionEvent,
        )
    }

    private fun drugsFromScheme(systemicTreatmentScheme: SystemicTreatmentScheme): List<Drug> {
        return systemicTreatmentScheme.treatmentComponents.map(SystemicTreatmentSchemeDrug::drug)
    }

    private fun extractTreatmentFromSchemes(treatmentSchemes: Iterable<SystemicTreatmentScheme>): Treatment {

        val firstSchemeDrugs = drugsFromScheme(treatmentSchemes.first()).toSet()

        val followUpDrugs = treatmentSchemes.drop(1).flatMap(::drugsFromScheme).toSet()
        val newDrugsToIgnore = if (firstSchemeDrugs.intersect(ALLOWED_SUBSTITUTIONS).isNotEmpty()) ALLOWED_SUBSTITUTIONS else emptySet()

        return if ((followUpDrugs - firstSchemeDrugs - newDrugsToIgnore).isEmpty()) {
            Treatment.findForDrugs(firstSchemeDrugs)
        } else Treatment.OTHER
    }

    private fun extractSystemicTreatmentSchemes(systemicTreatment: NcrSystemicTreatment): List<SystemicTreatmentScheme> {
        return with(systemicTreatment) {
            listOfNotNull(
                systCode1?.let { extractSystemicComponent(it, systSchemanum1, systKuren1, systStartInt1, systStopInt1, systPrepost1) },
                systCode2?.let { extractSystemicComponent(it, systSchemanum2, systKuren2, systStartInt2, systStopInt2, systPrepost2) },
                systCode3?.let { extractSystemicComponent(it, systSchemanum3, systKuren3, systStartInt3, systStopInt3, systPrepost3) },
                systCode4?.let { extractSystemicComponent(it, systSchemanum4, systKuren4, systStartInt4, systStopInt4, systPrepost4) },
                systCode5?.let { extractSystemicComponent(it, systSchemanum5, systKuren5, systStartInt5, systStopInt5, systPrepost5) },
                systCode6?.let { extractSystemicComponent(it, systSchemanum6, systKuren6, systStartInt6, systStopInt6, systPrepost6) },
                systCode7?.let { extractSystemicComponent(it, systSchemanum7, systKuren7, systStartInt7, systStopInt7, systPrepost7) },
                systCode8?.let { extractSystemicComponent(it, systSchemanum8, systKuren8, systStartInt8, systStopInt8, systPrepost8) },
                systCode9?.let { extractSystemicComponent(it, systSchemanum9, systKuren9, systStartInt9, systStopInt9, systPrepost9) },
                systCode10?.let {
                    extractSystemicComponent(
                        it,
                        systSchemanum10,
                        systKuren10,
                        systStartInt10,
                        systStopInt10,
                        systPrepost10
                    )
                },
                systCode11?.let {
                    extractSystemicComponent(
                        it,
                        systSchemanum11,
                        systKuren11,
                        systStartInt11,
                        systStopInt11,
                        systPrepost11
                    )
                },
                systCode12?.let {
                    extractSystemicComponent(
                        it,
                        systSchemanum12,
                        systKuren12,
                        systStartInt12,
                        systStopInt12,
                        systPrepost12
                    )
                },
                systCode13?.let {
                    extractSystemicComponent(
                        it,
                        systSchemanum13,
                        systKuren13,
                        systStartInt13,
                        systStopInt13,
                        systPrepost13
                    )
                },
                systCode14?.let {
                    extractSystemicComponent(
                        it, systSchemanum14, systKuren14, systStartInt14, systStopInt14, systPrepost14
                    )
                }
            )
                .groupBy(SystemicTreatmentSchemeDrug::schemeNumber)
                .map { (schemeNumber, treatments) ->
                    val (startMin, startMax, stopMin, stopMax) = treatments.map {
                        with(it) {
                            StartAndStopMinAndMax(
                                intervalTumorIncidenceTreatmentStartDays,
                                intervalTumorIncidenceTreatmentStartDays,
                                intervalTumorIncidenceTreatmentStopDays,
                                intervalTumorIncidenceTreatmentStopDays
                            )
                        }
                    }.reduce(StartAndStopMinAndMax::plus)

                    SystemicTreatmentScheme(schemeNumber, treatments, startMin, startMax, stopMin, stopMax)
                }
                .sortedBy(SystemicTreatmentScheme::schemeNumber)
        }
    }

    private fun extractSystemicComponent(
        code: String, schemaNum: Int?, cycleCode: Int?, startInterval: Int?, stopInterval: Int?, prePostCode: Int?
    ): SystemicTreatmentSchemeDrug {
        val (preSurgery, postSurgery) = resolvePreAndPostSurgery(prePostCode)
        val (cycles, intent, isOngoing) = resolveCyclesAndDetails(cycleCode)
        return SystemicTreatmentSchemeDrug(
            resolve(code),
            schemaNum,
            cycles,
            intent,
            isOngoing,
            startInterval,
            stopInterval,
            preSurgery,
            postSurgery
        )
    }

    private data class StartAndStopMinAndMax(
        val startMin: Int?, val startMax: Int?, val stopMin: Int?, val stopMax: Int?
    ) {

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
}