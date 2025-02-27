package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.diagnosis.Metastasis
import com.hartwig.actin.personalization.datamodel.diagnosis.NumberOfLiverMetastases
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrMetastaticDiagnosisExtractorTest {

    @Test
    fun `Should extract metastatic diagnosis from minimal NCR record`() {
        val metastaticDiagnosis = NcrMetastaticDiagnosisExtractor.extract(TestNcrRecordFactory.minimalTumorRecords())

        assertThat(metastaticDiagnosis).isNotNull()
    }

    @Test
    fun `Should extract metastatic diagnosis from proper set of NCR records`() {
        val metastaticDiagnosis = NcrMetastaticDiagnosisExtractor.extract(TestNcrRecordFactory.properTumorRecords())

        with(metastaticDiagnosis) {
            assertThat(distantMetastasesDetectionStatus).isEqualTo(MetastasesDetectionStatus.AT_START)
            assertThat(metastases).containsExactly(
                Metastasis(
                    daysSinceDiagnosis = 200,
                    location = TumorLocation.ADRENAL_CORTEX,
                    isLinkedToProgression = false
                ), Metastasis(
                    daysSinceDiagnosis = null,
                    location = TumorLocation.BRAIN_NOS,
                    isLinkedToProgression = null
                )
            )
            assertThat(numberOfLiverMetastases).isEqualTo(NumberOfLiverMetastases.FIVE_OR_MORE)
            assertThat(maximumSizeOfLiverMetastasisMm).isEqualTo(15)
            assertThat(investigatedLymphNodesCount).isEqualTo(3)
            assertThat(positiveLymphNodesCount).isEqualTo(1)
        }
    }
}