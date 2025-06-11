package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmN
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TnmInterpreterTest {

    private val primaryClinicalTnm = TnmClassification(TnmT.T2, TnmN.N1, TnmM.M0)
    private val primaryPathologicalTnm = TnmClassification(TnmT.T3, TnmN.N1, null)

    private val primaryDiagnosis = TestDatamodelFactory.primaryDiagnosis(
        clinicalTnmClassification = primaryClinicalTnm,
        pathologicalTnmClassification = primaryPathologicalTnm
    )

    private val nonMetastaticTnm = TnmClassification(TnmT.T4A, TnmN.N2, TnmM.M0)
    private val metastaticTnm = TnmClassification(TnmT.T4A, TnmN.N2, TnmM.M1B)

    @Test
    fun `Should pick metastatic diagnosis when metastatic TNM is present and metastatic`() {
        val metastaticDiagnosis = TestDatamodelFactory.metastaticDiagnosis(
            clinicalTnmClassification = metastaticTnm,
            pathologicalTnmClassification = metastaticTnm
        )
        
        val interpreter = TnmInterpreter(primaryDiagnosis, metastaticDiagnosis)
        
        assertThat(interpreter.clinicalTnm()).isEqualTo(metastaticTnm)
        assertThat(interpreter.pathologicalTnm()).isEqualTo(metastaticTnm)
    }

    @Test
    fun `Should fall back to primary diagnosis when metastatic TNM is missing or non-metastatic`() {
        val metastaticDiagnosis = TestDatamodelFactory.metastaticDiagnosis(
            clinicalTnmClassification = nonMetastaticTnm,
            pathologicalTnmClassification = nonMetastaticTnm
        )

        val interpreter = TnmInterpreter(primaryDiagnosis, metastaticDiagnosis)

        assertThat(interpreter.clinicalTnm()).isEqualTo(primaryClinicalTnm)
        assertThat(interpreter.pathologicalTnm()).isEqualTo(primaryPathologicalTnm)
    }
}