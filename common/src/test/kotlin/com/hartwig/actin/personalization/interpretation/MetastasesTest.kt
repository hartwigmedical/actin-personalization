package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetastasesTest {

    @Test
    fun `Should determine interval between primary and metastatic diagnosis for metachronous entries`() {
        val withMetastases = TestDatamodelFactory.metastaticDiagnosis(
            isMetachronous = true,
            metastases = listOf(
                TestDatamodelFactory.metastasis(daysSinceDiagnosis = null),
                TestDatamodelFactory.metastasis(daysSinceDiagnosis = 30),
                TestDatamodelFactory.metastasis(daysSinceDiagnosis = 20)
            )
        )
        assertThat(Metastases.daysBetweenPrimaryAndMetastaticDiagnosis(withMetastases)).isEqualTo(20)

        val withoutMetastases = TestDatamodelFactory.metastaticDiagnosis(isMetachronous = true, metastases = emptyList())
        assertThat(Metastases.daysBetweenPrimaryAndMetastaticDiagnosis(withoutMetastases)).isEqualTo(null)
    }

    @Test
    fun `Should determine interval between primary and metastatic diagnosis for synchronous entries`() {
        val withMetastases = TestDatamodelFactory.metastaticDiagnosis(
            isMetachronous = false,
            metastases = listOf(
                TestDatamodelFactory.metastasis(daysSinceDiagnosis = 20),
                TestDatamodelFactory.metastasis(daysSinceDiagnosis = 30),
                TestDatamodelFactory.metastasis(daysSinceDiagnosis = null)
            )
        )
        assertThat(Metastases.daysBetweenPrimaryAndMetastaticDiagnosis(withMetastases)).isEqualTo(0)

        val withoutMetastases = TestDatamodelFactory.metastaticDiagnosis(isMetachronous = false, metastases = emptyList())
        assertThat(Metastases.daysBetweenPrimaryAndMetastaticDiagnosis(withoutMetastases)).isEqualTo(0)
    }
}