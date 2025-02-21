package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrPrimaryDiagnosisExtractorTest {

    @Test
    fun `Should construct primary diagnosis for minimal NCR record`() {
        assertThat(NcrPrimaryDiagnosisExtractor.extract(listOf(TestNcrRecordFactory.minimalDiagnosisRecord()))).isNotNull()
    }
}