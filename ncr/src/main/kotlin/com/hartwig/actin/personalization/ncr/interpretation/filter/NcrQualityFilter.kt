package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import io.github.oshai.kotlinlogging.KotlinLogging

class NcrQualityFilter(private val logFilteredRecords: Boolean) {

    private val logger = KotlinLogging.logger {}

    fun run(records: List<NcrRecord>): List<NcrRecord> {
        val cleaned = records.groupBy { it.identification.keyNkr }.values.map { patientRecords ->
            patientRecords.groupBy { it.identification.keyZid }.values.filter { isReliableTumorRecordSet(it) }.flatten()
        }.flatten()
        
        val filteredRecords = records - cleaned.toSet()
        val filteredTumors = filteredRecords.map { it.identification.keyZid }.toSet()
        
        logger.info { " ${filteredRecords.size} records belonging to ${filteredTumors.size} tumors removed after filtering " }
        
        return cleaned
    }

    private fun isReliableTumorRecordSet(tumorRecords: List<NcrRecord>): Boolean {
        val filters = listOf(
            PatientRecordFilter(logFilteredRecords),
            PriorTumorRecordFilter(logFilteredRecords),
            PrimaryTumorRecordFilter(logFilteredRecords),
            MetastaticDiagnosisRecordFilter(logFilteredRecords),
            ComorbidityRecordFilter(logFilteredRecords),
            MolecularRecordFilter(logFilteredRecords),
            TreatmentRecordFilter(logFilteredRecords),
        )
        return filters.all { it.tumorRecords(tumorRecords) }
    }
}