package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetastaticInterpreterTest {

    @Test
    fun `Should determine interval between primary and metastatic diagnosis for metachronous entries`() {
        val withMetastases = MetastaticInterpreter(
            metastaticDiagnosis = TestDatamodelFactory.metastaticDiagnosis(
                isMetachronous = true,
                metastases = listOf(
                    TestDatamodelFactory.metastasis(daysSinceDiagnosis = null),
                    TestDatamodelFactory.metastasis(daysSinceDiagnosis = 30),
                    TestDatamodelFactory.metastasis(daysSinceDiagnosis = 20)
                )
            )
        )
        assertThat(withMetastases.daysBetweenPrimaryAndMetastaticDiagnosis()).isEqualTo(20)

        val withoutMetastases =
            MetastaticInterpreter(TestDatamodelFactory.metastaticDiagnosis(isMetachronous = true, metastases = emptyList()))
        assertThat(withoutMetastases.daysBetweenPrimaryAndMetastaticDiagnosis()).isEqualTo(null)
    }

    @Test
    fun `Should determine interval between primary and metastatic diagnosis for synchronous entries`() {
        val withMetastases = MetastaticInterpreter(
            TestDatamodelFactory.metastaticDiagnosis(
                isMetachronous = false,
                metastases = listOf(
                    TestDatamodelFactory.metastasis(daysSinceDiagnosis = 20),
                    TestDatamodelFactory.metastasis(daysSinceDiagnosis = 30),
                    TestDatamodelFactory.metastasis(daysSinceDiagnosis = null)
                )
            )
        )
        assertThat(withMetastases.daysBetweenPrimaryAndMetastaticDiagnosis()).isEqualTo(0)

        val withoutMetastases =
            MetastaticInterpreter(TestDatamodelFactory.metastaticDiagnosis(isMetachronous = false, metastases = emptyList()))
        assertThat(withoutMetastases.daysBetweenPrimaryAndMetastaticDiagnosis()).isEqualTo(0)
    }
}