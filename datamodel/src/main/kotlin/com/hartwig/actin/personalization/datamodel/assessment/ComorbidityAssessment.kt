package com.hartwig.actin.personalization.datamodel.assessment

import kotlinx.serialization.Serializable

@Serializable
data class ComorbidityAssessment(
    // TODO (KD) Can be calculated from other fields.
    val charlsonComorbidityIndex: Int,

    val daysSinceDiagnosis: Int,

    val hasAids: Boolean,
    val hasCongestiveHeartFailure: Boolean,
    val hasCollagenosis: Boolean,
    val hasCopd: Boolean,
    val hasCerebrovascularDisease: Boolean,
    val hasDementia: Boolean,
    val hasDiabetesMellitus: Boolean,
    val hasDiabetesMellitusWithEndOrganDamage: Boolean,
    val hasOtherMalignancy: Boolean,
    val hasOtherMetastaticSolidTumor: Boolean,
    val hasMyocardialInfarct: Boolean,
    val hasMildLiverDisease: Boolean,
    val hasHemiplegiaOrParaplegia: Boolean,
    val hasPeripheralVascularDisease: Boolean,
    val hasRenalDisease: Boolean,
    val hasLiverDisease: Boolean,
    val hasUlcerDisease: Boolean
)