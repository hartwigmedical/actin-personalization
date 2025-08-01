package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

class MolecularRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    internal fun hasNoMolecularDataForFollowUp(tumorRecords: List<NcrRecord>): Boolean {
        val (_, followUpDiagnosis) = extractPrimaryDiagnosis(tumorRecords)
        val hasNoMolecularDataForFollowUp = followUpDiagnosis.all { followUp ->
            with(followUp.molecularCharacteristics) { listOf(brafMut, rasMut, msiStat).all { it.ZeroOrNull() } }
        }
        if (!hasNoMolecularDataForFollowUp) {
            log("Follow-up diagnosis contains molecular data for tumor ID: ${tumorRecords.tumorId()}")
        }
        return hasNoMolecularDataForFollowUp
    }

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        return hasNoMolecularDataForFollowUp(tumorRecords)
    }
}