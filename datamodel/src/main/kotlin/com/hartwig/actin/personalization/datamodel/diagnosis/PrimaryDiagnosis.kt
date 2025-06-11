package com.hartwig.actin.personalization.datamodel.diagnosis

import kotlinx.serialization.Serializable

@Serializable
data class PrimaryDiagnosis(
    val basisOfDiagnosis: BasisOfDiagnosis,
    val hasDoublePrimaryTumor: Boolean,

    val primaryTumorType: TumorType,
    val primaryTumorLocation: TumorLocation,
    val sidedness: Sidedness?,
    val anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory?,
    val mesorectalFasciaIsClear: Boolean?,
    val distanceToMesorectalFasciaMm: Int?,

    val differentiationGrade: DifferentiationGrade?,
    val clinicalTnmClassification: TnmClassification,
    val pathologicalTnmClassification: TnmClassification?,
    val clinicalTumorStage: TumorStage,
    val pathologicalTumorStage: TumorStage,
    val investigatedLymphNodesCount: Int?,
    val positiveLymphNodesCount: Int?,

    val presentedWithIleus: Boolean?,
    val presentedWithPerforation: Boolean?,
    val venousInvasionDescription: VenousInvasionDescription?,
    val lymphaticInvasionCategory: LymphaticInvasionCategory?,
    val extraMuralInvasionCategory: ExtraMuralInvasionCategory?,
    val tumorRegression: TumorRegression?
)

