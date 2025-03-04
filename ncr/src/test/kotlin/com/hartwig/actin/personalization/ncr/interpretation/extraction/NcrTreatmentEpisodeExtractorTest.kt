package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrTreatmentEpisodeExtractorTest {

    @Test
    fun `Should extract treatment episodes from minimal NCR record`() {
        val treatmentEpisodes = NcrTreatmentEpisodeExtractor.extract(listOf(TestNcrRecordFactory.minimalDiagnosisRecord()))

        assertThat(treatmentEpisodes).hasSize(1)
    }

    @Test
    fun `Should extract treatment episodes from proper NCR record`() {
        val treatmentEpisodes = NcrTreatmentEpisodeExtractor.extract(TestNcrRecordFactory.properTumorRecords())

        assertThat(treatmentEpisodes).hasSize(3)
    }
}