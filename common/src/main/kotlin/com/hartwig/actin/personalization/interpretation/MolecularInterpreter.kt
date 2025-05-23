package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.assessment.MolecularResult

class MolecularInterpreter(
    private val molecularResults: List<MolecularResult>
) {

    private fun resultPriorTo(maxDaysSinceDiagnosis: Int): MolecularResult? =
        molecularResults
            .filter  { it.daysSinceDiagnosis <= maxDaysSinceDiagnosis }
            .maxByOrNull { it.daysSinceDiagnosis }

    fun mostRecentHasMsiPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        resultPriorTo(maxDaysSinceDiagnosis)?.hasMsi

    fun mostRecentHasBrafMutationPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        resultPriorTo(maxDaysSinceDiagnosis)?.hasBrafMutation

    fun mostRecentHasBrafV600EMutationPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        resultPriorTo(maxDaysSinceDiagnosis)?.hasBrafV600EMutation

    fun mostRecentHasRasMutationPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        resultPriorTo(maxDaysSinceDiagnosis)?.hasRasMutation

    fun mostRecentHasKrasG12CMutationPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        resultPriorTo(maxDaysSinceDiagnosis)?.hasKrasG12CMutation
}
