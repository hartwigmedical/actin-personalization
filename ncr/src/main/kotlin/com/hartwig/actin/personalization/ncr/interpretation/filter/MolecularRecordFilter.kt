package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class MolecularRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    internal fun hasCompleteMolecularData(tumorRecords: List<NcrRecord>): Boolean {
        val (_, followupDiagnosis) = extractPrimaryDiagnosis(tumorRecords)
        val allMolecularCharacteristics = followupDiagnosis.map { it.molecularCharacteristics }
        return allMolecularCharacteristics.all { it.brafMut != null && it.rasMut != null && it.msiStat != null }
    }

    override fun tumorRecords(record: List<NcrRecord>): Boolean {
        return hasCompleteMolecularData(record)
    }
}