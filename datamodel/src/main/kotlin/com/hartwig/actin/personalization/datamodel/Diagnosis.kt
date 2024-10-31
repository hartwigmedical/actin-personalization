package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class Diagnosis(
    override val consolidatedTumorType: TumorType,
    override val tumorLocations: Set<Location>,
    val sidedness: Sidedness? = determineSidedness(tumorLocations),
    override val hasHadTumorDirectedSystemicTherapy: Boolean,

    val ageAtDiagnosis: Int,
    val observedOsFromTumorIncidenceDays: Int,
    val hadSurvivalEvent: Boolean,
    val hasHadPriorTumor: Boolean,
    val priorTumors: List<PriorTumor>,

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
) : Tumor {

    companion object {

        fun determineSidedness(locations: Set<Location>): Sidedness? {
            val LOCATIONS_INDICATING_LEFT_SIDEDNESS =
                setOf(Location.FLEXURA_LIENALIS, Location.DESCENDING_COLON, Location.RECTOSIGMOID, Location.SIGMOID_COLON, Location.RECTUM)
            val LOCATIONS_INDICATING_RIGHT_SIDEDNESS =
                setOf(Location.APPENDIX, Location.COECUM, Location.ASCENDING_COLON, Location.FLEXURA_HEPATICA)

            val containsLeft = locations.any { it in LOCATIONS_INDICATING_LEFT_SIDEDNESS }
            val containsRight = locations.any { it in LOCATIONS_INDICATING_RIGHT_SIDEDNESS }

            return when {
                containsLeft && !containsRight -> Sidedness.LEFT
                containsRight && !containsLeft -> Sidedness.RIGHT
                else -> null
            }
        }
    }
}