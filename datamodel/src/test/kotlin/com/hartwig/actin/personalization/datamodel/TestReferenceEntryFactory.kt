package com.hartwig.actin.personalization.datamodel

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
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasurement
import com.hartwig.actin.personalization.datamodel.treatment.AnastomoticLeakageAfterSurgery
import com.hartwig.actin.personalization.datamodel.treatment.CircumferentialResectionMargin
import com.hartwig.actin.personalization.datamodel.treatment.Drug
import com.hartwig.actin.personalization.datamodel.treatment.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.treatment.GastroenterologyResectionType
import com.hartwig.actin.personalization.datamodel.treatment.HipecTreatment
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticRadiotherapyType
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticSurgery
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticSurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.PrimaryRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.PrimarySurgery
import com.hartwig.actin.personalization.datamodel.treatment.RadiotherapyType
import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryRadicality
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryTechnique
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryUrgency
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatmentDrug
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatmentScheme
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentIntent

object TestReferenceEntryFactory {

    fun emptyReferenceEntry() = ReferenceEntry(
        source = ReferenceSource.INTERNAL,
        sourceId = 1,
        diagnosisYear = 1971,
        ageAtDiagnosis = 63,
        sex = Sex.MALE,
        latestSurvivalMeasurement = SurvivalMeasurement(daysSinceDiagnosis = 251, isAlive = true),
        priorTumors = emptyList(),
        primaryDiagnosis = minimalPrimaryDiagnosis(),
        metastaticDiagnosis = minimalMetastaticDiagnosis(),
        whoAssessments = emptyList(),
        asaAssessments = emptyList(),
        comorbidityAssessments = emptyList(),
        molecularResults = emptyList(),
        labMeasurements = emptyList(),
        treatmentEpisodes = emptyList()
    )

    fun minimalReferenceEntry() = ReferenceEntry(
        source = ReferenceSource.INTERNAL,
        sourceId = 2,
        diagnosisYear = 1966,
        ageAtDiagnosis = 73,
        sex = Sex.MALE,
        latestSurvivalMeasurement = SurvivalMeasurement(daysSinceDiagnosis = 151, isAlive = false),
        priorTumors = listOf(minimalPriorTumor()),
        primaryDiagnosis = minimalPrimaryDiagnosis(),
        metastaticDiagnosis = minimalMetastaticDiagnosis(),
        whoAssessments = listOf(minimalWhoAssessment()),
        asaAssessments = listOf(minimalAsaAssessment()),
        comorbidityAssessments = listOf(minimalComorbidityAssessment()),
        molecularResults = listOf(minimalMolecularResult()),
        labMeasurements = listOf(minimalLabMeasurement()),
        treatmentEpisodes = listOf(minimalTreatmentEpisode())
    )

    fun exhaustiveReferenceEntry() = ReferenceEntry(
        source = ReferenceSource.INTERNAL,
        sourceId = 2,
        diagnosisYear = 1961,
        ageAtDiagnosis = 83,
        sex = Sex.MALE,
        latestSurvivalMeasurement = SurvivalMeasurement(daysSinceDiagnosis = 90, isAlive = true),
        priorTumors = listOf(minimalPriorTumor(), exhaustivePriorTumor()),
        primaryDiagnosis = exhaustivePrimaryDiagnosis(),
        metastaticDiagnosis = exhaustiveMetastaticDiagnosis(),
        whoAssessments = listOf(minimalWhoAssessment(), minimalWhoAssessment()),
        asaAssessments = listOf(minimalAsaAssessment(), minimalAsaAssessment()),
        comorbidityAssessments = listOf(minimalComorbidityAssessment(), minimalComorbidityAssessment()),
        molecularResults = listOf(minimalMolecularResult(), exhaustiveMolecularResult()),
        labMeasurements = listOf(minimalLabMeasurement(), exhaustiveLabMeasurement()),
        treatmentEpisodes = listOf(minimalTreatmentEpisode(), exhaustiveTreatmentEpisode())
    )

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
        isMetachronous = false,
        metastases = emptyList(),
        numberOfLiverMetastases = null,
        maximumSizeOfLiverMetastasisMm = null,
        investigatedLymphNodesCount = null,
        positiveLymphNodesCount = null
    )

    private fun exhaustiveMetastaticDiagnosis() = MetastaticDiagnosis(
        isMetachronous = true,
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

    private fun minimalWhoAssessment() = WhoAssessment(
        daysSinceDiagnosis = 2,
        whoStatus = 2
    )

    private fun minimalAsaAssessment() = AsaAssessment(
        daysSinceDiagnosis = 2,
        classification = AsaClassification.V
    )

    private fun minimalComorbidityAssessment() = ComorbidityAssessment(
        daysSinceDiagnosis = 2,
        charlsonComorbidityIndex = 1,
        hasAids = false,
        hasCongestiveHeartFailure = false,
        hasCollagenosis = false,
        hasCopd = false,
        hasCerebrovascularDisease = false,
        hasDementia = true,
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

    private fun minimalMolecularResult() = MolecularResult(
        daysSinceDiagnosis = 10,
        hasMsi = null,
        hasBrafMutation = null,
        hasBrafV600EMutation = null,
        hasRasMutation = null,
        hasKrasG12CMutation = null
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
        daysSinceDiagnosis = 0,
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

    private fun minimalGastroenterologyResection() = GastroenterologyResection(
        daysSinceDiagnosis = null, resectionType = GastroenterologyResectionType.ENDOSCOPIC_INTERMUSCULAR_DISSECTION
    )

    private fun exhaustiveGastroenterologyResection() = GastroenterologyResection(
        daysSinceDiagnosis = 10, resectionType = GastroenterologyResectionType.ENDOSCOPIC_INTERMUSCULAR_DISSECTION
    )

    private fun minimalPrimarySurgery() = PrimarySurgery(
        daysSinceDiagnosis = null,
        type = SurgeryType.HEMICOLECTOMY_OR_ILEOCECAL_RESECTION,
        technique = null,
        urgency = null,
        radicality = null,
        circumferentialResectionMargin = null,
        anastomoticLeakageAfterSurgery = null,
        hospitalizationDurationDays = null
    )

    private fun exhaustivePrimarySurgery() = PrimarySurgery(
        daysSinceDiagnosis = 10,
        type = SurgeryType.TRANSANAL_ENDOSCOPIC_MICROSURGERY,
        technique = SurgeryTechnique.CONVENTIONAL_SCOPIC_WITH_CONVERSION,
        urgency = SurgeryUrgency.PLACEMENT_STENT_OR_STOMA_LATER_FOLLOWED_BY_PLANNED_SURGERY,
        radicality = SurgeryRadicality.MICROSCOPIC_RADICAL,
        circumferentialResectionMargin = CircumferentialResectionMargin.RESECTION_MARGIN_BETWEEN_ZERO_AND_ONE_MM,
        anastomoticLeakageAfterSurgery = AnastomoticLeakageAfterSurgery.COMBINATION_OF_ANASTOMOTIC_LEAKAGE_AND_ABSCESS,
        hospitalizationDurationDays = 10
    )

    private fun minimalMetastaticSurgery() = MetastaticSurgery(
        daysSinceDiagnosis = null,
        type = MetastaticSurgeryType.BISEGMENT_OR_SEGMENT_RESECTION_OF_THE_LIVER_DUE_TO_METASTASES,
        radicality = null
    )

    private fun exhaustiveMetastaticSurgery() = MetastaticSurgery(
        daysSinceDiagnosis = 10,
        type = MetastaticSurgeryType.CRYOABLATION_DUE_TO_LUNG_METASTASES,
        radicality = SurgeryRadicality.MACROSCOPIC_IRRADICAL
    )

    private fun minimalHipecTreatment() = HipecTreatment(
        daysSinceDiagnosis = 24
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
        type = MetastaticRadiotherapyType.RADIOTHERAPY_ON_BRAIN_METASTASES_STEREOTACTIC_GAMMA_KNIFE_CYBER_KNIFE,
    )

    private fun exhaustiveMetastaticRadiotherapy() = MetastaticRadiotherapy(
        daysBetweenDiagnosisAndStart = 10,
        daysBetweenDiagnosisAndStop = 20,
        type = MetastaticRadiotherapyType.RADIOTHERAPY_ON_SKIN_METASTASES,
    )

    private fun minimalSystemicTreatment() = SystemicTreatment(
        daysBetweenDiagnosisAndStart = null,
        daysBetweenDiagnosisAndStop = null,
        treatment = Treatment.CAPECITABINE_BEVACIZUMAB,
        schemes = emptyList()
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
        schemes = listOf(minimalSystemicTreatmentScheme(), exhaustiveSystemicTreatmentScheme())
    )

    private fun minimalResponseMeasure() = ResponseMeasure(
        daysSinceDiagnosis = null, response = ResponseType.CR
    )

    private fun exhaustiveResponseMeasure() = ResponseMeasure(
        daysSinceDiagnosis = 10, response = ResponseType.MR
    )

    private fun minimalProgressionMeasure() = ProgressionMeasure(
        daysSinceDiagnosis = null, type = ProgressionMeasureType.PROGRESSION, followUpEvent = null
    )

    private fun exhaustiveProgressionMeasure() = ProgressionMeasure(
        daysSinceDiagnosis = 10,
        type = ProgressionMeasureType.CENSOR,
        followUpEvent = ProgressionMeasureFollowUpEvent.DISTANT_AND_POSSIBLY_REGIONAL_OR_LOCAL
    )

    private fun minimalTreatmentEpisode() = TreatmentEpisode(
        metastaticPresence = MetastaticPresence.AT_START,
        reasonRefrainmentFromTreatment = ReasonRefrainmentFromTreatment.NOT_APPLICABLE,
        gastroenterologyResections = listOf(minimalGastroenterologyResection()),
        primarySurgeries = listOf(minimalPrimarySurgery()),
        metastaticSurgeries = listOf(minimalMetastaticSurgery()),
        hipecTreatments = listOf(minimalHipecTreatment()),
        primaryRadiotherapies = listOf(minimalPrimaryRadiotherapy()),
        metastaticRadiotherapies = listOf(minimalMetastaticRadiotherapy()),
        systemicTreatments = listOf(minimalSystemicTreatment()),
        responseMeasures = listOf(minimalResponseMeasure()),
        progressionMeasures = listOf(minimalProgressionMeasure())
    )

    private fun exhaustiveTreatmentEpisode() = TreatmentEpisode(
        metastaticPresence = MetastaticPresence.AT_PROGRESSION,
        reasonRefrainmentFromTreatment = ReasonRefrainmentFromTreatment.NOT_APPLICABLE,
        gastroenterologyResections = listOf(minimalGastroenterologyResection(), exhaustiveGastroenterologyResection()),
        primarySurgeries = listOf(minimalPrimarySurgery(), exhaustivePrimarySurgery()),
        metastaticSurgeries = listOf(minimalMetastaticSurgery(), exhaustiveMetastaticSurgery()),
        hipecTreatments = listOf(minimalHipecTreatment()),
        primaryRadiotherapies = listOf(minimalPrimaryRadiotherapy(), exhaustivePrimaryRadiotherapy()),
        metastaticRadiotherapies = listOf(minimalMetastaticRadiotherapy(), exhaustiveMetastaticRadiotherapy()),
        systemicTreatments = listOf(minimalSystemicTreatment(), exhaustiveSystemicTreatment()),
        responseMeasures = listOf(minimalResponseMeasure(), exhaustiveResponseMeasure()),
        progressionMeasures = listOf(minimalProgressionMeasure(), exhaustiveProgressionMeasure())
    )
}