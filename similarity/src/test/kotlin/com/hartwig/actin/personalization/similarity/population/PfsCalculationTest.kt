package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.SystemicTreatmentPlan
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.similarity.DIAGNOSIS_AND_EPISODE
import com.hartwig.actin.personalization.similarity.report.TableElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ELIGIBLE_SUB_POPULATION_SIZE = 50

class PfsCalculationTest {

    @Test
    fun `Should evaluate patients as eligible when pfs is not null`() {
        assertThat(PfsCalculation.isEligible(DIAGNOSIS_AND_EPISODE)).isFalse
        assertThat(PfsCalculation.isEligible(patientWithPfs(null))).isFalse
        assertThat(PfsCalculation.isEligible(patientWithPfs(100))).isTrue
    }

    @Test
    fun `Should evaluate PFS as null for patient list with no non-null PFS`() {
        assertEmptyMeasurement(PfsCalculation.calculate(emptyList(), ELIGIBLE_SUB_POPULATION_SIZE))
        assertEmptyMeasurement(PfsCalculation.calculate(listOf(DIAGNOSIS_AND_EPISODE, patientWithPfs(null)), ELIGIBLE_SUB_POPULATION_SIZE))
    }

    @Test
    fun `Should evaluate PFS as only value for patient list with one non-null PFS`() {
        val pfs = 100
        val measurement = PfsCalculation.calculate(listOf(patientWithPfs(pfs), DIAGNOSIS_AND_EPISODE), ELIGIBLE_SUB_POPULATION_SIZE)
        assertThat(measurement.value).isEqualTo(pfs.toDouble())
        assertThat(measurement.numPatients).isEqualTo(1)
        assertThat(measurement.min).isEqualTo(pfs)
        assertThat(measurement.max).isEqualTo(pfs)
        assertThat(measurement.iqr).isNaN
    }

    @Test
    fun `Should evaluate PFS as median for patient list with 2 non-null PFS values`() {
        val pfsList = listOf(100, 200, null)
        val measurement = PfsCalculation.calculate(pfsList.map(::patientWithPfs), ELIGIBLE_SUB_POPULATION_SIZE)
        assertThat(measurement.value).isEqualTo(150.0)
        assertThat(measurement.numPatients).isEqualTo(2)
        assertThat(measurement.min).isEqualTo(100)
        assertThat(measurement.max).isEqualTo(200)
        assertThat(measurement.iqr).isEqualTo(100.0)
    }

    @Test
    fun `Should evaluate PFS as median for patient list with multiple non-null PFS`() {
        val pfsList = listOf(100, 200, 300, 400, 500, null)
        val measurement = PfsCalculation.calculate(pfsList.map(::patientWithPfs), ELIGIBLE_SUB_POPULATION_SIZE)
        assertThat(measurement.value).isEqualTo(300.0)
        assertThat(measurement.numPatients).isEqualTo(5)
        assertThat(measurement.min).isEqualTo(100)
        assertThat(measurement.max).isEqualTo(500)
        assertThat(measurement.iqr).isEqualTo(300.0)
    }

    @Test
    fun `Should create table elements with all available information`() {
        assertThat(PfsCalculation.createTableElement(Measurement(Double.NaN, 0))).isEqualTo(TableElement.regular("-"))
        assertThat(PfsCalculation.createTableElement(Measurement(100.0, 10))).isEqualTo(TableElement.regular("n≤20"))
        assertThat(PfsCalculation.createTableElement(Measurement(100.0, 50))).isEqualTo(TableElement("100.0", "\n(n=50)"))
        assertThat(PfsCalculation.createTableElement(Measurement(100.0, 50, 50, 150, Double.NaN)))
            .isEqualTo(TableElement("100.0", "\n(n=50)"))
        assertThat(PfsCalculation.createTableElement(Measurement(100.0, 50, 50, 150, 100.0)))
            .isEqualTo(TableElement("100.0", ", IQR: 100.0\n(n=50)"))
    }

    private fun assertEmptyMeasurement(measurement: Measurement) {
        assertThat(measurement.value).isNaN
        assertThat(measurement.numPatients).isEqualTo(0)
        assertThat(measurement.min).isNull()
        assertThat(measurement.max).isNull()
        assertThat(measurement.iqr).isNaN
    }

    private fun patientWithPfs(pfs: Int?): DiagnosisAndEpisode {
        return DIAGNOSIS_AND_EPISODE.copy(
            second = DIAGNOSIS_AND_EPISODE.second.copy(
                systemicTreatmentPlan = SystemicTreatmentPlan(
                    treatment = Treatment.CAPECITABINE,
                    systemicTreatmentSchemes = emptyList(),
                    pfs = pfs
                )
            )
        )
    }
}