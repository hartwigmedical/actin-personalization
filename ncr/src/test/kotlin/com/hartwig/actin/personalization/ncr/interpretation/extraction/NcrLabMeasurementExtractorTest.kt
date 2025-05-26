package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.assessment.LabMeasure
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasurement
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrLabMeasurementExtractorTest {

    @Test
    fun `Should extract lab measurements from minimal records`() {
        assertThat(NcrLabMeasurementExtractor.extract(TestNcrRecordFactory.minimalEntryRecords())).isEmpty()
    }

    @Test
    fun `Should extract lab measurements from proper records`() {
        val labMeasurements = NcrLabMeasurementExtractor.extract(TestNcrRecordFactory.properEntryRecords())
        assertThat(labMeasurements).isEqualTo(expectedLabMeasurements())
    }

    private fun expectedLabMeasurements(): List<LabMeasurement> {
        val diagnosisMeasurements = listOf(
            LabMeasurement(
                daysSinceDiagnosis = 1,
                name = LabMeasure.LACTATE_DEHYDROGENASE,
                value = 10.0,
                unit = LabMeasure.LACTATE_DEHYDROGENASE.unit,
                isPreSurgical = null,
                isPostSurgical = null
            ),
            LabMeasurement(
                daysSinceDiagnosis = 2,
                name = LabMeasure.ALKALINE_PHOSPHATASE,
                value = 20.0,
                unit = LabMeasure.ALKALINE_PHOSPHATASE.unit,
                isPreSurgical = null,
                isPostSurgical = null
            ),
            LabMeasurement(
                daysSinceDiagnosis = 3,
                name = LabMeasure.NEUTROPHILS_ABSOLUTE,
                value = 30.5,
                unit = LabMeasure.NEUTROPHILS_ABSOLUTE.unit,
                isPreSurgical = null,
                isPostSurgical = null
            ),
            LabMeasurement(
                daysSinceDiagnosis = 4,
                name = LabMeasure.ALBUMINE,
                value = 40.5,
                unit = LabMeasure.ALBUMINE.unit,
                isPreSurgical = null,
                isPostSurgical = null
            ),
            LabMeasurement(
                daysSinceDiagnosis = 5,
                name = LabMeasure.LEUKOCYTES_ABSOLUTE,
                value = 50.5,
                unit = LabMeasure.LEUKOCYTES_ABSOLUTE.unit,
                isPreSurgical = null,
                isPostSurgical = null
            ),
            LabMeasurement(
                daysSinceDiagnosis = 0,
                name = LabMeasure.CARCINOEMBRYONIC_ANTIGEN,
                value = 0.1,
                unit = LabMeasure.CARCINOEMBRYONIC_ANTIGEN.unit,
                isPreSurgical = true,
                isPostSurgical = false
            ),
            LabMeasurement(
                daysSinceDiagnosis = 0,
                name = LabMeasure.CARCINOEMBRYONIC_ANTIGEN,
                value = 0.2,
                unit = LabMeasure.CARCINOEMBRYONIC_ANTIGEN.unit,
                isPreSurgical = false,
                isPostSurgical = true
            )
        )

        val followupMeasurements = listOf(
            LabMeasurement(
                daysSinceDiagnosis = 300,
                name = LabMeasure.ALKALINE_PHOSPHATASE,
                value = 20.0,
                unit = LabMeasure.ALKALINE_PHOSPHATASE.unit,
                isPreSurgical = null,
                isPostSurgical = null
            ),
            LabMeasurement(
                daysSinceDiagnosis = 400,
                name = LabMeasure.ALKALINE_PHOSPHATASE,
                value = 30.0,
                unit = LabMeasure.ALKALINE_PHOSPHATASE.unit,
                isPreSurgical = null,
                isPostSurgical = null
            ),
            LabMeasurement(
                daysSinceDiagnosis = 50,
                name = LabMeasure.CARCINOEMBRYONIC_ANTIGEN,
                value = 0.3,
                unit = LabMeasure.CARCINOEMBRYONIC_ANTIGEN.unit,
                isPreSurgical = true,
                isPostSurgical = false
            ),
            LabMeasurement(
                daysSinceDiagnosis = 50,
                name = LabMeasure.CARCINOEMBRYONIC_ANTIGEN,
                value = 0.4,
                unit = LabMeasure.CARCINOEMBRYONIC_ANTIGEN.unit,
                isPreSurgical = false,
                isPostSurgical = true
            )
        )

        return diagnosisMeasurements + followupMeasurements
    }
}