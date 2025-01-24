package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.Sidedness
import com.hartwig.actin.personalization.datamodel.TumorType

data class TumorEntry(
    val diagnosisYear: Int,
    val ageAtDiagnosis: Int,
    val tumorType: TumorType,
    val tumorLocations: Set<Location>,
    val sidedness: Sidedness? = determineSidedness(tumorLocations),
    val isMetachronous: Boolean,

    val priorTumors: List<PriorTumor>,
    val presentedWithIleus: Boolean? = null,
    val presentedWithPerforation: Boolean? = null,
    val anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory? = null,

    /* TODO KD: Check whether fields are per episode or baseline only.
    val tumorBasisOfDiagnosis: TumorBasisOfDiagnosis,
    val tumorLocation: Location,
    val tumorDifferentiationGrade: TumorDifferentiationGrade? = null,
    val tnmCT: TnmT? = null,
    val tnmCN: TnmN? = null,
    val tnmCM: TnmM? = null,
    val tnmPT: TnmT? = null,
    val tnmPN: TnmN? = null,
    val tnmPM: TnmM? = null,
    val stageCTNM: StageTnm? = null,
    val stagePTNM: StageTnm? = null,
    val stageTNM: StageTnm? = null,
    val investigatedLymphNodesNumber: Int? = null,
    val positiveLymphNodesNumber: Int? = null,
     */

    val daysBetweenDiagnosisAndLatestAliveStatusFollowup: Int,
    val wasAliveAtLatestAliveStatusFollowup: Boolean,

    val comorbiditiesAtDiagnosis : ComorbidityAssessment?,
    val molecularResultAtDiagnosis : MolecularResult,
    val treatments: List<TreatmentEntry>

    /*
    val distantMetastasesDetectionStatus: MetastasesDetectionStatus,
    val metastases: List<Metastasis>,
    val numberOfLiverMetastases: NumberOfLiverMetastases? = null,
    val maximumSizeOfLiverMetastasisMm: Int? = null,

    val hasDoublePrimaryTumor: Boolean? = null,
    val mesorectalFasciaIsClear: Boolean? = null,
    val distanceToMesorectalFasciaMm: Int? = null,
    val venousInvasionDescription: VenousInvasionDescription? = null,
    val lymphaticInvasionCategory: LymphaticInvasionCategory? = null,
    val extraMuralInvasionCategory: ExtraMuralInvasionCategory? = null,
    val tumorRegression: TumorRegression? = null,

    val labMeasurements: List<LabMeasurement> = emptyList(),

    val hasReceivedTumorDirectedTreatment: Boolean,
    val reasonRefrainmentFromTumorDirectedTreatment: ReasonRefrainmentFromTumorDirectedTreatment? = null,
    val hasParticipatedInTrial: Boolean? = null,

    val gastroenterologyResections: List<GastroenterologyResection> = emptyList(),
    val surgeries: List<Surgery> = emptyList(),
    val metastasesSurgeries: List<MetastasesSurgery> = emptyList(),
    val radiotherapies: List<Radiotherapy> = emptyList(),
    val metastasesRadiotherapies: List<MetastasesRadiotherapy> = emptyList(),
    val hasHadHipecTreatment: Boolean,
    val intervalTumorIncidenceHipecTreatmentDays: Int? = null,
    val hasHadPreSurgeryRadiotherapy: Boolean,
    val hasHadPostSurgeryRadiotherapy: Boolean,
    val hasHadPreSurgeryChemoRadiotherapy: Boolean,
    val hasHadPostSurgeryChemoRadiotherapy: Boolean,
    val hasHadPreSurgerySystemicChemotherapy: Boolean,
    val hasHadPostSurgerySystemicChemotherapy: Boolean,
    val hasHadPreSurgerySystemicTargetedTherapy: Boolean,
    val hasHadPostSurgerySystemicTargetedTherapy: Boolean,

    val responseMeasure: ResponseMeasure? = null,
    val pfsMeasures: List<PfsMeasure>,
    val systemicTreatmentPlan: SystemicTreatmentPlan? = null,
    val ageAtTreatmentPlanStart: Int? = null
     */
)

private fun determineSidedness(locations: Set<Location>): Sidedness? {
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
