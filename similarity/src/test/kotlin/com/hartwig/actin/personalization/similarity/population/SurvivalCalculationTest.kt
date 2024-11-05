package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.*
import com.hartwig.actin.personalization.similarity.DIAGNOSIS_EPISODE_TREATMENT
import com.hartwig.actin.personalization.similarity.patientWithTreatment
import com.hartwig.actin.personalization.similarity.report.TableElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val ELIGIBLE_SUB_POPULATION_SIZE = 50
data class SurvivalData(val osDays: Int?, val pfsDays: Int?)

private val survivalList = listOf(
    SurvivalData(osDays = 400, pfsDays = 400),
    SurvivalData(osDays = 50, pfsDays = 50),
    SurvivalData(osDays = 800, pfsDays = 800),
    SurvivalData(osDays = 100, pfsDays = 100),
    SurvivalData(osDays = 25, pfsDays = 25),
    SurvivalData(osDays = 1600, pfsDays = 1600),
    SurvivalData(osDays = 200, pfsDays = 200)
)

class SurvivalCalculationTest {
    private val survivalCalculation = SurvivalCalculation(
        timeFunction = { it.diagnosis.observedOsFromTumorIncidenceDays },
        eventFunction = { it.diagnosis.hadSurvivalEvent },
        title = "Overall survival"
    )
    @Nested
    inner class SurvivalTests{
        @Test
        fun `Should calculate for OS - eligibility, Median and correctly work around censored values`() {
            runSurvivalCalculationTests(
                calculation = OS_CALCULATION,
                createPatient = { data, hadEvent ->
                    patientWithSurvivalDays(
                        osDays = data.osDays,
                        hadEvent = hadEvent
                    )
                }
            )
        }
        @Test
        fun `Should calculate for PFS - eligibility, Median and correctly work around censored values`() {
            runSurvivalCalculationTests(
                calculation = PFS_CALCULATION,
                createPatient = { data, hadEvent ->
                    patientWithSurvivalDays(
                        pfsDays = data.pfsDays,
                        hadEvent = hadEvent
                    )
                }
            )
        }
        private fun runSurvivalCalculationTests(
            calculation: SurvivalCalculation,
            createPatient: (SurvivalData, Boolean) -> DiagnosisEpisodeTreatment
        ) {
            testEligibility(calculation, createPatient)
            testMedianSurvival(
                calculation, createPatient
            )
            testCensoredValues(
                calculation, createPatient
            )
        }
        private fun testEligibility(
            calculation: SurvivalCalculation,
            createPatient: (SurvivalData, Boolean) -> DiagnosisEpisodeTreatment
        ) {
            val eligiblePatient = createPatient(SurvivalData(osDays = 100, pfsDays = 100), true)
            assertThat(calculation.isEligible(eligiblePatient)).isTrue()

            val ineligiblePatient = createPatient(SurvivalData(osDays = null, pfsDays = null), true)
            assertThat(calculation.isEligible(ineligiblePatient)).isFalse()
        }
        private fun testMedianSurvival(
            calculation: SurvivalCalculation,
            createPatient: (SurvivalData, Boolean) -> DiagnosisEpisodeTreatment
        ) {
            val patients = survivalList.map { data -> createPatient(data, true) }
            val measurement = calculation.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(measurement.value).isEqualTo(200.0)
            assertThat(measurement.numPatients).isEqualTo(7)
            assertThat(measurement.min).isEqualTo(25)
            assertThat(measurement.max).isEqualTo(1600)
            assertThat(measurement.iqr).isEqualTo(750.0)
        }
        private fun testCensoredValues(
            calculation: SurvivalCalculation,
            createPatient: (SurvivalData, Boolean) -> DiagnosisEpisodeTreatment
        ) {
            val censoredPatients = listOf(1, 2, 3, 4, 5).map { i ->
                val survivalDays = i * 365
                createPatient(SurvivalData(osDays = survivalDays, pfsDays = survivalDays), false)
            }
            val eligiblePatients = survivalList.map { data -> createPatient(data, true) }
            val patients = eligiblePatients + censoredPatients

            val measurement = calculation.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(measurement.value).isEqualTo(200.0)
            assertThat(measurement.numPatients).isEqualTo(7)
            assertThat(measurement.min).isEqualTo(25)
            assertThat(measurement.max).isEqualTo(1600)
            assertThat(measurement.iqr).isEqualTo(750.0)
        }
    }
    private fun patientWithSurvivalDays(
        osDays: Int?= null,
        pfsDays: Int? = null,
        hadEvent: Boolean = true
    ): DiagnosisEpisodeTreatment {
        val diagnosis = DIAGNOSIS_EPISODE_TREATMENT.diagnosis.copy(
            observedOsFromTumorIncidenceDays = osDays,
            hadSurvivalEvent = hadEvent
        )
        val systemicTreatmentPlan = SystemicTreatmentPlan(
            treatment = Treatment.FLUOROURACIL,
            systemicTreatmentSchemes = emptyList(),
            observedPfsDays = pfsDays,
            hadProgressionEvent = hadEvent
        )
        val episode = DIAGNOSIS_EPISODE_TREATMENT.episode.copy(
            systemicTreatmentPlan = systemicTreatmentPlan
        )
        return DiagnosisEpisodeTreatment(diagnosis, episode, systemicTreatmentPlan)
    }
    @Nested
    inner class TableElementTests {
        @Test
        fun `Should create table elements with all available information`() {
            assertThat(survivalCalculation.createTableElement(Measurement(Double.NaN, 0)))
                .isEqualTo(TableElement.regular("n≤20"))
            assertThat(survivalCalculation.createTableElement(Measurement(100.0, 10)))
                .isEqualTo(TableElement.regular("n≤20"))
            assertThat(survivalCalculation.createTableElement(Measurement(100.0, 50)))
                .isEqualTo(TableElement("100.0", "\n(n=50)"))
            assertThat(
                survivalCalculation.createTableElement(
                    Measurement(100.0, 50, 50, 150, Double.NaN)
                )
            ).isEqualTo(TableElement("100.0", "\n(n=50)"))
            assertThat(
                survivalCalculation.createTableElement(
                    Measurement(100.0, 50, 50, 150, 100.0)
                )
            ).isEqualTo(TableElement("100.0", ", IQR: 100.0\n(n=50)"))
        }
    }
    @Test
    fun `Should return empty measurement for empty population`() {
        val measurement = survivalCalculation.calculate(emptyList(), ELIGIBLE_SUB_POPULATION_SIZE)
        assertThat(measurement.value).isNaN()
        assertThat(measurement.numPatients).isEqualTo(0)
        assertThat(measurement.min).isNull()
        assertThat(measurement.max).isNull()
        assertThat(measurement.iqr).isNaN()
    }
}
