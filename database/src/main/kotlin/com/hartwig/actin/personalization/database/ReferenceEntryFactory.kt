package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.database.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Tumor

object ReferenceEntryFactory {
    
    fun create(patient: ReferencePatient, tumor: Tumor): ReferenceEntry {
        return ReferenceEntry()
    }
}