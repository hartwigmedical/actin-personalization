package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.TestReferenceEntryFactory
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetastasisDetectionTest {

    @Test
    fun `Should determine interval between primary and metastatic diagnosis for metachronous entries`() {
        val withMetastases = TestDatamodelFactory.metastaticDiagnosis(
            isMetachronous = true,
            metastases = listOf(
                TestDatamodelFactory.metastasis(daysSinceDiagnosis = 20),
                TestDatamodelFactory.metastasis(daysSinceDiagnosis = 30)
            )
        )
        assertThat(MetastasisDetection.determineDaysBetweenPrimaryAndMetastaticDiagnosis(entry(withMetastases))).isEqualTo(20)

        val withoutMetastases = TestDatamodelFactory.metastaticDiagnosis(isMetachronous = true, metastases = emptyList())
        assertThat(MetastasisDetection.determineDaysBetweenPrimaryAndMetastaticDiagnosis(entry(withoutMetastases))).isEqualTo(null)
    }

    @Test
    fun `Should determine interval between primary and metastatic diagnosis for synchronous entries`() {
        val withMetastases = TestDatamodelFactory.metastaticDiagnosis(
            isMetachronous = false,
            metastases = listOf(
                TestDatamodelFactory.metastasis(daysSinceDiagnosis = 20),
                TestDatamodelFactory.metastasis(daysSinceDiagnosis = 30)
            )
        )
        assertThat(MetastasisDetection.determineDaysBetweenPrimaryAndMetastaticDiagnosis(entry(withMetastases))).isEqualTo(0)

        val withoutMetastases = TestDatamodelFactory.metastaticDiagnosis(isMetachronous = false, metastases = emptyList())
        assertThat(MetastasisDetection.determineDaysBetweenPrimaryAndMetastaticDiagnosis(entry(withoutMetastases))).isEqualTo(0)
    }

    private fun entry(metastaticDiagnosis: MetastaticDiagnosis): ReferenceEntry {
        return TestReferenceEntryFactory.minimalReferenceEntry().copy(metastaticDiagnosis = metastaticDiagnosis)
    }
}