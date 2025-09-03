package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PrimaryTumorRecordFilterTest {
    
    private val filter = PrimaryTumorRecordFilter(true)
    private val diagnosisRecord = TestNcrRecordFactory.minimalDiagnosisRecord()
    private val followUpRecord = TestNcrRecordFactory.minimalFollowupRecord()

    @Test
    fun `Should return true for valid double tumor data`() {
        val records = listOf(
            diagnosisRecord.copy(
                clinicalCharacteristics = diagnosisRecord.clinicalCharacteristics.copy(dubbeltum = 1)
            ),
            followUpRecord.copy(
                clinicalCharacteristics = followUpRecord.clinicalCharacteristics.copy(dubbeltum = 0)
            )
        )
        assertThat(filter.hasValidDoubleTumorData(records)).isTrue()
    }

    @Test
    fun `Should return false for no double tumor data in primary diagnosis`() {
        val records = listOf(
            diagnosisRecord.copy(
                clinicalCharacteristics = diagnosisRecord.clinicalCharacteristics.copy(dubbeltum = null)
            ),
            followUpRecord.copy(
                clinicalCharacteristics = followUpRecord.clinicalCharacteristics.copy(dubbeltum = null)
            )
        )
        assertThat(filter.hasValidDoubleTumorData(records)).isFalse()
    }

    @Test
    fun `Should return false for double tumor data present in follow-up diagnosis`() {
        val records = listOf(
            diagnosisRecord.copy(
                clinicalCharacteristics = diagnosisRecord.clinicalCharacteristics.copy(dubbeltum = 1)
            ),
            followUpRecord.copy(
                clinicalCharacteristics = followUpRecord.clinicalCharacteristics.copy(dubbeltum = 1)
            )
        )
        assertThat(filter.hasValidDoubleTumorData(records)).isFalse()
    }

    @Test
    fun `Should return true for consistent topoSublok data`() {
        val records = listOf(
            diagnosisRecord.copy(
                primaryDiagnosis = diagnosisRecord.primaryDiagnosis.copy(topoSublok = "C000")
            ),
            followUpRecord.copy(
                primaryDiagnosis = followUpRecord.primaryDiagnosis.copy(topoSublok = "C000")
            )
        )
        assertThat(filter.hasConsistentTopoSublokData(records)).isTrue()
    }

    @Test
    fun `Should return false for inconsistent topoSublok data`() {
        val records = listOf(
            diagnosisRecord.copy(
                primaryDiagnosis = diagnosisRecord.primaryDiagnosis.copy(topoSublok = "C000")
            ),
            followUpRecord.copy(
                primaryDiagnosis = followUpRecord.primaryDiagnosis.copy(topoSublok = "C001")
            )
        )
        assertThat(filter.hasConsistentTopoSublokData(records)).isFalse()
    }
}
