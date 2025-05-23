package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.assessment.MolecularResult

class MolecularInterpreter(
    private val molecularResults: List<MolecularResult>
) {
    fun mostRecentHasMsiPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return resultPriorTo(maxDaysSinceDiagnosis)?.hasMsi
    }

    fun mostRecentHasBrafMutationPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return resultPriorTo(maxDaysSinceDiagnosis)?.hasBrafMutation
    }

    fun mostRecentHasBrafV600EMutationPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return resultPriorTo(maxDaysSinceDiagnosis)?.hasBrafV600EMutation
    }

    fun mostRecentHasRasMutationPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return resultPriorTo(maxDaysSinceDiagnosis)?.hasRasMutation
    }

    fun mostRecentHasKrasG12CMutationPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return resultPriorTo(maxDaysSinceDiagnosis)?.hasKrasG12CMutation
    }

    private fun resultPriorTo(maxDaysSinceDiagnosis: Int): MolecularResult? {
        return molecularResults
            .filter { it.daysSinceDiagnosis <= maxDaysSinceDiagnosis }
            .maxByOrNull { it.daysSinceDiagnosis }
    }

}
