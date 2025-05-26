package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.assessment.LabMeasure
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasurement
import com.hartwig.actin.personalization.ncr.datamodel.NcrLabValues
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.NcrFunctions

object NcrLabMeasurementExtractor {

    fun extract(records: List<NcrRecord>): List<LabMeasurement> {
        return NcrFunctions.recordsWithMinDaysSinceDiagnosis(records).flatMap { extractLabMeasurements(it.key.labValues, it.value) }
    }

    private fun extractLabMeasurements(labValues: NcrLabValues, minDaysSinceDiagnosis: Int): List<LabMeasurement> {
        with(labValues) {
            val measurements = listOf(
                LabMeasure.LACTATE_DEHYDROGENASE to listOf(
                    ldh1 to ldhInt1,
                    ldh2 to ldhInt2,
                    ldh3 to ldhInt3,
                    ldh4 to ldhInt4
                ),
                LabMeasure.ALKALINE_PHOSPHATASE to listOf(
                    af1 to afInt1,
                    af2 to afInt2,
                    af3 to afInt3,
                    af4 to afInt4
                ),
                LabMeasure.NEUTROPHILS_ABSOLUTE to listOf(
                    neutro1 to neutroInt1,
                    neutro2 to neutroInt2,
                    neutro3 to neutroInt3,
                    neutro4 to neutroInt4
                ),
                LabMeasure.ALBUMINE to listOf(
                    albumine1 to albumineInt1,
                    albumine2 to albumineInt2,
                    albumine3 to albumineInt3,
                    albumine4 to albumineInt4
                ),
                LabMeasure.LEUKOCYTES_ABSOLUTE to listOf(
                    leuko1 to leukoInt1,
                    leuko2 to leukoInt2,
                    leuko3 to leukoInt3,
                    leuko4 to leukoInt4
                )
            ).flatMap { (measure, values) ->
                values.mapNotNull { (value, interval) ->
                    value?.toDouble()?.takeIf { it != 9999.0 && it <= measure.upperBound }?.let {
                        LabMeasurement(
                            daysSinceDiagnosis = interval ?: minDaysSinceDiagnosis,
                            name = measure,
                            value = value.toDouble(),
                            unit = measure.unit,
                            isPreSurgical = null,
                            isPostSurgical = null
                        )
                    }
                }
            }

            return measurements + listOfNotNull(
                periSurgicalCeaMeasurement(minDaysSinceDiagnosis, prechirCea, true),
                periSurgicalCeaMeasurement(minDaysSinceDiagnosis, postchirCea, false)
            )
        }
    }

    private fun periSurgicalCeaMeasurement(minDaysSinceDiagnosis: Int, measurement: Double?, isPreSurgical: Boolean) =
        measurement?.takeIf { it != 9999.0 && it <= LabMeasure.CARCINOEMBRYONIC_ANTIGEN.upperBound }?.let {
            LabMeasurement(
                daysSinceDiagnosis = minDaysSinceDiagnosis,
                name = LabMeasure.CARCINOEMBRYONIC_ANTIGEN,
                value = it,
                unit = LabMeasure.CARCINOEMBRYONIC_ANTIGEN.unit,
                isPreSurgical = isPreSurgical,
                isPostSurgical = !isPreSurgical
            )
        }
}
