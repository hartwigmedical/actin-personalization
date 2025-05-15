package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.TestReferenceEntryFactory
import com.hartwig.actin.personalization.similarity.report.TableElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ELIGIBLE_SUB_POPULATION_SIZE = 100

class TreatmentDecisionCalculationTest {

    private val entry = TestReferenceEntryFactory.minimalReferenceEntry()
    
    @Test
    fun `Should evaluate all reference entries as eligible`() {
        assertThat(TreatmentDecisionCalculation.isEligible(entry)).isTrue
    }

    @Test
    fun `Should calculate measurement as percentage of population`() {
        val entries = listOf(entry, entry, entry)
        val measurement = TreatmentDecisionCalculation.calculate(entries, ELIGIBLE_SUB_POPULATION_SIZE)
        assertThat(measurement.value).isEqualTo(0.03)
        assertThat(measurement.numEntries).isEqualTo(3)
    }

    @Test
    fun `Should create table element with percentage of population`() {
        val measurement = Measurement(0.03, 3)
        val tableElement = TreatmentDecisionCalculation.createTableElement(measurement)
        assertThat(tableElement).isEqualTo(TableElement.regular("3.0%"))
    }
}