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
        return hasValidTreatmentData(tumorRecordsPerId)
    }

    private fun hasValidTreatmentData(tumorRecordsPerId: Map.Entry<Int, List<NcrRecord>>): Boolean {
        val allTreatments = tumorRecordsPerId.value.map { it.treatment }

        val hasAtLeastOneInvalidTreatment = allTreatments.any { treatment ->
            treatment.tumgerichtTher == 1 &&
                    (treatment.primarySurgery.chir == null || treatment.primarySurgery.chir == 0) &&
                    treatment.primaryRadiotherapy.rt == 0 &&
                    (treatment.primaryRadiotherapy.chemort == null || treatment.primaryRadiotherapy.chemort == 0) &&
                    (treatment.gastroenterologyResection.mdlRes == null || treatment.gastroenterologyResection.mdlRes == 0) &&
                    (treatment.hipec.hipec == null || treatment.hipec.hipec == 0) &&
                    treatment.systemicTreatment.chemo == 0 &&
                    treatment.systemicTreatment.target == 0 &&
                    treatment.metastaticSurgery.metaChirInt1 == null &&
                    treatment.metastaticRadiotherapy.metaRtCode1 == null
        }

        if (hasAtLeastOneInvalidTreatment) {
            log("Invalid treatment data found for set of NCR tumor records with ID: ${tumorRecordsPerId.key}")
        }

        return !hasAtLeastOneInvalidTreatment
    }

    private fun log(message: String) {
        if (logFilteredRecords) {
            logger.warn { " $message" }
        }
    }
}