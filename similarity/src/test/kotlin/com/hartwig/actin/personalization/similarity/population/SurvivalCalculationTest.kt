package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisode
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.similarity.DIAGNOSIS_EPISODE
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
    private val survivalCalculationsFunctions = listOf(
        OS_CALCULATION to "OS",
        PFS_CALCULATION to "PFS"
    )
    @Nested
    inner class SurvivalTests {

        @Test
        fun `Eligibility tests for OS and PFS`() {
            survivalCalculationsFunctions.forEach { (calculation, name) ->
                testEligibility(calculation, name)
            }
        }

        @Test
        fun `Median survival tests for OS and PFS`() {
            survivalCalculationsFunctions.forEach { (calculation, name) ->
                testMedianSurvival(calculation, name)
            }
        }

        @Test
        fun `Censored value tests for OS and PFS`() {
            survivalCalculationsFunctions.forEach { (calculation, name) ->
                testCensoredValues(calculation, name)
            }
        }

        private fun testEligibility(calculation:  SurvivalCalculation,name: String) {
            val createPatient: (Int?, Boolean?) -> DiagnosisEpisode = { days, hadEvent ->
                patientWithSurvivalDays(
                    osDays = if (name == "OS") days ?: 0 else 0,
                    pfsDays = if (name == "PFS") days else null,
                    hadEvent = hadEvent
                )
            }
            assertThat(calculation.isEligible(createPatient(100, true))).describedAs("Eligibility for  $name").isTrue()
            assertThat(calculation.isEligible(createPatient(100, null))).describedAs("Eligibility for  $name").isFalse()

            if (name == "PFS") {
                assertThat(calculation.isEligible(createPatient(null, true))).describedAs("Eligibility for $name with null days").isFalse()
            }
         }


        private fun testMedianSurvival(calculation: SurvivalCalculation, name: String) {
            val patients = survivalList.map { data ->
                patientWithSurvivalDays(
                    osDays = if (name.contains("OS")) data.osDays ?: 0 else 0,
                    pfsDays = if (name.contains("PFS")) data.pfsDays else null,
                    hadEvent = true
                )
            }
            val measurement = calculation.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(measurement.value).describedAs("Median survival value for $name").isEqualTo(200.0)
            assertThat(measurement.numPatients).describedAs("Number of patients for $name").isEqualTo(7)
            assertThat(measurement.min).describedAs("Minimum survival for $name").isEqualTo(25)
            assertThat(measurement.max).describedAs("Maximum survival for $name").isEqualTo(1600)
            assertThat(measurement.iqr).describedAs("IQR for $name").isEqualTo(750.0)
        }

        private fun testCensoredValues(calculation: SurvivalCalculation, name: String) {
            val censoredPatients = listOf(1, 2, 3, 4, 5).map { i ->
                val survivalDays = i * 365
                patientWithSurvivalDays(
                    osDays = if (name.contains("OS")) survivalDays ?:0 else 0,
                    pfsDays = if (name.contains("PFS")) survivalDays else null,
                    hadEvent = false
                )
            }
            val eligiblePatients = survivalList.map { data ->
                patientWithSurvivalDays(
                    osDays = if (name.contains("OS")) data.osDays ?:0 else 0,
                    pfsDays = if (name.contains("PFS")) data.pfsDays else null,
                    hadEvent = true
                )
            }
            val patients = eligiblePatients + censoredPatients

            val measurement = calculation.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(measurement.value).describedAs("Censored values median for $name").isEqualTo(800.0)
            assertThat(measurement.numPatients).describedAs("Total patients for  $name with censored values").isEqualTo(12)
            assertThat(measurement.min).describedAs("Minimum survival for $name with censored values").isEqualTo(25)
            assertThat(measurement.max).describedAs("Maximum survival for $name with censored values").isEqualTo(1825)
            assertThat(measurement.iqr).describedAs("IQR for $name with censored values").isEqualTo(1500.0)
        }
    }

    private fun patientWithSurvivalDays(
        osDays: Int= 0,
        pfsDays: Int? = null,
        hadEvent: Boolean? = true
    ): DiagnosisEpisode {
        val diagnosis = DIAGNOSIS_EPISODE.diagnosis.copy(
            observedOsFromTumorIncidenceDays = osDays,
            hadSurvivalEvent = hadEvent
        )
        val systemicTreatmentPlan = SystemicTreatmentPlan(
            treatment = Treatment.FLUOROURACIL,
            systemicTreatmentSchemes = emptyList(),
            observedPfsDays = pfsDays,
            hadProgressionEvent = hadEvent
        )
        val episode = DIAGNOSIS_EPISODE.episode.copy(
            systemicTreatmentPlan = systemicTreatmentPlan
        )
        return DiagnosisEpisode(diagnosis, episode)
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
