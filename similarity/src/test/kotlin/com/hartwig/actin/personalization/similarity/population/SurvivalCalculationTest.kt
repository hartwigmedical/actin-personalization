package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.similarity.SimilarityTestFactory
import com.hartwig.actin.personalization.similarity.report.TableElement
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
        
//        @Test
//        fun `Should evaluate eligibility for OS and PFS`() {
//            survivalCalculationsFunctions.forEach { (calculation, type) ->
//                testEligibility(calculation, type)
//            }
//        }

//        @Test
//        fun `Should evaluate median survival for OS and PFS`() {
//            survivalCalculationsFunctions.forEach { (calculation, type) ->
//                testMedianSurvival(calculation, type)
//            }
//        }

        private fun testEligibility(calculation: SurvivalCalculation, type: SurvivalType) {
            val createEntry: (Int?, Boolean) -> ReferenceEntry = { days, hadEvent ->
                entryWithSurvivalDays(
                    osDays = if (type == SurvivalType.OS) days ?: 0 else 0,
                    pfsDays = if (type == SurvivalType.PFS) days else null,
                    hadEvent = hadEvent
                )
            }
            assertThat(calculation.isEligible(createEntry(100, true))).describedAs("Eligibility for $type").isTrue()
            assertThat(calculation.isEligible(createEntry(100, false))).describedAs("Eligibility for $type").isTrue()

            if (type == SurvivalType.PFS) {
                assertThat(calculation.isEligible(createEntry(null, true))).describedAs("Eligibility for $type with null days").isFalse()
            }
        }

        private fun testMedianSurvival(calculation: SurvivalCalculation, type: SurvivalType) {
            val entries = survivalList.map { data ->
                entryWithSurvivalDays(
                    osDays = if (type == SurvivalType.OS) data.osDays ?: 0 else 0,
                    pfsDays = if (type == SurvivalType.PFS) data.pfsDays else null,
                    hadEvent = true
                )
            }
            val measurement = calculation.calculate(entries, ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(measurement.value).describedAs("Median survival value for $type").isEqualTo(200.0)
            assertThat(measurement.numEntries).describedAs("Number of patients for $type").isEqualTo(7)
            assertThat(measurement.min).describedAs("Minimum survival for $type").isEqualTo(25)
            assertThat(measurement.max).describedAs("Maximum survival for $type").isEqualTo(1600)
            assertThat(measurement.iqr).describedAs("IQR for $type").isEqualTo(750.0)
        }
    }

    private fun entryWithSurvivalDays(
        osDays: Int = 0,
        pfsDays: Int? = null,
        hadEvent: Boolean = true
    ): ReferenceEntry {
        return SimilarityTestFactory.createEntry(isAlive = !hadEvent,
            daysBetweenDiagnosisAndSurvivalMeasurement = osDays,
            systemicTreatment = Treatment.FLUOROURACIL,
            systemicTreatmentStart = null,
            daysBetweenDiagnosisAndProgression = pfsDays,
            hasProgressionEvent = hadEvent)
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
        assertThat(measurement.numEntries).isEqualTo(0)
        assertThat(measurement.min).isNull()
        assertThat(measurement.max).isNull()
        assertThat(measurement.iqr).isNaN()
    }
}
