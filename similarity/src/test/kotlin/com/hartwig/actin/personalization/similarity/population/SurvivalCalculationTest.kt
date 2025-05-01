package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.similarity.report.TableElement
import com.hartwig.actin.personalization.similarity.tumorWithTreatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val ELIGIBLE_SUB_POPULATION_SIZE = 50

data class SurvivalData(val osDays: Int?, val pfsDays: Int?)
enum class SurvivalType { OS, PFS }

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

    private val survivalCalculation = OS_CALCULATION

    private val survivalCalculationsFunctions = listOf(
        OS_CALCULATION to SurvivalType.OS,
        PFS_CALCULATION to SurvivalType.PFS
    )

    @Nested
    inner class SurvivalTests {

        @Test
        fun `Eligibility tests for OS and PFS`() {
            survivalCalculationsFunctions.forEach { (calculation, type) ->
                testEligibility(calculation, type)
            }
        }

        @Test
        fun `Median survival tests for OS and PFS`() {
            survivalCalculationsFunctions.forEach { (calculation, type) ->
                testMedianSurvival(calculation, type)
            }
        }

        @Test
        fun `Censored value tests for OS and PFS`() {
            survivalCalculationsFunctions.forEach { (calculation, type) ->
                testCensoredValues(calculation, type)
            }
        }

        private fun testEligibility(calculation: SurvivalCalculation, type: SurvivalType) {
            val createPatient: (Int?, Boolean) -> Tumor = { days, hadEvent ->
                patientWithSurvivalDays(
                    osDays = if (type == SurvivalType.OS) days ?: 0 else 0,
                    pfsDays = if (type == SurvivalType.PFS) days else null,
                    hadEvent = hadEvent
                )
            }
            assertThat(calculation.isEligible(createPatient(100, true))).describedAs("Eligibility for $type").isTrue()
            assertThat(calculation.isEligible(createPatient(100, false))).describedAs("Eligibility for $type").isTrue()

            if (type == SurvivalType.PFS) {
                assertThat(calculation.isEligible(createPatient(null, true))).describedAs("Eligibility for $type with null days").isFalse()
            }
        }


        private fun testMedianSurvival(calculation: SurvivalCalculation, type: SurvivalType) {
            val patients = survivalList.map { data ->
                patientWithSurvivalDays(
                    osDays = if (type == SurvivalType.OS) data.osDays ?: 0 else 0,
                    pfsDays = if (type == SurvivalType.PFS) data.pfsDays else null,
                    hadEvent = true
                )
            }
            val measurement = calculation.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(measurement.value).describedAs("Median survival value for $type").isEqualTo(200.0)
            assertThat(measurement.numPatients).describedAs("Number of patients for $type").isEqualTo(7)
            assertThat(measurement.min).describedAs("Minimum survival for $type").isEqualTo(25)
            assertThat(measurement.max).describedAs("Maximum survival for $type").isEqualTo(1600)
            assertThat(measurement.iqr).describedAs("IQR for $type").isEqualTo(750.0)
        }

        private fun testCensoredValues(calculation: SurvivalCalculation, type: SurvivalType) {
            val censoredPatients = listOf(1, 2, 3, 4, 5).map { i ->
                val survivalDays = i * 365
                patientWithSurvivalDays(
                    osDays = if (type == SurvivalType.OS) survivalDays else 0,
                    pfsDays = if (type == SurvivalType.PFS) survivalDays else null,
                    hadEvent = false
                )
            }
            val eligiblePatients = survivalList.map { data ->
                patientWithSurvivalDays(
                    osDays = if (type == SurvivalType.OS) data.osDays ?: 0 else 0,
                    pfsDays = if (type == SurvivalType.PFS) data.pfsDays else null,
                    hadEvent = true
                )
            }
            val patients = eligiblePatients + censoredPatients

            val measurement = calculation.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(measurement.value).describedAs("Censored values median for $type").isEqualTo(800.0)
            assertThat(measurement.numPatients).describedAs("Total patients for  $type with censored values").isEqualTo(12)
            assertThat(measurement.min).describedAs("Minimum survival for $type with censored values").isEqualTo(25)
            assertThat(measurement.max).describedAs("Maximum survival for $type with censored values").isEqualTo(1600)
            assertThat(measurement.iqr).describedAs("IQR for $type with censored values").isEqualTo(1500.0)
        }
    }

    private fun patientWithSurvivalDays(
        osDays: Int = 0,
        pfsDays: Int? = null,
        hadEvent: Boolean = true
    ): Tumor {
        return tumorWithTreatment(treatment = Treatment.FLUOROURACIL,
            pfsDays = pfsDays,
            planStart = null,
            osDays = osDays,
            hadSurvivalEvent = hadEvent,
            hadProgressionEvent = hadEvent)
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
