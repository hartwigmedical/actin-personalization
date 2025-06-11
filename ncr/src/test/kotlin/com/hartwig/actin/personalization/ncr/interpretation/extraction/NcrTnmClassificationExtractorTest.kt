package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmN
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmT
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrTnmClassificationExtractorTest {

    @Test
    fun `Should extract TNM from minimal records`() {
        val minimal = create(ctCode = null, cnCode = null, cmCode = null, ptCode = null, pnCode = null, pmCode = null)
        
        assertThat(NcrTnmClassificationExtractor.extractClinical(minimal)).isNull()
        assertThat(NcrTnmClassificationExtractor.extractPathological(minimal)).isNull()
    }

    @Test
    fun `Should extract TNM from proper records`() {
        val proper = create(ctCode = "1", cnCode = "2", cmCode = "0", ptCode = "2", pnCode = null, pmCode = "1")

        assertThat(NcrTnmClassificationExtractor.extractClinical(proper)).isEqualTo(TnmClassification(TnmT.T1, TnmN.N2, TnmM.M0))
        assertThat(NcrTnmClassificationExtractor.extractPathological(proper)).isEqualTo(TnmClassification(TnmT.T2, null, TnmM.M1))
    }

    private fun create(ctCode: String?, cnCode: String?, cmCode: String?, ptCode: String?, pnCode: String?, pmCode: String?): NcrRecord {
        val base = TestNcrRecordFactory.minimalDiagnosisRecord()
        return base.copy(
            primaryDiagnosis = base.primaryDiagnosis.copy(
                ct = ctCode,
                cn = cnCode,
                cm = cmCode,
                pt = ptCode,
                pn = pnCode,
                pm = pmCode
            )
        )
    }
}