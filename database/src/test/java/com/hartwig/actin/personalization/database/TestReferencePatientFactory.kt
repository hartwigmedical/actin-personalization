package com.hartwig.actin.personalization.database

import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification
import com.hartwig.actin.personalization.datamodel.assessment.ComorbidityAssessment
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasure
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasurement
import com.hartwig.actin.personalization.datamodel.assessment.MolecularResult
import com.hartwig.actin.personalization.datamodel.assessment.Unit
import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment
import com.hartwig.actin.personalization.datamodel.diagnosis.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.BasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.DifferentiationGrade
import com.hartwig.actin.personalization.datamodel.diagnosis.ExtraMuralInvasionCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.LymphaticInvasionCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.diagnosis.Metastasis
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.NumberOfLiverMetastases
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.diagnosis.Sidedness
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmN
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmT
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorRegression
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorStage
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.diagnosis.VenousInvasionDescription
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.outcome.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ResponseType
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasure
import com.hartwig.actin.personalization.datamodel.treatment.AnastomoticLeakageAfterSurgery
import com.hartwig.actin.personalization.datamodel.treatment.Drug
import com.hartwig.actin.personalization.datamodel.treatment.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.treatment.GastroenterologyResectionType
import com.hartwig.actin.personalization.datamodel.treatment.HipecTreatment
import com.hartwig.actin.personalization.datamodel.treatment.MetastasesRadiotherapyType
import com.hartwig.actin.personalization.datamodel.treatment.MetastasesSurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticSurgery
import com.hartwig.actin.personalization.datamodel.treatment.PrimaryRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.PrimarySurgery
import com.hartwig.actin.personalization.datamodel.treatment.RadiotherapyType
import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTumorDirectedTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryCircumferentialResectionMargin
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryRadicality
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryTechnique
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryUrgency
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatmentDrug
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatmentScheme
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentIntent

object TestReferencePatientFactory {

    private fun minimalPriorTumor() = PriorTumor(
        daysBeforeDiagnosis = 120,
        primaryTumorType = TumorType.GASTROINTESTINAL_STROMAL_TUMOR,
        primaryTumorLocation = TumorLocation.ABDOMEN_NOS,
        primaryTumorLocationCategory = TumorLocationCategory.DIGESTIVE_TRACT,
        primaryTumorStage = null,
        systemicDrugsReceived = emptyList()
    )

    private fun exhaustivePriorTumor() = PriorTumor(
        daysBeforeDiagnosis = 732,
        primaryTumorType = TumorType.GASTROINTESTINAL_STROMAL_TUMOR,
        primaryTumorLocation = TumorLocation.ABDOMEN_NOS,
        primaryTumorLocationCategory = TumorLocationCategory.DIGESTIVE_TRACT,
        primaryTumorStage = TumorStage.II,
        systemicDrugsReceived = listOf(Drug.IMATINIB, Drug.TEGAFUR)
    )

    private fun minimalPrimaryDiagnosis() = PrimaryDiagnosis(
        basisOfDiagnosis = BasisOfDiagnosis.HISTOLOGICAL_CONFIRMATION,
        hasDoublePrimaryTumor = false,
        primaryTumorType = TumorType.CRC_OTHER,
        primaryTumorLocation = TumorLocation.DESCENDING_COLON,
        differentiationGrade = null,
        clinicalTnmClassification = TnmClassification(tnmT = null, tnmN = null, tnmM = null),
        pathologicalTnmClassification = TnmClassification(tnmT = null, tnmN = null, tnmM = null),
        clinicalTumorStage = TumorStage.II,
        pathologicalTumorStage = TumorStage.IV,
        investigatedLymphNodesCount = null,
        positiveLymphNodesCount = null,
        venousInvasionDescription = null,
        lymphaticInvasionCategory = null,
        extraMuralInvasionCategory = null,
        tumorRegression = null,
        sidedness = null,
        presentedWithIleus = null,
        presentedWithPerforation = null,
        anorectalVergeDistanceCategory = null,
        mesorectalFasciaIsClear = null,
        distanceToMesorectalFasciaMm = null
    )

    private fun exhaustivePrimaryDiagnosis() = minimalPrimaryDiagnosis().copy(
        hasDoublePrimaryTumor = true,
        differentiationGrade = DifferentiationGrade.GRADE_4_OR_UNDIFFERENTIATED_OR_ANAPLASTIC_OR_GGG4,
        clinicalTnmClassification = TnmClassification(tnmT = TnmT.T2, tnmN = TnmN.N1, tnmM = null),
        pathologicalTnmClassification = TnmClassification(tnmT = TnmT.T3, tnmN = null, tnmM = TnmM.M1),
        investigatedLymphNodesCount = 10,
        positiveLymphNodesCount = 10,
        venousInvasionDescription = VenousInvasionDescription.EXTRAMURAL,
        lymphaticInvasionCategory = LymphaticInvasionCategory.PRESENT,
        extraMuralInvasionCategory = ExtraMuralInvasionCategory.LESS_THAN_FIVE_MM,
        tumorRegression = TumorRegression.NO_SIGNS_OF_REGRESSION,
        sidedness = Sidedness.LEFT,
        presentedWithIleus = false,
        presentedWithPerforation = false,
        anorectalVergeDistanceCategory = AnorectalVergeDistanceCategory.TEN_TO_FIFTEEN_CM,
        mesorectalFasciaIsClear = false,
        distanceToMesorectalFasciaMm = 2
    )

    private fun minimalMetastaticDiagnosis() = MetastaticDiagnosis(
        distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_START, metastases = emptyList()
    )

    private fun exhaustiveMetastaticDiagnosis() = MetastaticDiagnosis(
        distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_START,
        metastases = listOf(
            Metastasis(
                daysSinceDiagnosis = null, location = TumorLocation.OVERLAPPING_BLADDER_LOCATION, isLinkedToProgression = null
            ), Metastasis(
                daysSinceDiagnosis = 2,
                location = TumorLocation.PERIPHERAL_NERVE_AUTONOMIC_NS_UPPER_EXTR_SHOULDER,
                isLinkedToProgression = false
            )
        ),
        numberOfLiverMetastases = NumberOfLiverMetastases.MULTIPLE_BUT_EXACT_NUMBER_UNKNOWN,
        maximumSizeOfLiverMetastasisMm = 10,
        investigatedLymphNodesCount = 10,
        positiveLymphNodesCount = 10
    )

    private fun exhaustiveComorbidityAssessment() = ComorbidityAssessment(
        charlsonComorbidityIndex = 1,
        daysSinceDiagnosis = 2,
        hasAids = false,
        hasCongestiveHeartFailure = false,
        hasCollagenosis = false,
        hasCopd = false,
        hasCerebrovascularDisease = false,
        hasDementia = false,
        hasDiabetesMellitus = false,
        hasDiabetesMellitusWithEndOrganDamage = false,
        hasOtherMalignancy = false,
        hasOtherMetastaticSolidTumor = false,
        hasMyocardialInfarct = false,
        hasMildLiverDisease = false,
        hasHemiplegiaOrParaplegia = false,
        hasPeripheralVascularDisease = false,
        hasRenalDisease = false,
        hasLiverDisease = false,
        hasUlcerDisease = false
    )

    private fun exhaustiveMolecularResult() = MolecularResult(
        daysSinceDiagnosis = 10,
        hasMsi = false,
        hasBrafMutation = true,
        hasBrafV600EMutation = false,
        hasRasMutation = false,
        hasKrasG12CMutation = false
    )

    private fun minimalLabMeasurement() = LabMeasurement(
        daysSinceDiagnosis = null,
        name = LabMeasure.CARCINOEMBRYONIC_ANTIGEN,
        value = 10.0,
        unit = Unit.MICROGRAM_PER_LITER,
        isPreSurgical = null,
        isPostSurgical = null
    )

    private fun exhaustiveLabMeasurement() = LabMeasurement(
        daysSinceDiagnosis = 10,
        name = LabMeasure.CARCINOEMBRYONIC_ANTIGEN,
        value = 10.0,
        unit = Unit.MICROGRAM_PER_LITER,
        isPreSurgical = false,
        isPostSurgical = true
    )

    private fun exhaustivePrimarySurgery() = PrimarySurgery(
        daysSinceDiagnosis = 10,
        type = SurgeryType.TRANSANAL_ENDOSCOPIC_MICROSURGERY,
        technique = SurgeryTechnique.CONVENTIONAL_SCOPIC_WITH_CONVERSION,
        urgency = SurgeryUrgency.PLACEMENT_STENT_OR_STOMA_LATER_FOLLOWED_BY_PLANNED_SURGERY,
        radicality = SurgeryRadicality.MICROSCOPIC_RADICAL,
        circumferentialResectionMargin = SurgeryCircumferentialResectionMargin.RESECTION_MARGIN_BETWEEN_ZERO_AND_ONE_MM,
        anastomoticLeakageAfterSurgery = AnastomoticLeakageAfterSurgery.COMBINATION_OF_ANASTOMOTIC_LEAKAGE_AND_ABSCESS,
        hospitalizationDurationDays = 10
    )

    private fun minimalMetastaticSurgery() = MetastaticSurgery(
        daysSinceDiagnosis = null,
        type = MetastasesSurgeryType.BISEGMENT_OR_SEGMENT_RESECTION_OF_THE_LIVER_DUE_TO_METASTASES,
        radicality = null
    )

    private fun exhaustiveMetastaticSurgery() = MetastaticSurgery(
        daysSinceDiagnosis = 10,
        type = MetastasesSurgeryType.CRYOABLATION_DUE_TO_LUNG_METASTASES,
        radicality = SurgeryRadicality.MACROSCOPIC_IRRADICAL
    )

    private fun minimalPrimaryRadiotherapy() = PrimaryRadiotherapy(
        daysBetweenDiagnosisAndStart = null, daysBetweenDiagnosisAndStop = null, type = RadiotherapyType.INTRA_OPERATIVE, totalDosage = null
    )

    private fun exhaustivePrimaryRadiotherapy() = PrimaryRadiotherapy(
        daysBetweenDiagnosisAndStart = 10, daysBetweenDiagnosisAndStop = 20, type = RadiotherapyType.LONG_DURATION, totalDosage = 10.0
    )

    private fun minimalMetastaticRadiotherapy() = MetastaticRadiotherapy(
        daysBetweenDiagnosisAndStart = null,
        daysBetweenDiagnosisAndStop = null,
        type = MetastasesRadiotherapyType.RADIOTHERAPY_ON_BRAIN_METASTASES_STEREOTACTIC_GAMMA_KNIFE_CYBER_KNIFE,
    )

    private fun exhaustiveMetastaticRadiotherapy() = MetastaticRadiotherapy(
        daysBetweenDiagnosisAndStart = 10,
        daysBetweenDiagnosisAndStop = 20,
        type = MetastasesRadiotherapyType.RADIOTHERAPY_ON_SKIN_METASTASES,
    )

    private fun minimalSystemicTreatment() = SystemicTreatment(
        daysBetweenDiagnosisAndStart = null,
        daysBetweenDiagnosisAndStop = null,
        treatment = Treatment.CAPECITABINE_BEVACIZUMAB,
        systemicTreatmentSchemes = emptyList()
    )

    private fun minimalSystemicTreatmentScheme() = SystemicTreatmentScheme(
        minDaysBetweenDiagnosisAndStart = null,
        maxDaysBetweenDiagnosisAndStart = null,
        minDaysBetweenDiagnosisAndStop = null,
        maxDaysBetweenDiagnosisAndStop = null,
        components = emptyList()
    )

    private fun minimalSystemicTreatmentDrug() = SystemicTreatmentDrug(
        daysBetweenDiagnosisAndStart = null,
        daysBetweenDiagnosisAndStop = null,
        drug = Drug.EXTERNAL_RADIOTHERAPY_WITH_SENSITIZER,
        numberOfCycles = null,
        intent = null,
        drugTreatmentIsOngoing = null,
        isAdministeredPreSurgery = null,
        isAdministeredPostSurgery = null
    )

    private fun exhaustiveSystemicTreatmentDrug() = SystemicTreatmentDrug(
        daysBetweenDiagnosisAndStart = 10,
        daysBetweenDiagnosisAndStop = 20,
        drug = Drug.EXTERNAL_RADIOTHERAPY_WITH_SENSITIZER,
        numberOfCycles = 10,
        intent = TreatmentIntent.MAINTENANCE,
        drugTreatmentIsOngoing = true,
        isAdministeredPreSurgery = false,
        isAdministeredPostSurgery = false
    )

    private fun exhaustiveSystemicTreatmentScheme() = SystemicTreatmentScheme(
        minDaysBetweenDiagnosisAndStart = 10,
        maxDaysBetweenDiagnosisAndStart = 20,
        minDaysBetweenDiagnosisAndStop = 30,
        maxDaysBetweenDiagnosisAndStop = 40,
        components = listOf(minimalSystemicTreatmentDrug(), exhaustiveSystemicTreatmentDrug())
    )

    private fun exhaustiveSystemicTreatment() = SystemicTreatment(
        daysBetweenDiagnosisAndStart = 10,
        daysBetweenDiagnosisAndStop = 20,
        treatment = Treatment.CAPECITABINE_BEVACIZUMAB,
        systemicTreatmentSchemes = listOf(minimalSystemicTreatmentScheme(), exhaustiveSystemicTreatmentScheme())
    )


    private fun minimalTumor() = Tumor(
        diagnosisYear = 1961,
        ageAtDiagnosis = 73,
        latestSurvivalStatus = SurvivalMeasure(daysSinceDiagnosis = 151, isAlive = true),
        priorTumors = listOf(minimalPriorTumor()),
        primaryDiagnosis = minimalPrimaryDiagnosis(),
        metastaticDiagnosis = minimalMetastaticDiagnosis(),
        hasReceivedTumorDirectedTreatment = false,
        hipecTreatment = HipecTreatment(hasHadHipecTreatment = false),
    )

    private fun exhaustiveTumor() = Tumor(
        diagnosisYear = 1961,
        ageAtDiagnosis = 83,
        latestSurvivalStatus = SurvivalMeasure(daysSinceDiagnosis = 90, isAlive = true),

        priorTumors = listOf(minimalPriorTumor(), exhaustivePriorTumor()),

        primaryDiagnosis = exhaustivePrimaryDiagnosis(),
        metastaticDiagnosis = exhaustiveMetastaticDiagnosis(),

        whoAssessments = listOf(
            WhoAssessment(daysSinceDiagnosis = 1, whoStatus = 2), WhoAssessment(daysSinceDiagnosis = 10, whoStatus = 2)
        ),
        asaAssessments = listOf(AsaAssessment(daysSinceDiagnosis = 1, asaClassification = AsaClassification.II)),

        comorbidityAssessments = listOf(exhaustiveComorbidityAssessment()),
        molecularResults = listOf(MolecularResult(daysSinceDiagnosis = 10), exhaustiveMolecularResult()),
        labMeasurements = listOf(minimalLabMeasurement(), exhaustiveLabMeasurement()),

        hasReceivedTumorDirectedTreatment = false,
        reasonRefrainmentFromTumorDirectedTreatment = ReasonRefrainmentFromTumorDirectedTreatment.COMORBIDITY_AND_OR_PERFORMANCE_OR_FUNCTIONAL_STATUS_OR_PRESENCE_OTHER_TUMOR,
        hasParticipatedInTrial = false,

        gastroenterologyResections = listOf(
            GastroenterologyResection(
                daysSinceDiagnosis = null, resectionType = GastroenterologyResectionType.ENDOSCOPIC_INTERMUSCULAR_DISSECTION
            ), GastroenterologyResection(
                daysSinceDiagnosis = 10, resectionType = GastroenterologyResectionType.ENDOSCOPIC_INTERMUSCULAR_DISSECTION
            )
        ),
        primarySurgeries = listOf(
            PrimarySurgery(type = SurgeryType.HEMICOLECTOMY_OR_ILEOCECAL_RESECTION), exhaustivePrimarySurgery()
        ),
        metastaticSurgeries = listOf(minimalMetastaticSurgery(), exhaustiveMetastaticSurgery()),
        hipecTreatment = HipecTreatment(daysSinceDiagnosis = 10, hasHadHipecTreatment = false),
        primaryRadiotherapies = listOf(minimalPrimaryRadiotherapy(), exhaustivePrimaryRadiotherapy()),
        metastaticRadiotherapies = listOf(minimalMetastaticRadiotherapy(), exhaustiveMetastaticRadiotherapy()),
        systemicTreatments = listOf(minimalSystemicTreatment(), exhaustiveSystemicTreatment()),
        responseMeasures = listOf(
            ResponseMeasure(daysSinceDiagnosis = null, response = ResponseType.CR),
            ResponseMeasure(daysSinceDiagnosis = 10, response = ResponseType.MR)
        ),
        progressionMeasures = listOf(
            ProgressionMeasure(
                daysSinceDiagnosis = null, type = ProgressionMeasureType.PROGRESSION, followUpEvent = null
            ), ProgressionMeasure(
                daysSinceDiagnosis = 10,
                type = ProgressionMeasureType.CENSOR,
                followUpEvent = ProgressionMeasureFollowUpEvent.DISTANT_AND_POSSIBLY_REGIONAL_OR_LOCAL
            )
        )
    )

    fun minimalReferencePatientRecord() = ReferencePatient(
        sex = Sex.MALE, tumors = listOf(minimalTumor())
    )

    fun exhaustiveReferencePatientRecord() = ReferencePatient(
        sex = Sex.MALE, tumors = listOf(exhaustiveTumor(), minimalTumor())
    )
}