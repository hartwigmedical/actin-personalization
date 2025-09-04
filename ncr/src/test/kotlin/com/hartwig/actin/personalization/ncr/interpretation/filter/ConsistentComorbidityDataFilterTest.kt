package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConsistentComorbidityDataFilterTest {

    private val filter = ConsistentComorbidityDataFilter(true)
    private val minimalDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord()
    private val minimalFollowup = TestNcrRecordFactory.minimalFollowupRecord()

    @Test
    fun `Should returns true when all follow-up records have no comorbidity`() {
        val records = listOf(
            minimalFollowup.copy(
                comorbidities = minimalFollowup.comorbidities.copy(
                    cciPvd = null, cciRenal = null, cciSevereLiver = null, cciUlcer = null
                )
            )
        )
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should returns false when any follow-up record has comorbidity`() {
        val records = listOf(
            minimalFollowup.copy(
                comorbidities = minimalFollowup.comorbidities.copy(
                    cciPvd = 1, cciRenal = null, cciSevereLiver = null, cciUlcer = null
                )
            )
        )
        assertThat(filter.apply(records)).isFalse()
    }

    @Test
    fun `Should returns true when all records are complete or empty`() {
        val records = listOf(
            minimalDiagnosis.copy(
                comorbidities = minimalDiagnosis.comorbidities.copy(
                    cci = 1,
                    cciAids = 1,
                    cciCat = 1,
                    cciChf = 1,
                    cciCollagenosis = 1,
                    cciCopd = 1,
                    cciCvd = 1,
                    cciDementia = 1,
                    cciDm = 1,
                    cciEodDm = 1,
                    cciMalignancy = 1,
                    cciMetastatic = 1,
                    cciMi = 1,
                    cciMildLiver = 1,
                    cciPlegia = 1,
                    cciPvd = 1,
                    cciRenal = 1,
                    cciSevereLiver = 1,
                    cciUlcer = 1
                )
            ),
            minimalDiagnosis
        )
        assertThat(filter.apply(records)).isTrue()
    }

    @Test
    fun `Should returns false when any record is partially complete`() {
        val records = listOf(
            minimalDiagnosis.copy(
                comorbidities = minimalDiagnosis.comorbidities.copy(
                    cciPvd = 1, cciRenal = null, cciSevereLiver = 1, cciUlcer = 1
                )
            )
        )
        assertThat(filter.apply(records)).isFalse()
    }
}

