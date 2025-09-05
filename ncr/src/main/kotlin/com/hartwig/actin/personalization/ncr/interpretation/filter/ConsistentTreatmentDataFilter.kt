package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class ConsistentTreatmentDataFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        val allTreatments = tumorRecords.map { it.treatment }
        val indicatesTreatmentButNoTreatmentDefined = allTreatments.any { treatment ->
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

        if (indicatesTreatmentButNoTreatmentDefined) {
            log("Inconsistent treatment data found for set of NCR tumor records with ID: ${tumorRecords.tumorId()}")
        }

        return !indicatesTreatmentButNoTreatmentDefined
    }
}