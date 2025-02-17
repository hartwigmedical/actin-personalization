package com.hartwig.actin.personalization.datamodel.diagnosis

import kotlinx.serialization.Serializable

@Serializable
data class PrimaryDiagnosis(
    val basisOfDiagnosis: TumorBasisOfDiagnosis,
    val hasDoublePrimaryTumor: Boolean? = null,
    val primaryTumorType: TumorType,
    val primaryTumorLocation: TumorLocation,
    val differentiationGrade: TumorDifferentiationGrade? = null,

    val clinicalTnmClassification: TnmClassification? = null,
    val pathologicalTnmClassification: TnmClassification? = null,
    val clinicalTumorStage: TumorStage? = null,
    val pathologicalTumorStage: TumorStage? = null,
    val investigatedLymphNodesCount: Int? = null,
    val positiveLymphNodesCount: Int? = null,

    val venousInvasionDescription: VenousInvasionDescription? = null,
    val lymphaticInvasionCategory: LymphaticInvasionCategory? = null,
    val extraMuralInvasionCategory: ExtraMuralInvasionCategory? = null,
    val tumorRegression: TumorRegression? = null,

    // KD: Specific for CRC, could be hidden behind interface eventually
    val sidedness: Sidedness? = determineSidedness(primaryTumorLocation),
    val presentedWithIleus: Boolean? = null,
    val presentedWithPerforation: Boolean? = null,

    // KD: Only present in case location == RECTUM, could be hidden behind interface eventually.
    val anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory? = null,

    // KD: Only present in case location in (RECTUM, RECTOSIGMOID), could be hidden behind interface eventually
    val mesorectalFasciaIsClear: Boolean? = null,
    val distanceToMesorectalFasciaMm: Int? = null,
)

val LOCATIONS_INDICATING_LEFT_SIDEDNESS =
    setOf(TumorLocation.FLEXURA_LIENALIS, TumorLocation.DESCENDING_COLON, TumorLocation.RECTOSIGMOID, TumorLocation.SIGMOID_COLON, TumorLocation.RECTUM)
val LOCATIONS_INDICATING_RIGHT_SIDEDNESS = setOf(TumorLocation.APPENDIX, TumorLocation.COECUM, TumorLocation.ASCENDING_COLON, TumorLocation.FLEXURA_HEPATICA)

private fun determineSidedness(location: TumorLocation): Sidedness? {
    val containsLeft = location in LOCATIONS_INDICATING_LEFT_SIDEDNESS
    val containsRight = location in LOCATIONS_INDICATING_RIGHT_SIDEDNESS

    return when {
        containsLeft && !containsRight -> Sidedness.LEFT
        containsRight && !containsLeft -> Sidedness.RIGHT
        else -> null
    }
}
