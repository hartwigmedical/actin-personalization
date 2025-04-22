package com.hartwig.actin.personalization.cairo.interpretation


import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.Tumor

import com.hartwig.actin.personalization.cairo.datamodel.CairoRecord
import com.hartwig.actin.personalization.cairo.interpretation.extraction.CairoTumorExtractor
import com.hartwig.actin.personalization.cairo.interpretation.mapping.CairoSexMapper



import java.util.stream.Collectors


object CairoReferencePatientFactory {
    fun create(cairoRecords: List<CairoRecord>): List<ReferencePatient> {
        return cairoRecords.groupBy { it.identification.patnr }.values.parallelStream()
            .map(::createReferencePatient)
            .collect(Collectors.toList())
    }

    private fun createReferencePatient(cairoRecords: List<CairoRecord>): ReferencePatient{
        return ReferencePatient(
            sex = extractSex(cairoRecords),
            tumors = extractTumors(cairoRecords)
        )
    }

    private fun extractSex(cairoRecords: List<CairoRecord>): Sex {
        return CairoSexMapper.resolve(cairoRecords.map { it.patientCharacteristics.sex }.distinct().single())
    } // in case of C2 sex is called Gender

    private fun extractTumors(cairoRecords: List<CairoRecord>): List<Tumor> {
        return cairoRecords.groupBy { it.identification.patnr }.entries
            .map { (_, records) -> CairoTumorExtractor.extractTumor(records) }
    }
}