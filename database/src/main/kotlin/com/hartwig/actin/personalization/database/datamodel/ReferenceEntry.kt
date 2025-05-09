package com.hartwig.actin.personalization.database.datamodel

import com.hartwig.actin.personalization.datamodel.ReferenceSource
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.diagnosis.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.BasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.DifferentiationGrade
import com.hartwig.actin.personalization.datamodel.diagnosis.ExtraMuralInvasionCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.LymphaticInvasionCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.NumberOfLiverMetastases
import com.hartwig.actin.personalization.datamodel.diagnosis.Sidedness
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmN
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmT
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorRegression
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorStage
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.diagnosis.VenousInvasionDescription

data class ReferenceEntry(
    val source: ReferenceSource,
    val sourceId: Int,
    val diagnosisYear: Int,
    val ageAtDiagnosis: Int,
    val isAlive: Boolean,
    val sex: Sex,
    val basisOfDiagnosis: BasisOfDiagnosis,
    val numberOfPriorTumors: Int,
    val hasDoublePrimaryTumor: Boolean,
    val primaryTumorType: TumorType,
    val primaryTumorLocation: TumorLocation,
    val sidedness: Sidedness?,
    val anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory?,
    val mesorectalFasciaIsClear: Boolean?,
    val distanceToMesorectalFasciaMm: Int?,
    val differentiationGrade: DifferentiationGrade?,
    val clinicalTnmT: TnmT?,
    val clinicalTnmN: TnmN?,
    val clinicalTnmM: TnmM?,
    val pathologicalTnmT: TnmT?,
    val pathologicalTnmN: TnmN?,
    val pathologicalTnmM: TnmM?,
    val clinicalTumorStage: TumorStage,
    val pathologicalTumorStage: TumorStage,
    val investigatedLymphNodesCountPrimaryDiagnosis: Int?,
    val positiveLymphNodesCountPrimaryDiagnosis: Int?,
    val presentedWithIleus: Boolean?,
    val presentedWithPerforation: Boolean?,
    val venousInvasionDescription: VenousInvasionDescription?,
    val lymphaticInvasionCategory: LymphaticInvasionCategory?,
    val extraMuralInvasionCategory: ExtraMuralInvasionCategory?,
    val tumorRegression: TumorRegression?,
    val isMetachronous: Boolean,
    val numberOfLiverMetastases: NumberOfLiverMetastases?,
    val maximumSizeOfLiverMetastasisMm: Int?,
    val investigatedLymphNodesCountMetastaticDiagnosis: Int?,
    val positiveLymphNodesCountMetastaticDiagnosis: Int?,
    val metastasisLocationGroups: String,
    val metastasisLocationGroupsDays: String,
    val earliestDistantMetastasisDetectionDays: Int?,
    val AllWhoAssessments: String,
    val WhoAssessmentsDates: String,
    val WhoAssessmentBeforeMetastasisTreatment: Double,
    val WhoAssessmentDateBeforeMetastasisTreatment: Double,
    val WhoAssessmentAtMetastasisDetection: Double,
    val WhoAssessmentDateAtMetastasisDetection: Double,
    val AllAsaAssessments: String,
    val AsaAssessmentsDates: String,
    val AsaAssessmentBeforeMetastasisTreatment: String,
    val AsaAssessmentDateBeforeMetastasisTreatment: Double,
    val AsaAssessmentAtMetastasisDetection: String,
    val AsaAssessmentDateAtMetastasisDetection: Double,
    val lactateDehydrogenaseMetastasisDetection: Double,
    val alkalinePhosphataseMetastasisDetection: Double,
    val leukocytesAbsoluteMetastasisDetection: Double,
    val carcinoembryonicAntigenMetastasisDetection: Double,
    val albumineMetastasisDetection: Double,
    val neutrophilsAbsoluteMetastasisDetection: Double,
    val closestLabValueDateMetastasisDetection: Double,
    val surgeriesPrimary: String,
    val surgeriesPrimaryDates: String,
    val surgeriesMetastatic: String,
    val surgeriesMetastaticDates: String,
    val surgeriesGastroenterology: String,
    val surgeriesGastroenterologyDates: String,
    val hadHipec: Int,
    val hadHipecDate: Double,
    val radiotherapiesPrimary: String,
    val radiotherapiesPrimaryDates: String,
    val radiotherapiesMetastatic: String,
    val radiotherapiesMetastaticDates: String,
    val treatment: String,
    val firstTreatmentStartAfterMetastasis: Double,
    val treatmentStop: Double,
    val numberOfCycles: Double,
    val hasHadPriorSystemicTherapy: Int,
    val hadSurvivalEvent: Int,
    val hasHadPriorTumor: Int,
    val observedOsFromTumorIncidenceDays: Int,
    val observedOsFromMetastasisDetectionDays: Double,
    val observedOsFromTreatmentStartDays: Double,
    val systemicTreatmentPlanDuration: Double
)
