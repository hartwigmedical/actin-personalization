package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

object NcrRecordReliabilityFilter {
    fun isReliableTumorRecord(tumorRecords: List<NcrRecord>): Boolean {
        return hasValidTumgerichtTreatmentData(tumorRecords)
    }

    private fun hasValidTumgerichtTreatmentData(tumorRecords: List<NcrRecord>): Boolean {
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

        return !allTreatments.any { it.tumgerichtTher == 1 } || hasRealTreatment
    }

}