package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.datamodel.AsaClassification
import com.hartwig.actin.personalization.datamodel.ExtraMuralInvasionCategory
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.LymphaticInvasionCategory
import com.hartwig.actin.personalization.datamodel.Sidedness
import com.hartwig.actin.personalization.datamodel.StageTnm
import com.hartwig.actin.personalization.datamodel.TnmM
import com.hartwig.actin.personalization.datamodel.TnmN
import com.hartwig.actin.personalization.datamodel.TnmT
import com.hartwig.actin.personalization.datamodel.TumorType
import com.hartwig.actin.personalization.datamodel.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.TumorDifferentiationGrade
import com.hartwig.actin.personalization.datamodel.TumorRegression
import com.hartwig.actin.personalization.datamodel.VenousInvasionDescription

data class PrimaryDiagnosis(
    val basisOfDiagnosis: TumorBasisOfDiagnosis,
    val hasDoublePrimaryTumor: Boolean? = null,
    val primaryTumorType: TumorType,
    val primaryTumorLocation: Location,
    val differentiationGrade: TumorDifferentiationGrade? = null,

    // TODO (KD): Specific for CRC, should use interface
    val sidedness: Sidedness? = determineSidedness(primaryTumorLocation),
    val presentedWithIleus: Boolean? = null,
    val presentedWithPerforation: Boolean? = null,

    // TODO (KD): Only present in case location == RECTUM, should use interface
    val anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory? = null,

    // TODO (KD): Only present in case location in (RECTUM, RECTOSIGMOID), should use interface
    val mesorectalFasciaIsClear: Boolean? = null,
    val distanceToMesorectalFasciaMm: Int? = null,

    // TODO (KD): What do these values mean in the context of a metastasis diagnosis ("VERB")?
    val venousInvasionDescription: VenousInvasionDescription? = null,
    val lymphaticInvasionCategory: LymphaticInvasionCategory? = null,
    val extraMuralInvasionCategory: ExtraMuralInvasionCategory? = null,
    val tumorRegression: TumorRegression? = null,

    // TODO (KD): What do these values mean in the context of a metastasis diagnosis ("VERB")?
    val tnmCT: TnmT? = null,
    val tnmCN: TnmN? = null,
    val tnmCM: TnmM? = null,
    val tnmPT: TnmT? = null,
    val tnmPN: TnmN? = null,
    val tnmPM: TnmM? = null,
    val stageCTNM: StageTnm? = null,
    val stagePTNM: StageTnm? = null,
    val stageTNM: StageTnm? = null,
    val investigatedLymphNodesCount: Int? = null,
    val positiveLymphNodesCount: Int? = null,

    val whoStatus: Int? = null,
    val asaClassificationPreSurgeryOrEndoscopy: AsaClassification? = null,
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
