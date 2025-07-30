package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class MolecularRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    internal fun hasCompleteMolecularData(tumorRecords: List<NcrRecord>): Boolean {
        val (_, followupDiagnosis) = extractPrimaryDiagnosis(tumorRecords)
        val allMolecularCharacteristics = followupDiagnosis.map { it.molecularCharacteristics }
        val hasCompleteMolecularData = allMolecularCharacteristics.all { it.brafMut != null && it.rasMut != null && it.msiStat != null }
        if (!hasCompleteMolecularData) {
            log("Incomplete molecular data for tumor ID: ${tumorRecords.tumorId()}")
        }
        return hasCompleteMolecularData
    }

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        return hasCompleteMolecularData(tumorRecords)
    }
}