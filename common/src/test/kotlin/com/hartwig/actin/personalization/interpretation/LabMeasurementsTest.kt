package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasure
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LabMeasurementsTest {

    private val measurements =
        listOf(
            TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 10, name = LabMeasure.LACTATE_DEHYDROGENASE, value = 10.0),
            TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 20, name = LabMeasure.ALKALINE_PHOSPHATASE, value = 20.0),
            TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 30, name = LabMeasure.LEUKOCYTES_ABSOLUTE, value = 30.0),
            TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 40, name = LabMeasure.CARCINOEMBRYONIC_ANTIGEN, value = 40.0),
            TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 50, name = LabMeasure.ALBUMINE, value = 50.0),
            TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 60, name = LabMeasure.NEUTROPHILS_ABSOLUTE, value = 50.0)
        )

    @Test
    fun `Should determine lactate dehydrogenase at metastatic diagnosis`() {
        assertThat(LabMeasurements.lactateDehydrogenaseAtMetastaticDiagnosis(measurements, 5)).isNull()
        assertThat(LabMeasurements.lactateDehydrogenaseAtMetastaticDiagnosis(measurements, 30)).isEqualTo(10.0)
        assertThat(LabMeasurements.lactateDehydrogenaseAtMetastaticDiagnosis(measurements, 80)).isEqualTo(10.0)
    }

    @Test
    fun `Should determine alkaline phosphatase at metastatic diagnosis`() {
        assertThat(LabMeasurements.alkalinePhosphataseAtMetastaticDiagnosis(measurements, 5)).isNull()
        assertThat(LabMeasurements.alkalinePhosphataseAtMetastaticDiagnosis(measurements, 30)).isEqualTo(20.0)
        assertThat(LabMeasurements.alkalinePhosphataseAtMetastaticDiagnosis(measurements, 80)).isEqualTo(20.0)
    }
}