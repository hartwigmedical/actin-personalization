package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.extraction.NcrTumorExtractor
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrSexMapper
import java.util.stream.Collectors

object ReferencePatientFactory {

    fun create(ncrRecords: List<NcrRecord>): List<ReferencePatient> {
        return ncrRecords.groupBy { it.identification.keyNkr }.values.parallelStream()
            .map(::createReferencePatient)
            .collect(Collectors.toList())
    }

    private fun createReferencePatient(ncrRecords: List<NcrRecord>): ReferencePatient {
        return ReferencePatient(
            sex = extractSex(ncrRecords),
            tumors = extractTumors(ncrRecords)
        )
    }

    private fun extractSex(ncrRecords: List<NcrRecord>): Sex {
        return NcrSexMapper.resolve(ncrRecords.map { it.patientCharacteristics.gesl }.distinct().single())
    }

    private fun extractTumors(ncrRecords: List<NcrRecord>): List<Tumor> {
        return ncrRecords.groupBy { it.identification.keyZid }.entries
            .map { (_, records) -> NcrTumorExtractor.extractTumor(records) }
    }
}
