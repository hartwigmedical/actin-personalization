package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import io.github.oshai.kotlinlogging.KotlinLogging

class NcrQualityFilter(private val logFilteredRecords: Boolean) {

    private val logger = KotlinLogging.logger {}

    fun run(records: List<NcrRecord>): List<NcrRecord> {
        val cleaned = records.groupBy { it.identification.keyNkr }.values.map { patientRecords ->
            patientRecords.groupBy { it.identification.keyZid }.entries.filter { isReliableTumorRecordSet(it) }.map { it.value }.flatten()
        }.flatten()
        
        val filteredRecords = records - cleaned.toSet()
        val filteredTumors = filteredRecords.map { it.identification.keyZid }.toSet()
        
        logger.info { " ${filteredRecords.size} records belonging to ${filteredTumors.size} tumors removed after filtering " }
        
        return cleaned
    }

    private fun isReliableTumorRecordSet(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val patientRecordFilter = PatientRecordFilter { ::log }
        val priorTumorRecordFilter = PriorTumorRecordFilter { ::log }
        val filters = listOf(
            patientRecordFilter::hasValidTreatmentData,
            patientRecordFilter::hasIdentialSex,
            patientRecordFilter::hasExactlyOneDiagnosis,
            patientRecordFilter::hasVitalStatusForDIARecords,
            patientRecordFilter::hasEmptyVitalStatusForVerbRecords,
            patientRecordFilter::hasIdenticalYearOfIncidence,
            priorTumorRecordFilter::hasEmptyPriorTumorInVerbEpisode,
            priorTumorRecordFilter::hasNoPositiveValueInMalInt,
            priorTumorRecordFilter::hasCompletePriorTumorData,
        )
        return filters.all { it(tumorRecordsPerId) }
    }
    

    internal fun log(message: String) {
        if (logFilteredRecords) {
            logger.warn { " $message" }
        }
    }
}