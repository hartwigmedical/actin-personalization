package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.database.datamodel.ReferenceRecord
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Tumor

object ReferenceRecordFactory {

    fun create(referencePatients: List<ReferencePatient>, indexedTumors: List<Pair<Int, Tumor>>): List<ReferenceRecord> {
        return emptyList()
    }
}