package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.diagnosis.Metastasis
import com.hartwig.actin.personalization.datamodel.diagnosis.NumberOfLiverMetastases
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrMetastaticDiagnosisExtractorTest {

    @Test
    fun `Should extract metastatic diagnosis from minimal NCR record`() {
        val metastaticDiagnosis = NcrMetastaticDiagnosisExtractor.extract(TestNcrRecordFactory.minimalEntryRecords())

        assertThat(metastaticDiagnosis.isMetachronous).isFalse()
    }

    @Test
    fun `Should extract metastatic diagnosis from proper set of NCR records`() {
        val metastaticDiagnosis = NcrMetastaticDiagnosisExtractor.extract(TestNcrRecordFactory.properEntryRecords())

        with(metastaticDiagnosis) {
            assertThat(isMetachronous).isTrue()
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
            assertThat(clinicalTnmClassification).isNull()
            assertThat(pathologicalTnmClassification).isEqualTo(TnmClassification(tnmT = null, tnmN = null, tnmM = TnmM.M1))
            assertThat(investigatedLymphNodesCount).isEqualTo(4)
            assertThat(positiveLymphNodesCount).isEqualTo(2)
        }
    }
}