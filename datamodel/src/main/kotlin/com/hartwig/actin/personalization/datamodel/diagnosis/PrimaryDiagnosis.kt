package com.hartwig.actin.personalization.datamodel.diagnosis

import kotlinx.serialization.Serializable

@Serializable
data class PrimaryDiagnosis(
    val basisOfDiagnosis: BasisOfDiagnosis,
    val hasDoublePrimaryTumor: Boolean,
    val primaryTumorType: TumorType,
    val primaryTumorLocation: TumorLocation,
    val differentiationGrade: DifferentiationGrade?,

    val clinicalTnmClassification: TnmClassification,
    val pathologicalTnmClassification: TnmClassification,
    val clinicalTumorStage: TumorStage,
    val pathologicalTumorStage: TumorStage,
    val investigatedLymphNodesCount: Int?,
    val positiveLymphNodesCount: Int?,

    val venousInvasionDescription: VenousInvasionDescription?,
    val lymphaticInvasionCategory: LymphaticInvasionCategory?,
    val extraMuralInvasionCategory: ExtraMuralInvasionCategory?,
    val tumorRegression: TumorRegression?,

    // KD: Specific for CRC, could be hidden behind interface eventually
    val sidedness: Sidedness?,
    val presentedWithIleus: Boolean?,
    val presentedWithPerforation: Boolean?,

    // KD: Only present in case location == RECTUM, could be hidden behind interface eventually.
    val anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory?,

    // KD: Only present in case location in (RECTUM, RECTOSIGMOID), could be hidden behind interface eventually
    val mesorectalFasciaIsClear: Boolean?,
    val distanceToMesorectalFasciaMm: Int?,
)

