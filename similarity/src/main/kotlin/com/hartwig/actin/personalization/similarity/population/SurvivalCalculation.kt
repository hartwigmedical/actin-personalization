package com.hartwig.actin.personalization.similarity.population
import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.similarity.report.TableElement


typealias DiagnosisAndEpisode = Pair<Diagnosis, Episode>

val PFS_CALCULATION = SurvivalCalculation<SystemicTreatmentPlan>(
    timeFunction = SystemicTreatmentPlan::observedPfsDays,
    eventFunction = SystemicTreatmentPlan::hadProgressionEvent,
    title = "Progression-free survival (median, IQR) in NCR real-world data set",
    extractor = { it.second.systemicTreatmentPlan }
)

val OS_CALCULATION = SurvivalCalculation<Diagnosis>(
    timeFunction = Diagnosis::observedOsFromTumorIncidenceDays,
    eventFunction = Diagnosis::hadSurvivalEvent,
    title = "Overall survival (median, IQR) in NCR real-world data set",
    extractor = { it.first }
)

class SurvivalCalculation<T>(
    val timeFunction: (T) -> Int?,
    val eventFunction: (T) -> Boolean?,
    val title: String,
    val extractor: (DiagnosisAndEpisode) -> T?
) : Calculation {

    private val MIN_PATIENT_COUNT = 20

    override fun isEligible(patient: DiagnosisAndEpisode): Boolean {
        val item = extractor(patient) ?: return false
        return timeFunction(item) != null && eventFunction(item) == true
    }


    override fun calculate(patients: List<DiagnosisAndEpisode>, eligiblePopulationSize: Int): Measurement {
        val items = patients.mapNotNull { extractor(it) }
        val sortedItems = items.mapNotNull { item ->
            timeFunction(item)?.let { item to it }
        }.sortedBy { it.second }.map { it.first }

        val eventHistory = eventHistory(sortedItems)

        return Measurement(
            survivalForQuartile(eventHistory, 0.5),
            items.size,
            eventHistory.firstOrNull()?.daysSincePlanStart,
            eventHistory.lastOrNull()?.daysSincePlanStart,
            survivalForQuartile(eventHistory, 0.75) - survivalForQuartile(eventHistory, 0.25)
        )
    }
    private fun survivalForQuartile(eventHistory: List<EventCountAndSurvivalAtTime>, quartileAsDecimal: Double): Double {
        val expectedSurvivalFraction = 1 - quartileAsDecimal
        val searchIndex = eventHistory.binarySearchBy(-expectedSurvivalFraction) { -it.survival }
        val realIndex = if (searchIndex < 0) -(searchIndex + 1) else searchIndex

        return if (realIndex == eventHistory.size) Double.NaN else eventHistory[realIndex].daysSincePlanStart.toDouble()
    }

    override fun createTableElement(measurement: Measurement): TableElement {
        return when {
            measurement.numPatients <= MIN_PATIENT_COUNT -> TableElement.regular("n≤$MIN_PATIENT_COUNT")
            measurement.value.isNaN() -> TableElement.regular("-")
            else -> with(measurement) {
                val iqrString = if (iqr != null && !iqr.isNaN()) ", IQR: $iqr" else ""
                TableElement(value.toString(), "${iqrString}\n(n=$numPatients)")
            }
        }
    }

    override fun title(): String {
        return title
    }

    tailrec fun eventHistory(
        populationToProcess: List<T>,
        eventHistory: List<EventCountAndSurvivalAtTime> = emptyList()
    ): List<EventCountAndSurvivalAtTime> {
        if (populationToProcess.isEmpty()) return eventHistory

        val current = populationToProcess.first()
        val time = timeFunction(current)
        val eventOccurred = eventFunction(current)

        return if (time == null || eventOccurred != true) {
            eventHistory(populationToProcess.drop(1), eventHistory)
        } else {
            val previousEvent = eventHistory.lastOrNull() ?: EventCountAndSurvivalAtTime(0, 0, 1.0)
            val newEvent = EventCountAndSurvivalAtTime(
                time,
                previousEvent.numEvents + 1,
                previousEvent.survival * (1 - (1.0 / populationToProcess.size))
            )
            eventHistory(populationToProcess.drop(1), eventHistory + newEvent)
        }
    }
}