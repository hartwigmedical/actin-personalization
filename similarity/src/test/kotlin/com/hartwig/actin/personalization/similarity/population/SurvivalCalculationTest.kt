package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.similarity.DIAGNOSIS_AND_EPISODE
import com.hartwig.actin.personalization.similarity.report.TableElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested

private const val ELIGIBLE_SUB_POPULATION_SIZE = 50
private val survivalList = listOf(400, 50, 800, 100, 25, 1600, 200)

class SurvivalCalculationTest {

    private val survivalCalculation = SurvivalCalculation(
        timeFunction = SystemicTreatmentPlan::observedOsFromTreatmentStartDays,
        eventFunction = SystemicTreatmentPlan::hadSurvivalEvent,
        title = "Overall survival"
    )

    @Nested
    inner class EligibilityTests {
        @Test
        fun `Should evaluate patients as eligible when observed survival days is not null`() {
            assertThat(survivalCalculation.isEligible(DIAGNOSIS_AND_EPISODE)).isFalse
            assertThat(survivalCalculation.isEligible(patientWithSurvivalDays(null))).isFalse
            assertThat(survivalCalculation.isEligible(patientWithSurvivalDays(100))).isTrue
        }
    }

    @Nested
    inner class CalculationTests {
        @Test
        fun `Should evaluate median survival for patient list`() {
            val measurement = survivalCalculation.calculate(survivalList.map(::patientWithSurvivalDays), ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(measurement.value).isEqualTo(200.0)
            assertThat(measurement.numPatients).isEqualTo(7)
            assertThat(measurement.min).isEqualTo(25)
            assertThat(measurement.max).isEqualTo(1600)
            assertThat(measurement.iqr).isEqualTo(750.0)
        }

        @Test
        fun `Should incorporate censored values in survival calculation`() {
            val censoredPatients = listOf(1, 2, 3, 4, 5).map { patientWithSurvivalDays(it * 365, false) }
            val patients = survivalList.map(::patientWithSurvivalDays) + censoredPatients
            val measurement = survivalCalculation.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(measurement.value).isEqualTo(800.0)
            assertThat(measurement.numPatients).isEqualTo(12)
            assertThat(measurement.min).isEqualTo(25)
            assertThat(measurement.max).isEqualTo(1600)
            assertThat(measurement.iqr).isEqualTo(1500.0)
        }
    }

    @Nested
    inner class TableElementTests {
        @Test
        fun `Should create table elements with all available information`() {
            assertThat(survivalCalculation.createTableElement(Measurement(Double.NaN, 0))).isEqualTo(TableElement.regular("n≤20"))
            assertThat(survivalCalculation.createTableElement(Measurement(100.0, 10))).isEqualTo(TableElement.regular("n≤20"))
            assertThat(survivalCalculation.createTableElement(Measurement(100.0, 50))).isEqualTo(TableElement("100.0", "\n(n=50)"))
            assertThat(survivalCalculation.createTableElement(Measurement(100.0, 50, 50, 150, Double.NaN)))
                .isEqualTo(TableElement("100.0", "\n(n=50)"))
            assertThat(survivalCalculation.createTableElement(Measurement(100.0, 50, 50, 150, 100.0)))
                .isEqualTo(TableElement("100.0", ", IQR: 100.0\n(n=50)"))
        }
    }

    @Test
    fun `Should return empty measurement for empty population`() {
        val measurement = survivalCalculation.calculate(emptyList(), ELIGIBLE_SUB_POPULATION_SIZE)
        assertThat(measurement.value).isNaN
        assertThat(measurement.numPatients).isEqualTo(0)
        assertThat(measurement.min).isNull()
        assertThat(measurement.max).isNull()
        assertThat(measurement.iqr).isNaN
    }

    private fun patientWithSurvivalDays(days: Int?, hadEvent: Boolean = true): DiagnosisAndEpisode {
        return DIAGNOSIS_AND_EPISODE.copy(
            second = DIAGNOSIS_AND_EPISODE.second.copy(
                systemicTreatmentPlan = SystemicTreatmentPlan(
                    treatment = Treatment.CAPECITABINE,
                    systemicTreatmentSchemes = emptyList(),
                    observedOsFromTreatmentStartDays = days,
                    hadSurvivalEvent = hadEvent
                )
            )
        )
    }
}
