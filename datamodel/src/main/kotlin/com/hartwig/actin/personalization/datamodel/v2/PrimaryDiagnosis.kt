package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.datamodel.ExtraMuralInvasionCategory
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.LymphaticInvasionCategory
import com.hartwig.actin.personalization.datamodel.Sidedness
import com.hartwig.actin.personalization.datamodel.TumorType
import com.hartwig.actin.personalization.datamodel.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.TumorDifferentiationGrade
import com.hartwig.actin.personalization.datamodel.TumorRegression
import com.hartwig.actin.personalization.datamodel.VenousInvasionDescription
import kotlinx.serialization.Serializable

@Serializable
data class PrimaryDiagnosis(
    val basisOfDiagnosis: TumorBasisOfDiagnosis,
    val hasDoublePrimaryTumor: Boolean? = null,
    val primaryTumorType: TumorType,
    val primaryTumorLocation: Location,
    val differentiationGrade: TumorDifferentiationGrade? = null,

    // TODO (KD): What do these values mean in the context of a metastasis diagnosis ("VERB")?
    val clinicalTnmClassification: TnmClassification? = null,
    val pathologicalTnmClassification: TnmClassification? = null,
    val clinicalTumorStage: TumorStage? = null,
    val pathologicalTumorStage: TumorStage? = null,
    val investigatedLymphNodesCount: Int? = null,
    val positiveLymphNodesCount: Int? = null,

    // KD: Specific for CRC, could be hidden behind interface eventually
    val sidedness: Sidedness? = determineSidedness(primaryTumorLocation),
    val presentedWithIleus: Boolean? = null,
    val presentedWithPerforation: Boolean? = null,

    // KD: Only present in case location == RECTUM, could be hidden behind interface eventually.
    val anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory? = null,

    // KD: Only present in case location in (RECTUM, RECTOSIGMOID), could be hidden behind interface eventually
    val mesorectalFasciaIsClear: Boolean? = null,
    val distanceToMesorectalFasciaMm: Int? = null,

    // TODO (KD): What do these values mean in the context of a metastasis diagnosis ("VERB")?
    val venousInvasionDescription: VenousInvasionDescription? = null,
    val lymphaticInvasionCategory: LymphaticInvasionCategory? = null,
    val extraMuralInvasionCategory: ExtraMuralInvasionCategory? = null,
    val tumorRegression: TumorRegression? = null,
)

val LOCATIONS_INDICATING_LEFT_SIDEDNESS =
    setOf(Location.FLEXURA_LIENALIS, Location.DESCENDING_COLON, Location.RECTOSIGMOID, Location.SIGMOID_COLON, Location.RECTUM)
val LOCATIONS_INDICATING_RIGHT_SIDEDNESS = setOf(Location.APPENDIX, Location.COECUM, Location.ASCENDING_COLON, Location.FLEXURA_HEPATICA)

private fun determineSidedness(location: Location): Sidedness? {
    val containsLeft = location in LOCATIONS_INDICATING_LEFT_SIDEDNESS
    val containsRight = location in LOCATIONS_INDICATING_RIGHT_SIDEDNESS

    return when {
        containsLeft && !containsRight -> Sidedness.LEFT
        containsRight && !containsLeft -> Sidedness.RIGHT
        else -> null
    }
}
