package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.TestReferencePatientFactory
import com.hartwig.actin.personalization.similarity.report.TableElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ELIGIBLE_SUB_POPULATION_SIZE = 100

class TreatmentDecisionCalculationTest {

    private val tumor = TestReferencePatientFactory.minimalReferencePatient().tumors.first()
    
    @Test
    fun `Should evaluate all patients as eligible`() {
        assertThat(TreatmentDecisionCalculation.isEligible(tumor)).isTrue
    }

    @Test
    fun `Should calculate measurement as percentage of population`() {
        val patients = listOf(tumor, tumor, tumor)
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