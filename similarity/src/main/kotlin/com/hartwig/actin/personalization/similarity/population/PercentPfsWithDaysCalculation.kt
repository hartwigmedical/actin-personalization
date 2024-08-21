package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.similarity.report.TableElement

private const val MIN_PATIENT_COUNT = 20

class PercentPfsWithDaysCalculation(val minPfsDays: Int) : Calculation {

    override fun isEligible(patient: DiagnosisAndEpisode): Boolean {
        return with(patient.second) {
            systemicTreatmentPlan?.intervalTumorIncidenceTreatmentPlanStart?.let { planStart ->
                val minDaysSinceIncidence = planStart + minPfsDays
                hasProgressionOrDeathBeforeMinDays(pfsMeasures, minDaysSinceIncidence)
                        || hasMeasureWithMinDaysAndNoUnknownMeasures(pfsMeasures, minDaysSinceIncidence)
            } == true
        }
    }

    override fun calculate(patients: List<DiagnosisAndEpisode>, eligiblePopulationSize: Int): Measurement {
        val pfsCount = patients.count { (_, episode) ->
            !hasProgressionOrDeathBeforeMinDays(
                episode.pfsMeasures, episode.systemicTreatmentPlan?.intervalTumorIncidenceTreatmentPlanStart!! + minPfsDays
            )
        }
        return Measurement(pfsCount.toDouble() / patients.size, patients.size)
    }

    override fun createTableElement(measurement: Measurement): TableElement {
        return if (measurement.numPatients < MIN_PATIENT_COUNT) {
            TableElement.regular("(n≤$MIN_PATIENT_COUNT)")
        } else {
            TableElement(String.format("%.1f%% ", 100.0 * measurement.value), "(n=${measurement.numPatients})", measurement.value)
        }
    }

    override fun title(): String {
        return "Percent patients with progression-free survival of at least $minPfsDays days in NCR real-world data set"
    }

    private fun hasProgressionOrDeathBeforeMinDays(pfsMeasures: List<PfsMeasure>, minDaysSinceIncidence: Int) = pfsMeasures.any {
        it.pfsMeasureType != PfsMeasureType.CENSOR && it.intervalTumorIncidencePfsMeasureDate?.let { measureInterval ->
            measureInterval < minDaysSinceIncidence
        } == true
    }

    private fun hasNoProgressionOrDeathWithUnknownInterval(pfsMeasures: List<PfsMeasure>) = pfsMeasures.none {
        it.pfsMeasureType != PfsMeasureType.CENSOR && it.intervalTumorIncidencePfsMeasureDate == null
    }

    private fun hasAnyPfsMeasureWithMinDays(pfsMeasures: List<PfsMeasure>, minDaysSinceIncidence: Int) = pfsMeasures.any { measure ->
        measure.intervalTumorIncidencePfsMeasureDate?.let { it >= minDaysSinceIncidence } == true
    }

    private fun hasMeasureWithMinDaysAndNoUnknownMeasures(pfsMeasures: List<PfsMeasure>, minDaysSinceIncidence: Int) =
        hasNoProgressionOrDeathWithUnknownInterval(pfsMeasures) && hasAnyPfsMeasureWithMinDays(pfsMeasures, minDaysSinceIncidence)
}
