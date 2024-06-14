package com.hartwig.actin.personalization.datamodel

data class Diagnosis(
    override val consolidatedTumorType: TumorType,
    override val tumorLocations: Set<Location>,
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val ageAtDiagnosis: Int,
    val hasHadPriorTumor: Boolean,
    val priorTumors: List<PriorTumor>,
    
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

    val presentedWithIleus: Boolean?,
    val presentedWithPerforation: Boolean?,
    val anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory?,

    val hasMsi: Boolean?,
    val hasBrafMutation: Boolean?,
    val hasBrafV600EMutation: Boolean?,
    val hasRasMutation: Boolean?,
    val hasKrasG12CMutation: Boolean?,
) : Tumor