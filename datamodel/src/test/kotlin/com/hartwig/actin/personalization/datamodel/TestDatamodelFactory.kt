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
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasurement
import com.hartwig.actin.personalization.datamodel.treatment.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.treatment.HipecTreatment
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticSurgery
import com.hartwig.actin.personalization.datamodel.treatment.PrimaryRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.PrimarySurgery
import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode

object TestDatamodelFactory {

    fun entry(
        ageAtDiagnosis: Int = 75,
        isAlive: Boolean = false,
        daysBetweenDiagnosisAndSurvivalMeasurement: Int = 100,
        metastaticPresenceUnderSystemicTreatment: MetastaticPresence = MetastaticPresence.AT_START,
        systemicTreatment: Treatment? = null,
        systemicTreatmentStart: Int? = null,
        primarySurgeryType: SurgeryType? = null,
        daysBetweenDiagnosisAndProgression: Int? = null,
        hasProgressionEvent: Boolean? = null
    ): ReferenceEntry {
        val treatmentEpisode = treatmentEpisode(
            metastaticPresence = metastaticPresenceUnderSystemicTreatment,
            systemicTreatment = systemicTreatment,
            daysBetweenDiagnosisAndSystemicTreatmentStart = systemicTreatmentStart,
            primarySurgeryType = primarySurgeryType,
            daysBetweenDiagnosisAndProgression = daysBetweenDiagnosisAndProgression,
            hasProgressionEvent = hasProgressionEvent
        )

        return entry(ageAtDiagnosis, isAlive, daysBetweenDiagnosisAndSurvivalMeasurement, treatmentEpisode)
    }

    fun entry(
        ageAtDiagnosis: Int = 75,
        isAlive: Boolean = true,
        daysBetweenDiagnosisAndSurvivalMeasurement: Int = 100,
        treatmentEpisode: TreatmentEpisode? = null
    ): ReferenceEntry {
        return TestReferenceEntryFactory.minimalReferenceEntry().copy(
            ageAtDiagnosis = ageAtDiagnosis,
            latestSurvivalMeasurement = SurvivalMeasurement(
                daysSinceDiagnosis = daysBetweenDiagnosisAndSurvivalMeasurement,
                isAlive = isAlive
            ),
            treatmentEpisodes = treatmentEpisode?.let { listOf(it) } ?: emptyList()
        )
    }

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

    fun treatmentEpisode(
        metastaticPresence: MetastaticPresence = MetastaticPresence.AT_START,
        systemicTreatment: Treatment? = null,
        daysBetweenDiagnosisAndSystemicTreatmentStart: Int? = null,
        primarySurgeryType: SurgeryType? = null,
        hasProgressionEvent: Boolean? = null,
        daysBetweenDiagnosisAndProgression: Int? = null
    ): TreatmentEpisode {
        val primarySurgeries = primarySurgeryType?.let {
            listOf(
                PrimarySurgery(
                    daysSinceDiagnosis = null,
                    type = it,
                    technique = null,
                    urgency = null,
                    radicality = null,
                    circumferentialResectionMargin = null,
                    anastomoticLeakageAfterSurgery = null,
                    hospitalizationDurationDays = null
                )
            )
        } ?: emptyList()

        val systemicTreatments = systemicTreatment?.let {
            listOf(
                SystemicTreatment(
                    daysBetweenDiagnosisAndStart = daysBetweenDiagnosisAndSystemicTreatmentStart,
                    daysBetweenDiagnosisAndStop = null,
                    treatment = systemicTreatment,
                    schemes = emptyList()
                )
            )
        } ?: emptyList()

        val progressionMeasures = hasProgressionEvent?.let { hasEvent ->
            if (hasEvent) listOf(progressionMeasure(daysSinceDiagnosis = daysBetweenDiagnosisAndProgression)) else emptyList()
        } ?: emptyList()

        return TreatmentEpisode(
            metastaticPresence = metastaticPresence,
            reasonRefrainmentFromTreatment = ReasonRefrainmentFromTreatment.NOT_APPLICABLE,
            gastroenterologyResections = emptyList(),
            primarySurgeries = primarySurgeries,
            metastaticSurgeries = emptyList(),
            hipecTreatments = emptyList(),
            primaryRadiotherapies = emptyList(),
            metastaticRadiotherapies = emptyList(),
            systemicTreatments = systemicTreatments,
            responseMeasures = emptyList(),
            progressionMeasures = progressionMeasures
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