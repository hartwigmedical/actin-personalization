package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.similarity.DIAGNOSIS_EPISODE_TREATMENT
import com.hartwig.actin.personalization.similarity.report.TableElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ELIGIBLE_SUB_POPULATION_SIZE = 100

class TreatmentDecisionCalculationTest {

    @Test
    fun `Should evaluate all patients as eligible`() {
        assertThat(TreatmentDecisionCalculation.isEligible(DIAGNOSIS_EPISODE_TREATMENT)).isTrue
    }

    @Test
    fun `Should calculate measurement as percentage of population`() {
        val patients = listOf(DIAGNOSIS_EPISODE_TREATMENT, DIAGNOSIS_EPISODE_TREATMENT, DIAGNOSIS_EPISODE_TREATMENT)
        val measurement = TreatmentDecisionCalculation.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
        assertThat(measurement.value).isEqualTo(0.03)
        assertThat(measurement.numPatients).isEqualTo(3)
    }

    @Test
    fun `Should create table element with percentage of population`() {
        val measurement = Measurement(0.03, 3)
        val tableElement = TreatmentDecisionCalculation.createTableElement(measurement)
        assertThat(tableElement).isEqualTo(TableElement.regular("3.0%"))
    }
}