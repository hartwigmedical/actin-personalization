package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.database.datamodel.ReferenceRecord
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Tumor

object ReferenceRecordFactory {
    
    fun create(patient: ReferencePatient, tumor: Tumor): ReferenceRecord {
        return ReferenceRecord()
    }
}