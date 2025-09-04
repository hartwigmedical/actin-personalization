package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class MolecularRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        return hasNoMolecularDataForFollowup(tumorRecords)
    }

    internal fun hasNoMolecularDataForFollowup(tumorRecords: List<NcrRecord>): Boolean {
        val (_, followupRecords) = splitDiagnosisAndFollowup(tumorRecords)
        val hasNoMolecularDataForFollowup = followupRecords.all { followup ->
            with(followup.molecularCharacteristics) { listOf(brafMut, rasMut, msiStat).all { it.zeroOrNull() } }
        }
        
        if (!hasNoMolecularDataForFollowup) {
            log("Followup diagnosis contains molecular data for tumor ID: ${tumorRecords.tumorId()}")
        }
        
        return hasNoMolecularDataForFollowup
    }
}