package com.hartwig.actin.personalization.ncr.datamodel

data class DiagnosisEpisode  (
    override val id: Int,
    override val order: Int,
    override val distantMetastasesStatus: DistantMetastasesStatus,
    override val whoStatusPreTreatmentStart: Int?,
    override val asaClassificationPreSurgeryOrEndoscopy: AsaClassificationPreSurgeryOrEndoscopy?,

    val cci: Int?,
    val cciNumberOfCategories: CciNumberOfCategories?,
    val cciHasAids: Boolean?,
    val cciHasCongestiveHeartFailure: Boolean?,
    val cciHasCollagenosis: Boolean?,
    val cciHasCopd: Boolean?,
    val cciHasCerebrovascularDisease: Boolean?,
    val cciHasDementia: Boolean?,
    val cciHasDiabetesMellitus: Boolean?,
    val cciHasDiabetesMellitusWithEndOrganDamage: Boolean?,
    val cciHasOtherMalignancy: Boolean?,
    val cciHasOtherMetastaticSolidTumor: Boolean?,
    val cciHasMyocardialInfarct: Boolean?,
    val cciHasMildLiverDisease: Boolean?,
    val cciHasHemiplegiaOrParaplegia: Boolean?,
    val cciHasPeripheralVascularDisease: Boolean?,
    val cciHasRenalDisease: Boolean?,
    val cciHasLiverDisease: Boolean?,
    val cciHasUlcerDisease: Boolean?,



    ): Episode
