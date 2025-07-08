package com.hartwig.actin.personalization.cairo.interpretation.extraction

import com.hartwig.actin.personalization.cairo.datamodel.CairoRecord
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis

object CairoMetastaticDiagnosisExtractor {

    fun extract(records: List<CairoRecord>): MetastaticDiagnosis {
        val record = records.first()
        return MetastaticDiagnosis(
            distantMetastasesDetectionStatus = ,//TODO
            metastases = ,//TODO
            numberOfLiverMetastases = ,//TODO
            maximumSizeOfLiverMetastasisMm = ,//TODO
            investigatedLymphNodesCount =,//TODO
            positiveLymphNodesCount = //TODO
        )
    }
}