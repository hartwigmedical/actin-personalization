package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.assessment.ComorbidityAssessment

class ComorbidityInterpreter(
    private val comorbidityAssessments: List<ComorbidityAssessment>
) {

    private fun assessmentPriorTo(maxDaysSinceDiagnosis: Int): ComorbidityAssessment? =
        comorbidityAssessments
            .filter  { it.daysSinceDiagnosis <= maxDaysSinceDiagnosis }
            .maxByOrNull { it.daysSinceDiagnosis }

    fun mostRecentCharlsonComorbidityIndexPriorTo(maxDaysSinceDiagnosis: Int): Int? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.charlsonComorbidityIndex

    fun mostRecentHasAidsPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasAids

    fun mostRecentHasCongestiveHeartFailurePriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasCongestiveHeartFailure

    fun mostRecentHasCollagenosisPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasCollagenosis

    fun mostRecentHasCopdPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasCopd

    fun mostRecentHasCerebrovascularDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasCerebrovascularDisease

    fun mostRecentHasDementiaPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasDementia

    fun mostRecentHasDiabetesMellitusPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasDiabetesMellitus

    fun mostRecentHasDiabetesMellitusWithEndOrganDamagePriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasDiabetesMellitusWithEndOrganDamage

    fun mostRecentHasOtherMalignancyPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasOtherMalignancy

    fun mostRecentHasOtherMetastaticSolidTumorPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasOtherMetastaticSolidTumor

    fun mostRecentHasMyocardialInfarctPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasMyocardialInfarct

    fun mostRecentHasMildLiverDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasMildLiverDisease

    fun mostRecentHasHemiplegiaOrParaplegiaPriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasHemiplegiaOrParaplegia

    fun mostRecentHasPeripheralVascularDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasPeripheralVascularDisease

    fun mostRecentHasRenalDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasRenalDisease

    fun mostRecentHasLiverDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasLiverDisease

    fun mostRecentHasUlcerDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? =
        assessmentPriorTo(maxDaysSinceDiagnosis)?.hasUlcerDisease
}
