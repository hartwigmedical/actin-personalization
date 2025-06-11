package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasure
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasurement
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
import com.hartwig.actin.personalization.datamodel.diagnosis.Sidedness
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmN
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmT
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorRegression
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorStage
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.diagnosis.VenousInvasionDescription
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.outcome.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.treatment.AnastomoticLeakageAfterSurgery
import com.hartwig.actin.personalization.datamodel.treatment.CircumferentialResectionMargin
import com.hartwig.actin.personalization.datamodel.treatment.Drug
import com.hartwig.actin.personalization.datamodel.treatment.DrugScheme
import com.hartwig.actin.personalization.datamodel.treatment.DrugTreatment
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
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentIntent

object TestDatamodelFactory {

    fun primaryDiagnosis(
        basisOfDiagnosis: BasisOfDiagnosis = BasisOfDiagnosis.CLINICAL_ONLY_INVESTIGATION,
        hasDoublePrimaryTumor: Boolean = false,
        primaryTumorType: TumorType = TumorType.CRC_ADENOCARCINOMA,
        primaryTumorLocation: TumorLocation = TumorLocation.COECUM,
        sidedness: Sidedness? = null,
        anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory? = null,
        mesorectalFasciaIsClear: Boolean? = null,
        distanceToMesorectalFasciaMm: Int? = null,
        differentiationGrade: DifferentiationGrade? = null,
        clinicalTnmClassification: TnmClassification = TnmClassification(TnmT.T1, TnmN.N0, TnmM.M0),
        pathologicalTnmClassification: TnmClassification? = null,
        clinicalTumorStage: TumorStage = TumorStage.II,
        pathologicalTumorStage: TumorStage = TumorStage.II,
        investigatedLymphNodesCount: Int? = null,
        positiveLymphNodesCount: Int? = null,
        presentedWithIleus: Boolean? = null,
        presentedWithPerforation: Boolean? = null,
        venousInvasionDescription: VenousInvasionDescription? = null,
        lymphaticInvasionCategory: LymphaticInvasionCategory? = null,
        extraMuralInvasionCategory: ExtraMuralInvasionCategory? = null,
        tumorRegression: TumorRegression? = null
    ): PrimaryDiagnosis {
        return PrimaryDiagnosis(
            basisOfDiagnosis = basisOfDiagnosis,
            hasDoublePrimaryTumor = hasDoublePrimaryTumor,
            primaryTumorType = primaryTumorType,
            primaryTumorLocation = primaryTumorLocation,
            sidedness = sidedness,
            anorectalVergeDistanceCategory = anorectalVergeDistanceCategory,
            mesorectalFasciaIsClear = mesorectalFasciaIsClear,
            distanceToMesorectalFasciaMm = distanceToMesorectalFasciaMm,
            differentiationGrade = differentiationGrade,
            clinicalTnmClassification = clinicalTnmClassification,
            pathologicalTnmClassification = pathologicalTnmClassification,
            clinicalTumorStage = clinicalTumorStage,
            pathologicalTumorStage = pathologicalTumorStage,
            investigatedLymphNodesCount = investigatedLymphNodesCount,
            positiveLymphNodesCount = positiveLymphNodesCount,
            presentedWithIleus = presentedWithIleus,
            presentedWithPerforation = presentedWithPerforation,
            venousInvasionDescription = venousInvasionDescription,
            lymphaticInvasionCategory = lymphaticInvasionCategory,
            extraMuralInvasionCategory = extraMuralInvasionCategory,
            tumorRegression = tumorRegression
        )
    }

    fun metastaticDiagnosis(
        isMetachronous: Boolean = false,
        metastases: List<Metastasis> = emptyList(),
        numberOfLiverMetastases: NumberOfLiverMetastases? = null,
        maximumSizeOfLiverMetastasisMm: Int? = null,
        clinicalTnmClassification: TnmClassification? = null,
        pathologicalTnmClassification: TnmClassification? = null,
        investigatedLymphNodesCount: Int? = null,
        positiveLymphNodesCount: Int? = null
    ): MetastaticDiagnosis {
        return MetastaticDiagnosis(
            isMetachronous = isMetachronous,
            metastases = metastases,
            numberOfLiverMetastases = numberOfLiverMetastases,
            maximumSizeOfLiverMetastasisMm = maximumSizeOfLiverMetastasisMm,
            clinicalTnmClassification = clinicalTnmClassification,
            pathologicalTnmClassification = pathologicalTnmClassification,
            investigatedLymphNodesCount = investigatedLymphNodesCount,
            positiveLymphNodesCount = positiveLymphNodesCount
        )
    }

    fun metastasis(
        daysSinceDiagnosis: Int? = null,
        location: TumorLocation = TumorLocation.OTHER_POORLY_DEFINED_LOCALIZATIONS,
        isLinkedToProgression: Boolean? = null
    ): Metastasis {
        return Metastasis(
            daysSinceDiagnosis = daysSinceDiagnosis,
            location = location,
            isLinkedToProgression = isLinkedToProgression
        )
    }

    fun whoAssessment(daysSinceDiagnosis: Int = 0, whoStatus: Int = 0): WhoAssessment {
        return WhoAssessment(
            daysSinceDiagnosis = daysSinceDiagnosis,
            whoStatus = whoStatus
        )
    }

    fun asaAssessment(daysSinceDiagnosis: Int = 0, classification: AsaClassification = AsaClassification.I): AsaAssessment {
        return AsaAssessment(
            daysSinceDiagnosis = daysSinceDiagnosis,
            classification = classification
        )
    }

    fun labMeasurement(
        daysSinceDiagnosis: Int = 0, name: LabMeasure = LabMeasure.ALBUMINE, value: Double = 0.0,
        unit: Unit = LabMeasure.ALBUMINE.unit, isPreSurgical: Boolean? = null, isPostSurgical: Boolean? = null
    ): LabMeasurement {
        return LabMeasurement(
            daysSinceDiagnosis = daysSinceDiagnosis,
            name = name,
            value = value,
            unit = unit,
            isPreSurgical = isPreSurgical,
            isPostSurgical = isPostSurgical
        )
    }

    fun treatmentEpisode(
        metastaticPresence: MetastaticPresence = MetastaticPresence.ABSENT,
        reasonRefrainmentFromTreatment: ReasonRefrainmentFromTreatment = ReasonRefrainmentFromTreatment.NOT_APPLICABLE,
        gastroenterologyResections: List<GastroenterologyResection> = emptyList(),
        primarySurgeries: List<PrimarySurgery> = emptyList(),
        metastaticSurgeries: List<MetastaticSurgery> = emptyList(),
        hipecTreatments: List<HipecTreatment> = emptyList(),
        primaryRadiotherapies: List<PrimaryRadiotherapy> = emptyList(),
        metastaticRadiotherapies: List<MetastaticRadiotherapy> = emptyList(),
        systemicTreatments: List<SystemicTreatment> = emptyList(),
        responseMeasures: List<ResponseMeasure> = emptyList(),
        progressionMeasures: List<ProgressionMeasure> = emptyList()
    ): TreatmentEpisode {
        return TreatmentEpisode(
            metastaticPresence = metastaticPresence,
            reasonRefrainmentFromTreatment = reasonRefrainmentFromTreatment,
            gastroenterologyResections = gastroenterologyResections,
            primarySurgeries = primarySurgeries,
            metastaticSurgeries = metastaticSurgeries,
            hipecTreatments = hipecTreatments,
            primaryRadiotherapies = primaryRadiotherapies,
            metastaticRadiotherapies = metastaticRadiotherapies,
            systemicTreatments = systemicTreatments,
            responseMeasures = responseMeasures,
            progressionMeasures = progressionMeasures
        )
    }

    fun gastroenterologyResection(
        daysSinceDiagnosis: Int? = null,
        resectionType: GastroenterologyResectionType = GastroenterologyResectionType.ABLATION
    ): GastroenterologyResection {
        return GastroenterologyResection(daysSinceDiagnosis = daysSinceDiagnosis, resectionType = resectionType)
    }

    fun primarySurgery(
        daysSinceDiagnosis: Int? = null,
        type: SurgeryType = SurgeryType.TOTAL_COLECTOMY,
        technique: SurgeryTechnique? = null,
        urgency: SurgeryUrgency? = null,
        radicality: SurgeryRadicality? = null,
        circumferentialResectionMargin: CircumferentialResectionMargin? = null,
        anastomoticLeakageAfterSurgery: AnastomoticLeakageAfterSurgery? = null,
        hospitalizationDurationDays: Int? = null
    ): PrimarySurgery {
        return PrimarySurgery(
            daysSinceDiagnosis = daysSinceDiagnosis,
            type = type,
            technique = technique,
            urgency = urgency,
            radicality = radicality,
            circumferentialResectionMargin = circumferentialResectionMargin,
            anastomoticLeakageAfterSurgery = anastomoticLeakageAfterSurgery,
            hospitalizationDurationDays = hospitalizationDurationDays
        )
    }

    fun metastaticSurgery(
        daysSinceDiagnosis: Int? = null,
        type: MetastaticSurgeryType = MetastaticSurgeryType.METASTASECTOMY_PERITONEUM,
        radicality: SurgeryRadicality? = null
    ): MetastaticSurgery {
        return MetastaticSurgery(
            daysSinceDiagnosis = daysSinceDiagnosis,
            type = type,
            radicality = radicality
        )
    }

    fun hipecTreatment(daysSinceDiagnosis: Int = 0): HipecTreatment {
        return HipecTreatment(daysSinceDiagnosis = daysSinceDiagnosis)
    }

    fun primaryRadiotherapy(
        daysBetweenDiagnosisAndStart: Int? = null,
        daysBetweenDiagnosisAndStop: Int? = null,
        type: RadiotherapyType? = null,
        totalDosage: Double? = null
    ): PrimaryRadiotherapy {
        return PrimaryRadiotherapy(
            daysBetweenDiagnosisAndStart = daysBetweenDiagnosisAndStart,
            daysBetweenDiagnosisAndStop = daysBetweenDiagnosisAndStop,
            type = type,
            totalDosage = totalDosage
        )
    }

    fun metastaticRadiotherapy(
        daysBetweenDiagnosisAndStart: Int? = null,
        daysBetweenDiagnosisAndStop: Int? = null,
        type: MetastaticRadiotherapyType = MetastaticRadiotherapyType.RADIOTHERAPY_ON_BONE_METASTASES,
    ): MetastaticRadiotherapy {
        return MetastaticRadiotherapy(
            daysBetweenDiagnosisAndStart = daysBetweenDiagnosisAndStart,
            daysBetweenDiagnosisAndStop = daysBetweenDiagnosisAndStop,
            type = type
        )
    }

    fun systemicTreatment(
        treatment: Treatment = Treatment.OTHER,
        schemes: List<DrugScheme> = emptyList()
    ): SystemicTreatment {
        return SystemicTreatment(
            treatment = treatment,
            schemes = schemes
        )
    }

    fun drugTreatment(
        daysBetweenDiagnosisAndStart: Int? = null,
        daysBetweenDiagnosisAndStop: Int? = null,
        drug: Drug = Drug.CAPECITABINE,
        numberOfCycles: Int? = null,
        intent: TreatmentIntent? = null,
        drugTreatmentIsOngoing: Boolean? = null,
        isAdministeredPreSurgery: Boolean? = null,
        isAdministeredPostSurgery: Boolean? = null
    ): DrugTreatment {
        return DrugTreatment(
            daysBetweenDiagnosisAndStart = daysBetweenDiagnosisAndStart,
            daysBetweenDiagnosisAndStop = daysBetweenDiagnosisAndStop,
            drug = drug,
            numberOfCycles = numberOfCycles,
            intent = intent,
            drugTreatmentIsOngoing = drugTreatmentIsOngoing,
            isAdministeredPreSurgery = isAdministeredPreSurgery,
            isAdministeredPostSurgery = isAdministeredPostSurgery
        )
    }

    fun progressionMeasure(
        daysSinceDiagnosis: Int? = null,
        type: ProgressionMeasureType = ProgressionMeasureType.PROGRESSION,
        followUpEvent: ProgressionMeasureFollowUpEvent? = null
    ): ProgressionMeasure {
        return ProgressionMeasure(
            daysSinceDiagnosis = daysSinceDiagnosis,
            type = type,
            followUpEvent = followUpEvent
        )
    }
}