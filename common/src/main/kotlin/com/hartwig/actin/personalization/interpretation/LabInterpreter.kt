package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.assessment.LabMeasure
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasurement

class LabInterpreter(private val labMeasurements: List<LabMeasurement>) {

    fun mostRecentLactateDehydrogenasePriorTo(maxDaysSinceDiagnosis: Int): Double? {
        return measurePriorTo(LabMeasure.LACTATE_DEHYDROGENASE, maxDaysSinceDiagnosis)
    }

    fun mostRecentAlkalinePhosphatasePriorTo(maxDaysSinceDiagnosis: Int): Double? {
        return measurePriorTo(LabMeasure.ALKALINE_PHOSPHATASE, maxDaysSinceDiagnosis)
    }

    fun mostRecentLeukocytesAbsolutePriorTo(maxDaysSinceDiagnosis: Int): Double? {
        return measurePriorTo(LabMeasure.LEUKOCYTES_ABSOLUTE, maxDaysSinceDiagnosis)
    }

    fun mostRecentCarcinoembryonicAntigenPriorTo(maxDaysSinceDiagnosis: Int): Double? {
        return measurePriorTo(LabMeasure.CARCINOEMBRYONIC_ANTIGEN, maxDaysSinceDiagnosis)
    }

    fun mostRecentAlbuminePriorTo(maxDaysSinceDiagnosis: Int): Double? {
        return measurePriorTo(LabMeasure.ALBUMINE, maxDaysSinceDiagnosis)
    }

    fun mostRecentNeutrophilsAbsolutePriorTo(maxDaysSinceDiagnosis: Int): Double? {
        return measurePriorTo(LabMeasure.NEUTROPHILS_ABSOLUTE, maxDaysSinceDiagnosis)
    }
    
    private fun measurePriorTo(measure: LabMeasure, maxDaysSinceDiagnosis: Int): Double? {
        return labMeasurements
            .filter { it.name == measure && it.daysSinceDiagnosis <= maxDaysSinceDiagnosis }
            .maxByOrNull { it.daysSinceDiagnosis }?.value
    }
}