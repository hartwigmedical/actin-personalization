package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidVitalStatusFilterTest {

    private val filter = ValidVitalStatusFilter(true)
    private val diagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()
    private val followup = TestNcrRecordFactory.minimalFollowupRecord()

    @Test
    fun `Should return true when all diagnosis records have vital status`() {
        val records = listOf(diagnosis.copy(patientCharacteristics = diagnosis.patientCharacteristics.copy(vitStat = 1, vitStatInt = 1)))

        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false when any diagnosis record is missing vital status`() {
        val records = listOf(
            diagnosis.copy(patientCharacteristics = diagnosis.patientCharacteristics.copy(vitStat = null, vitStatInt = null))
        )

        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should return true when all followup records have empty vital status`() {
        val records = listOf(
            followup.copy(patientCharacteristics = followup.patientCharacteristics.copy(vitStat = null, vitStatInt = null))
        )

        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should return false when any followup record has vital status`() {
        val records = listOf(
            followup.copy(patientCharacteristics = followup.patientCharacteristics.copy(vitStat = 1, vitStatInt = 1))
        )

        assertThat(filter.apply(records)).isFalse()
    }
}