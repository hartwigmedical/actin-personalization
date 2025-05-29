package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTnmMMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTnmNMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrTnmTMapper

object NcrTnmClassificationExtractor {

    fun extractClinical(record: NcrRecord): TnmClassification? {
        return extractTnmClassification(record.primaryDiagnosis.ct, record.primaryDiagnosis.cn, record.primaryDiagnosis.cm)
    }

    fun extractPathological(record: NcrRecord): TnmClassification? {
        return extractTnmClassification(record.primaryDiagnosis.pt, record.primaryDiagnosis.pn, record.primaryDiagnosis.pm)
    }

    private fun extractTnmClassification(tCode: String?, nCode: String?, mCode: String?): TnmClassification? {
        return if (tCode == null && nCode == null && mCode == null) {
            null
        } else {
            TnmClassification(
                tnmT = NcrTnmTMapper.resolveNullable(tCode),
                tnmN = NcrTnmNMapper.resolveNullable(nCode),
                tnmM = NcrTnmMMapper.resolveNullable(mCode)
            )
        }
    }
}