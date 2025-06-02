package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.extraction.NcrReferenceEntryExtractor

import java.util.stream.Collectors

object ReferenceEntryFactory {
    fun create(records: List<NcrRecord>): List<ReferenceEntry> {
        return records.groupBy { it.identification.keyNkr }.values.parallelStream()
            .map { patientRecords -> createReferenceEntriesForPatient(patientRecords) }
            .collect(Collectors.toList())
            .flatten()
    }

    private fun createReferenceEntriesForPatient(patientRecords: List<NcrRecord>): List<ReferenceEntry> {
        return patientRecords.groupBy { it.identification.keyZid }.values.parallelStream()
            .filter { tumorRecords -> NcrRecordReliabilityFilter.isReliableTumorRecord(tumorRecords) }
            .map { tumorRecords -> NcrReferenceEntryExtractor.extract(tumorRecords) }
            .collect(Collectors.toList())
    }
}
