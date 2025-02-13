package com.hartwig.actin.personalization.datamodel.old

import com.hartwig.actin.personalization.datamodel.diagnosis.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.Sidedness
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import kotlinx.serialization.Serializable

@Serializable
data class Diagnosis(
    override val consolidatedTumorType: TumorType,
    override val tumorLocations: Set<TumorLocation>,
    val sidedness: Sidedness? = determineSidedness(tumorLocations),
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val ageAtDiagnosis: Int,
    val observedOsFromTumorIncidenceDays: Int,
    val hadSurvivalEvent: Boolean,
    val hasHadPriorTumor: Boolean,
    val priorTumors: List<PriorTumor>,
    val orderOfFirstDistantMetastasesEpisode: Int,
    val isMetachronous: Boolean,

    val cci: Int? = null,
    val cciNumberOfCategories: NumberOfCciCategories? = null,
    val cciHasAids: Boolean? = null,
    val cciHasCongestiveHeartFailure: Boolean? = null,
    val cciHasCollagenosis: Boolean? = null,
    val cciHasCopd: Boolean? = null,
    val cciHasCerebrovascularDisease: Boolean? = null,
    val cciHasDementia: Boolean? = null,
    val cciHasDiabetesMellitus: Boolean? = null,
    val cciHasDiabetesMellitusWithEndOrganDamage: Boolean? = null,
    val cciHasOtherMalignancy: Boolean? = null,
    val cciHasOtherMetastaticSolidTumor: Boolean? = null,
    val cciHasMyocardialInfarct: Boolean? = null,
    val cciHasMildLiverDisease: Boolean? = null,
    val cciHasHemiplegiaOrParaplegia: Boolean? = null,
    val cciHasPeripheralVascularDisease: Boolean? = null,
    val cciHasRenalDisease: Boolean? = null,
    val cciHasLiverDisease: Boolean? = null,
    val cciHasUlcerDisease: Boolean? = null,

    val presentedWithIleus: Boolean? = null,
    val presentedWithPerforation: Boolean? = null,
    val anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory? = null,

    val hasMsi: Boolean? = null,
    val hasBrafMutation: Boolean? = null,
    val hasBrafV600EMutation: Boolean? = null,
    val hasRasMutation: Boolean? = null,
    val hasKrasG12CMutation: Boolean? = null,
) : Tumor

private fun determineSidedness(locations: Set<TumorLocation>): Sidedness? {
    val LOCATIONS_INDICATING_LEFT_SIDEDNESS =
        setOf(TumorLocation.FLEXURA_LIENALIS, TumorLocation.DESCENDING_COLON, TumorLocation.RECTOSIGMOID, TumorLocation.SIGMOID_COLON, TumorLocation.RECTUM)
    val LOCATIONS_INDICATING_RIGHT_SIDEDNESS =
        setOf(TumorLocation.APPENDIX, TumorLocation.COECUM, TumorLocation.ASCENDING_COLON, TumorLocation.FLEXURA_HEPATICA)

    val containsLeft = locations.any { it in LOCATIONS_INDICATING_LEFT_SIDEDNESS }
    val containsRight = locations.any { it in LOCATIONS_INDICATING_RIGHT_SIDEDNESS }

    return when {
        containsLeft && !containsRight -> Sidedness.LEFT
        containsRight && !containsLeft -> Sidedness.RIGHT
        else -> null
    }
}