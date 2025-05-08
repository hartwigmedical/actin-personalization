package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.database.datamodel.ReferenceRecord
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Tumor

object ReferenceRecordFactory {

    fun create(referencePatients: List<ReferencePatient>, indexedTumors: List<Pair<Int, Tumor>>): List<ReferenceRecord> {
        return referencePatients.flatMap { extractReferenceRecords(it, indexedTumors) }
    }

    private fun extractReferenceRecords(referencePatient: ReferencePatient, indexedTumors: List<Pair<Int, Tumor>>): List<ReferenceRecord> {
        return referencePatient.tumors.map { 
            ReferenceRecord(source = "")
        }
    }
}