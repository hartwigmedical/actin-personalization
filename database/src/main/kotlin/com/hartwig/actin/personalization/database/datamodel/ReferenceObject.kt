package com.hartwig.actin.personalization.database.datamodel

import com.hartwig.actin.personalization.datamodel.ReferenceSource
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification
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

data class ReferenceObject(
    val source: ReferenceSource,
    val sourceId: Int,
    val diagnosisYear: Int,
    val ageAtDiagnosis: Int,
    val ageAtMetastaticDiagnosis: Int,
    val sex: Sex,

    val hadSurvivalEvent: Boolean,
    val survivalDaysSincePrimaryDiagnosis: Int,
    val survivalDaysSinceMetastaticDiagnosis: Int,
    val survivalDaysSinceTreatmentStart: Int?,

    val numberOfPriorTumors: Int,
    val hasDoublePrimaryTumor: Boolean,

    val basisOfDiagnosis: BasisOfDiagnosis,
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

    val charlsonComorbidityIndex: Int?,
    val hasAids: Boolean?,
    val hasCongestiveHeartFailure: Boolean?,
    val hasCollagenosis: Boolean?,
    val hasCopd: Boolean?,
    val hasCerebrovascularDisease: Boolean?,
    val hasDementia: Boolean?,
    val hasDiabetesMellitus: Boolean?,
    val hasDiabetesMellitusWithEndOrganDamage: Boolean?,
    val hasOtherMalignancy: Boolean?,
    val hasOtherMetastaticSolidTumor: Boolean?,
    val hasMyocardialInfarct: Boolean?,
    val hasMildLiverDisease: Boolean?,
    val hasHemiplegiaOrParaplegia: Boolean?,
    val hasPeripheralVascularDisease: Boolean?,
    val hasRenalDisease: Boolean?,
    val hasLiverDisease: Boolean?,
    val hasUlcerDisease: Boolean?,

    val daysBetweenPrimaryAndMetastaticDiagnosis: Int,
    val isMetachronous: Boolean,
    val hasLiverOrIntrahepaticBileDuctMetastases: Boolean,
    val numberOfLiverMetastases: NumberOfLiverMetastases?,
    val maximumSizeOfLiverMetastasisMm: Int?,
    val hasLymphNodeMetastases: Boolean,
    val investigatedLymphNodesCountMetastaticDiagnosis: Int?,
    val positiveLymphNodesCountMetastaticDiagnosis: Int?,
    val hasPeritonealMetastases: Boolean,
    val hasBronchusOrLungMetastases: Boolean,
    val hasBrainMetastases: Boolean,
    val hasOtherMetastases: Boolean,

    val whoAssessmentAtMetastaticDiagnosis: Int?,
    val asaAssessmentAtMetastaticDiagnosis: AsaClassification?,
    val lactateDehydrogenaseAtMetastaticDiagnosis: Double?,
    val alkalinePhosphataseAtMetastaticDiagnosis: Double?,
    val leukocytesAbsoluteAtMetastaticDiagnosis: Double?,
    val carcinoembryonicAntigenAtMetastaticDiagnosis: Double?,
    val albumineAtMetastaticDiagnosis: Double?,
    val neutrophilsAbsoluteAtMetastaticDiagnosis: Double?,

    val hasMsi: Boolean?,
    val hasBrafMutation: Boolean?,
    val hasBrafV600EMutation: Boolean?,
    val hasRasMutation: Boolean?,
    val hasKrasG12CMutation: Boolean?,

    val hasHadPrimarySurgeryPriorToMetastaticTreatment: Boolean,
    val hasHadPrimarySurgeryDuringMetastaticTreatment: Boolean,
    val hasHadGastroenterologySurgeryPriorToMetastaticTreatment: Boolean,
    val hasHadGastroenterologySurgeryDuringMetastaticTreatment: Boolean,
    val hasHadHipecPriorToMetastaticTreatment: Boolean,
    val hasHadHipecDuringMetastaticTreatment: Boolean,
    val hasHadPrimaryRadiotherapyPriorToMetastaticTreatment: Boolean,
    val hasHadPrimaryRadiotherapyDuringMetastaticTreatment: Boolean,

    val hasHadMetastaticSurgery: Boolean,
    val hasHadMetastaticRadiotherapy: Boolean,

    val hasHadSystemicTreatmentPriorToMetastaticTreatment: Boolean,
    val isMetastaticPriorToMetastaticTreatmentDecision: Boolean,
    val reasonRefrainmentFromTreatment: String,
    val daysBetweenMetastaticDiagnosisAndTreatmentStart: Int?,
    val systemicTreatmentsAfterMetastaticDiagnosis: Int,
    val firstSystemicTreatmentAfterMetastaticDiagnosis: String?,
    val firstSystemicTreatmentDurationDays: Int?,
    val hadProgressionEvent: Boolean?,
    val daysBetweenTreatmentStartAndProgression: Int?
)
