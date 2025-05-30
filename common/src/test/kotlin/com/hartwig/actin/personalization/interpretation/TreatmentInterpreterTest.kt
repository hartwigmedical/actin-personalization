package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.TestTreatmentFactory
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TreatmentInterpreterTest {

    @Test
    fun `Should determine state about existence and timing of metastatic treatment`() {
        val noEpisodeInterpreter = TreatmentInterpreter(emptyList())
        assertThat(noEpisodeInterpreter.hasMetastaticTreatment()).isFalse()
        assertThat(noEpisodeInterpreter.isMetastaticPriorToMetastaticTreatmentDecision()).isFalse()

        val noMetastaticTreatmentInterpreter =
            TreatmentInterpreter(listOf(TestDatamodelFactory.treatmentEpisode(metastaticPresence = MetastaticPresence.ABSENT)))
        assertThat(noMetastaticTreatmentInterpreter.hasMetastaticTreatment()).isFalse()
        assertThat(noMetastaticTreatmentInterpreter.isMetastaticPriorToMetastaticTreatmentDecision()).isFalse()

        val startMetastaticTreatmentInterpreter =
            TreatmentInterpreter(listOf(TestDatamodelFactory.treatmentEpisode(metastaticPresence = MetastaticPresence.AT_START)))
        assertThat(startMetastaticTreatmentInterpreter.hasMetastaticTreatment()).isTrue()
        assertThat(startMetastaticTreatmentInterpreter.isMetastaticPriorToMetastaticTreatmentDecision()).isTrue()
        
        val progressionMetastaticTreatmentInterpreter =
            TreatmentInterpreter(listOf(TestDatamodelFactory.treatmentEpisode(metastaticPresence = MetastaticPresence.AT_PROGRESSION)))
        assertThat(progressionMetastaticTreatmentInterpreter.hasMetastaticTreatment()).isTrue()
        assertThat(progressionMetastaticTreatmentInterpreter.isMetastaticPriorToMetastaticTreatmentDecision()).isFalse()
    }

    @Test
    fun `Should determine whether progression has occurred`() {
        val interpreter = TreatmentInterpreter(listOf(TestTreatmentFactory.create(systemicTreatment = Treatment.CAPOX)))

        assertThat(interpreter.hasProgressionEventAfterMetastaticSystemicTreatmentStart()).isFalse()
    }

    @Test
    fun `Should ignore progression events prior to systemic treatment start`() {
        val treatmentEpisode =
            TestTreatmentFactory.create(
                systemicTreatment = Treatment.CAPOX,
                daysBetweenDiagnosisAndSystemicTreatmentStart = 10,
                hasProgressionEvent = true,
                daysBetweenDiagnosisAndProgression = 8
            )

        val interpreter = TreatmentInterpreter(listOf(treatmentEpisode))

        assertThat(interpreter.hasProgressionEventAfterMetastaticSystemicTreatmentStart()).isFalse()
    }

    @Test
    fun `Should return first progression measure when multiple after treatment start`() {
        val treatmentEpisodeWithMultipleProgression =
            TestTreatmentFactory.create(
                systemicTreatment = Treatment.CAPOX,
                daysBetweenDiagnosisAndSystemicTreatmentStart = 10
            ).copy(
                progressionMeasures = listOf(
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = 15),
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = 18),
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = 12),
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = 11, type = ProgressionMeasureType.CENSOR),
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = 8)
                )
            )

        val interpreter = TreatmentInterpreter(listOf(treatmentEpisodeWithMultipleProgression))

        assertThat(interpreter.daysBetweenProgressionAndPrimaryDiagnosis()).isEqualTo(12)
        assertThat(interpreter.daysBetweenProgressionAndMetastaticSystemicTreatmentStart()).isEqualTo(2)
    }
}