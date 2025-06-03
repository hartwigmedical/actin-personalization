package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import io.github.oshai.kotlinlogging.KotlinLogging

object NcrQualityFilter {

    private val LOGGER = KotlinLogging.logger {}
    
    fun isReliableTumorRecordSet(tumorRecords: List<NcrRecord>): Boolean {
        val hasSingleUniqueTumorId = hasSingleUniqueTumorId(tumorRecords)
        val hasValidTreatmentData = hasValidTreatmentData(tumorRecords)
        
        return hasSingleUniqueTumorId && hasValidTreatmentData
    }

    private fun hasSingleUniqueTumorId(tumorRecords: List<NcrRecord>): Boolean {
        val tumorIds = tumorRecords.map { it.identification.keyZid }.toSet()
        val hasUniqueTumorId = tumorIds.size == 1
        if (!hasUniqueTumorId) {
            LOGGER.warn { " Multiple tumor IDs found for single set of tumor records: $tumorIds" }
        }
        return hasUniqueTumorId
    }

    private fun hasValidTreatmentData(tumorRecords: List<NcrRecord>): Boolean {
        val allTreatments = tumorRecords.map { it.treatment }

        val hasRealTreatment = allTreatments.any { treatment ->
            treatment.systemicTreatment.chemo == 1 ||
                    treatment.systemicTreatment.target == 1 ||
                    treatment.primarySurgery.chir == 1 ||
                    treatment.metastaticSurgery.metaChirInt1 != null ||
                    treatment.primaryRadiotherapy.rt == 1 ||
                    treatment.primaryRadiotherapy.chemort == 1 ||
                    treatment.metastaticRadiotherapy.metaRtCode1 != null ||
                    treatment.gastroenterologyResection.mdlRes == 1 ||
                    treatment.hipec.hipec == 1
        }

        val hasValidTreatmentData = !allTreatments.any { it.tumgerichtTher == 1 } || hasRealTreatment
        if (!hasValidTreatmentData) {
            LOGGER.warn { " Invalid treatment data found for set of NCR tumor records with ID: ${tumorId(tumorRecords)}" }
        }
        
        return hasValidTreatmentData
    }

    private fun tumorId(tumorRecords: List<NcrRecord>) : Int {
        return tumorRecords.map { it.identification.keyZid }.first()
    }
}