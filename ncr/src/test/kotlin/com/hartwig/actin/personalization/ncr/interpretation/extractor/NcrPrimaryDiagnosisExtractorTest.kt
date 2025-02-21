package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.diagnosis.BasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.DifferentiationGrade
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmN
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmT
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrPrimaryDiagnosisExtractorTest {

    @Test
    fun `Should extract primary diagnosis for minimal NCR record`() {
        val primaryDiagnosis = NcrPrimaryDiagnosisExtractor.extract(listOf(TestNcrRecordFactory.minimalDiagnosisRecord()))

        assertThat(primaryDiagnosis).isNotNull()
    }

    @Test
    fun `Should extract primary diagnosis for proper set of NCR tumor records`() {
        val primaryDiagnosis = NcrPrimaryDiagnosisExtractor.extract(TestNcrRecordFactory.properTumorRecords())

        assertThat(primaryDiagnosis.basisOfDiagnosis).isEqualTo(BasisOfDiagnosis.CLINICAL_ONLY_INVESTIGATION)
        assertThat(primaryDiagnosis.hasDoublePrimaryTumor).isFalse()
        assertThat(primaryDiagnosis.primaryTumorType).isEqualTo(TumorType.CRC_ADENOCARCINOMA)
        assertThat(primaryDiagnosis.primaryTumorLocation).isEqualTo(TumorLocation.ASCENDING_COLON)
        assertThat(primaryDiagnosis.differentiationGrade).isEqualTo(DifferentiationGrade.GRADE_2_OR_MODERATELY_DIFFERENTIATED)
        assertThat(primaryDiagnosis.clinicalTnmClassification).isEqualTo(
            TnmClassification(
                tumor = TnmT.T2,
                lymphNodes = TnmN.N1,
                metastasis = TnmM.M0
            )
        )
        assertThat(primaryDiagnosis.pathologicalTnmClassification).isEqualTo(
            TnmClassification(
                tumor = TnmT.T3,
                lymphNodes = TnmN.N2,
                metastasis = null
            )
        )
    }
}