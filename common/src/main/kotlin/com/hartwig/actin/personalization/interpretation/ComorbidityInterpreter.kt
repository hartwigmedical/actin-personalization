package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.assessment.ComorbidityAssessment

class ComorbidityInterpreter(
    private val comorbidityAssessments: List<ComorbidityAssessment>
) {
    fun mostRecentCharlsonComorbidityIndexPriorTo(maxDaysSinceDiagnosis: Int): Int? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.charlsonComorbidityIndex
    }

    fun mostRecentHasAidsPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasAids
    }

    fun mostRecentHasCongestiveHeartFailurePriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasCongestiveHeartFailure
    }

    fun mostRecentHasCollagenosisPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasCollagenosis
    }

    fun mostRecentHasCopdPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasCopd
    }

    fun mostRecentHasCerebrovascularDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasCerebrovascularDisease
    }

    fun mostRecentHasDementiaPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasDementia
    }

    fun mostRecentHasDiabetesMellitusPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasDiabetesMellitus
    }

    fun mostRecentHasDiabetesMellitusWithEndOrganDamagePriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasDiabetesMellitusWithEndOrganDamage
    }

    fun mostRecentHasOtherMalignancyPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasOtherMalignancy
    }

    fun mostRecentHasOtherMetastaticSolidTumorPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasOtherMetastaticSolidTumor
    }

    fun mostRecentHasMyocardialInfarctPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasMyocardialInfarct
    }

    fun mostRecentHasMildLiverDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasMildLiverDisease
    }

    fun mostRecentHasHemiplegiaOrParaplegiaPriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasHemiplegiaOrParaplegia
    }

    fun mostRecentHasPeripheralVascularDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasPeripheralVascularDisease
    }

    fun mostRecentHasRenalDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasRenalDisease
    }

    fun mostRecentHasLiverDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean?{
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasLiverDisease
    }

    fun mostRecentHasUlcerDiseasePriorTo(maxDaysSinceDiagnosis: Int): Boolean? {
        return assessmentPriorTo(maxDaysSinceDiagnosis)?.hasUlcerDisease
    }

    private fun assessmentPriorTo(maxDaysSinceDiagnosis: Int): ComorbidityAssessment? {
        return comorbidityAssessments
            .filter { it.daysSinceDiagnosis <= maxDaysSinceDiagnosis }
            .maxByOrNull { it.daysSinceDiagnosis }
    }
}
