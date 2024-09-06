package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.similarity.DIAGNOSIS_AND_EPISODE
import com.hartwig.actin.personalization.similarity.report.TableElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ELIGIBLE_SUB_POPULATION_SIZE = 50
private val pfsList = listOf(400, 50, 800, 100, 25, 1600, 200)

class PfsCalculationTest {

    @Test
    fun `Should evaluate patients as eligible when observed PFS is not null`() {
        assertThat(PfsCalculation.isEligible(DIAGNOSIS_AND_EPISODE)).isFalse
        assertThat(PfsCalculation.isEligible(patientWithPfs(null))).isFalse
        assertThat(PfsCalculation.isEligible(patientWithPfs(100))).isTrue
    }

    @Test
    fun `Should evaluate median PFS for patient list`() {
        val measurement = PfsCalculation.calculate(pfsList.map(::patientWithPfs), ELIGIBLE_SUB_POPULATION_SIZE)
        assertThat(measurement.value).isEqualTo(200.0)
        assertThat(measurement.numPatients).isEqualTo(7)
        assertThat(measurement.min).isEqualTo(25)
        assertThat(measurement.max).isEqualTo(1600)
        assertThat(measurement.iqr).isEqualTo(750.0)
    }

    @Test
    fun `Should incorporate censored values in PFS calculation`() {
        // 25, 50, 100, 200, 365, 400, 730, 800, 1095, 1460, 1600, 1825
        val censoredPatients = listOf(1, 2, 3, 4, 5).map { patientWithPfs(it * 365, false) }
        val patients = pfsList.map(::patientWithPfs) + censoredPatients
        val measurement = PfsCalculation.calculate(patients, ELIGIBLE_SUB_POPULATION_SIZE)
        assertThat(measurement.value).isEqualTo(800.0)
        assertThat(measurement.numPatients).isEqualTo(12)
        assertThat(measurement.min).isEqualTo(25)
        assertThat(measurement.max).isEqualTo(1600)
        assertThat(measurement.iqr).isEqualTo(1500.0)
    }

    @Test
    fun `Should create table elements with all available information`() {
        assertThat(PfsCalculation.createTableElement(Measurement(Double.NaN, 0))).isEqualTo(TableElement.regular("n≤20"))
        assertThat(PfsCalculation.createTableElement(Measurement(100.0, 10))).isEqualTo(TableElement.regular("n≤20"))
        assertThat(PfsCalculation.createTableElement(Measurement(100.0, 50))).isEqualTo(TableElement("100.0", "\n(n=50)"))
        assertThat(PfsCalculation.createTableElement(Measurement(100.0, 50, 50, 150, Double.NaN)))
            .isEqualTo(TableElement("100.0", "\n(n=50)"))
        assertThat(PfsCalculation.createTableElement(Measurement(100.0, 50, 50, 150, 100.0)))
            .isEqualTo(TableElement("100.0", ", IQR: 100.0\n(n=50)"))
    }

    @Test
    fun `Should return empty measurement for empty population`() {
        val measurement = PfsCalculation.calculate(emptyList(), ELIGIBLE_SUB_POPULATION_SIZE)
        assertThat(measurement.value).isNaN
        assertThat(measurement.numPatients).isEqualTo(0)
        assertThat(measurement.min).isNull()
        assertThat(measurement.max).isNull()
        assertThat(measurement.iqr).isNaN
    }

    private fun patientWithPfs(pfs: Int?, hadProgressionEvent: Boolean = true): DiagnosisAndEpisode {
        return DIAGNOSIS_AND_EPISODE.copy(
            second = DIAGNOSIS_AND_EPISODE.second.copy(
                systemicTreatmentPlan = SystemicTreatmentPlan(
                    treatment = Treatment.CAPECITABINE,
                    systemicTreatmentSchemes = emptyList(),
                    observedPfsDays = pfs,
                    hadProgressionEvent = hadProgressionEvent
                )
            )
        )
    }
}