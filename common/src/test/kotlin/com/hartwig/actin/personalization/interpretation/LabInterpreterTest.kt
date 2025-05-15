package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasure
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LabInterpreterTest {

    private val interpreter =
        LabInterpreter(
            listOf(
                TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 10, name = LabMeasure.LACTATE_DEHYDROGENASE, value = 10.0),
                TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 20, name = LabMeasure.ALKALINE_PHOSPHATASE, value = 20.0),
                TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 30, name = LabMeasure.LEUKOCYTES_ABSOLUTE, value = 30.0),
                TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 40, name = LabMeasure.CARCINOEMBRYONIC_ANTIGEN, value = 40.0),
                TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 50, name = LabMeasure.ALBUMINE, value = 50.0),
                TestDatamodelFactory.labMeasurement(daysSinceDiagnosis = 60, name = LabMeasure.NEUTROPHILS_ABSOLUTE, value = 60.0)
            )
        )

    @Test
    fun `Should determine lactate dehydrogenase for multiple intervals`() {
        assertThat(interpreter.mostRecentLactateDehydrogenasePriorTo(maxDaysSinceDiagnosis = 5)).isNull()
        assertThat(interpreter.mostRecentLactateDehydrogenasePriorTo(maxDaysSinceDiagnosis = 30)).isEqualTo(10.0)
        assertThat(interpreter.mostRecentLactateDehydrogenasePriorTo(maxDaysSinceDiagnosis = 80)).isEqualTo(10.0)
    }

    @Test
    fun `Should determine alkaline phosphatase for multiple intervals`() {
        assertThat(interpreter.mostRecentAlkalinePhosphatasePriorTo(maxDaysSinceDiagnosis = 5)).isNull()
        assertThat(interpreter.mostRecentAlkalinePhosphatasePriorTo(maxDaysSinceDiagnosis =  30)).isEqualTo(20.0)
        assertThat(interpreter.mostRecentAlkalinePhosphatasePriorTo(maxDaysSinceDiagnosis = 80)).isEqualTo(20.0)
    }

    @Test
    fun `Should determine leukocytes absolute for multiple intervals`() {
        assertThat(interpreter.mostRecentLeukocytesAbsolutePriorTo(maxDaysSinceDiagnosis = 5)).isNull()
        assertThat(interpreter.mostRecentLeukocytesAbsolutePriorTo(maxDaysSinceDiagnosis =  30)).isEqualTo(30.0)
        assertThat(interpreter.mostRecentLeukocytesAbsolutePriorTo(maxDaysSinceDiagnosis = 80)).isEqualTo(30.0)
    }

    @Test
    fun `Should determine carcinoembryonic antigen for multiple intervals`() {
        assertThat(interpreter.mostRecentCarcinoembryonicAntigenPriorTo(maxDaysSinceDiagnosis = 5)).isNull()
        assertThat(interpreter.mostRecentCarcinoembryonicAntigenPriorTo(maxDaysSinceDiagnosis =  30)).isNull()
        assertThat(interpreter.mostRecentCarcinoembryonicAntigenPriorTo(maxDaysSinceDiagnosis = 80)).isEqualTo(40.0)
    }

    @Test
    fun `Should determine albumine for multiple intervals`() {
        assertThat(interpreter.mostRecentAlbuminePriorTo(maxDaysSinceDiagnosis = 5)).isNull()
        assertThat(interpreter.mostRecentAlbuminePriorTo(maxDaysSinceDiagnosis =  30)).isNull()
        assertThat(interpreter.mostRecentAlbuminePriorTo(maxDaysSinceDiagnosis = 80)).isEqualTo(50.0)
    }

    @Test
    fun `Should determine neutrophils for multiple intervals`() {
        assertThat(interpreter.mostRecentNeutrophilsAbsolutePriorTo(maxDaysSinceDiagnosis = 5)).isNull()
        assertThat(interpreter.mostRecentNeutrophilsAbsolutePriorTo(maxDaysSinceDiagnosis =  30)).isNull()
        assertThat(interpreter.mostRecentNeutrophilsAbsolutePriorTo(maxDaysSinceDiagnosis = 80)).isEqualTo(60.0)
    }
}