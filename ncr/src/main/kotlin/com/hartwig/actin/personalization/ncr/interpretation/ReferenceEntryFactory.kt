package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.extraction.NcrReferenceEntryExtractor
import com.hartwig.actin.personalization.ncr.interpretation.filter.NcrQualityFilter

import java.util.stream.Collectors

class ReferenceEntryFactory(private val filter: NcrQualityFilter) {
    
    fun create(records: List<NcrRecord>): List<ReferenceEntry> {
        val filtered = filter.run(records)
        
        return filtered.groupBy { it.identification.keyNkr }.values.parallelStream()
            .map { patientRecords -> createReferenceEntriesForPatient(patientRecords) }
            .collect(Collectors.toList())
            .flatten()
    }

    private fun createReferenceEntriesForPatient(patientRecords: List<NcrRecord>): List<ReferenceEntry> {
        return patientRecords.groupBy { it.identification.keyZid }.values.parallelStream()
            .map { tumorRecords -> NcrReferenceEntryExtractor.extract(tumorRecords) }
            .collect(Collectors.toList())
    }
}
