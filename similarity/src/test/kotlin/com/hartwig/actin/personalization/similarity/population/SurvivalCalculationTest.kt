package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.similarity.DIAGNOSIS_AND_EPISODE
import com.hartwig.actin.personalization.similarity.episodeWithTreatment
import com.hartwig.actin.personalization.similarity.report.TableElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import weka.core.pmml.jaxbbindings.False
import weka.core.pmml.jaxbbindings.True

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

    private val survivalCalculation = SurvivalCalculation<Diagnosis>(
        timeFunction = Diagnosis::observedOsFromTumorIncidenceDays,
        eventFunction = Diagnosis::hadSurvivalEvent,
        title = "Overall survival",
        extractor = { it.first }
    )

    @Nested
    inner class EligibilityTests {
        @Test
        fun `Should evaluate patients as eligible when observed survival days is not null`() { //TODO: rename test
            assertThat(OS_CALCULATION.isEligible(DIAGNOSIS_AND_EPISODE)).isFalse
            assertThat(OS_CALCULATION.isEligible(patientWithSurvivalDays(osDays = null,  hadSurvivalEvent = true))).isFalse
            assertThat(OS_CALCULATION.isEligible(patientWithSurvivalDays(osDays = 100,  hadSurvivalEvent = true ))).isTrue

            assertThat(PFS_CALCULATION.isEligible(DIAGNOSIS_AND_EPISODE)).isFalse
            assertThat(PFS_CALCULATION.isEligible(patientWithSurvivalDays(pfsDays = null, hadProgressionEvent = true))).isFalse
            assertThat(PFS_CALCULATION.isEligible(patientWithSurvivalDays(pfsDays = null, hadProgressionEvent = false))).isTrue
            assertThat(PFS_CALCULATION.isEligible(patientWithSurvivalDays(pfsDays = 100, hadProgressionEvent = true))).isTrue
        }
    }

    @Nested
    inner class CalculationTests {
        @Test
        fun `Should evaluate median survival for patient list`() {
            val osMeasurement = OS_CALCULATION.calculate(
                survivalList.map { data -> patientWithSurvivalDays(osDays = data.osDays) }, ELIGIBLE_SUB_POPULATION_SIZE
            )
            assertThat(osMeasurement.value).isEqualTo(200.0)
            assertThat(osMeasurement.numPatients).isEqualTo(7)
            assertThat(osMeasurement.min).isEqualTo(25)
            assertThat(osMeasurement.max).isEqualTo(1600)
            assertThat(osMeasurement.iqr).isEqualTo(750.0)

            val pfsMeasurement = PFS_CALCULATION.calculate(
                survivalList.map { data -> patientWithSurvivalDays(pfsDays = data.pfsDays) }, ELIGIBLE_SUB_POPULATION_SIZE
            )
            assertThat(pfsMeasurement.value).isEqualTo(200.0)
            assertThat(pfsMeasurement.numPatients).isEqualTo(7)
            assertThat(pfsMeasurement.min).isEqualTo(25)
            assertThat(pfsMeasurement.max).isEqualTo(1600)
            assertThat(pfsMeasurement.iqr).isEqualTo(750.0)
        }

        @Test
        fun `Should incorporate censored values in survival calculation`() {
            val censoredPatients = listOf(1, 2, 3, 4, 5).map { patientWithSurvivalDays(osDays = it * 365, pfsDays = it * 365) }
            val patients = survivalList.map { data ->
                patientWithSurvivalDays(osDays = data.osDays, pfsDays = data.pfsDays)
            } + censoredPatients

            val osMeasurement = OS_CALCULATION.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(osMeasurement.value).isEqualTo(400.0)
            assertThat(osMeasurement.numPatients).isEqualTo(12)
            assertThat(osMeasurement.min).isEqualTo(25)
            assertThat(osMeasurement.max).isEqualTo(1825)
            assertThat(osMeasurement.iqr).isEqualTo(995.0)

            val pfsMeasurement = PFS_CALCULATION.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
            assertThat(pfsMeasurement.value).isEqualTo(400.0)
            assertThat(pfsMeasurement.numPatients).isEqualTo(12)
            assertThat(pfsMeasurement.min).isEqualTo(25)
            assertThat(pfsMeasurement.max).isEqualTo(1825)
            assertThat(pfsMeasurement.iqr).isEqualTo(995.0)
        }
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

    private fun patientWithSurvivalDays(
        osDays: Int? = 200,
        pfsDays: Int? = 200,
        hadSurvivalEvent: Boolean = true,
        hadProgressionEvent: Boolean = true
    ): DiagnosisAndEpisode {
        return episodeWithTreatment(
            treatment = Treatment.FLUOROURACIL,
            pfs = pfsDays,
            os = osDays,
            hadSurvivalEvent = hadSurvivalEvent,
            hadProgressionEvent = hadProgressionEvent,
            diagnosis = DIAGNOSIS_AND_EPISODE.first.copy(
                observedOsFromTumorIncidenceDays = osDays,
                hadSurvivalEvent = hadSurvivalEvent
            )

        )
    }


}