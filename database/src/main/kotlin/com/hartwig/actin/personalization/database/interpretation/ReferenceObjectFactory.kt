package com.hartwig.actin.personalization.database.interpretation

import com.hartwig.actin.personalization.database.datamodel.ReferenceObject
import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.interpretation.AsaAssessments
import com.hartwig.actin.personalization.interpretation.LabMeasurements
import com.hartwig.actin.personalization.interpretation.Metastases
import com.hartwig.actin.personalization.interpretation.Treatments
import com.hartwig.actin.personalization.interpretation.WhoAssessments
import com.hartwig.actin.personalization.selection.TreatmentSelection
import io.github.oshai.kotlinlogging.KotlinLogging

object ReferenceObjectFactory {

    private val LOGGER = KotlinLogging.logger {}

    fun create(entry: ReferenceEntry): ReferenceObject? {
        val daysBetweenPrimaryAndMetastaticDiagnosis =
            Metastases.daysBetweenPrimaryAndMetastaticDiagnosis(entry.metastaticDiagnosis)
        if (daysBetweenPrimaryAndMetastaticDiagnosis == null) {
            LOGGER.warn { "Could not determine interval towards metastatic diagnosis for entry with source ID ${entry.sourceId}" }
            return null
        }

        val survivalSincePrimaryDiagnosis = entry.latestSurvivalMeasurement.daysSinceDiagnosis
        val metastaticTreatmentEpisode = TreatmentSelection.extractMetastaticTreatmentEpisode(entry) ?: return null
        val systemicTreatmentStart = Treatments.determineSystemicTreatmentStart(metastaticTreatmentEpisode)

        return ReferenceObject(
            source = entry.source,
            sourceId = entry.sourceId,
            diagnosisYear = entry.diagnosisYear,
            ageAtDiagnosis = entry.ageAtDiagnosis,
            sex = entry.sex,

            hadSurvivalEvent = !entry.latestSurvivalMeasurement.isAlive,
            survivalDaysSincePrimaryDiagnosis = survivalSincePrimaryDiagnosis,
            survivalDaysSinceMetastaticDiagnosis = survivalSincePrimaryDiagnosis - daysBetweenPrimaryAndMetastaticDiagnosis,
            survivalDaysSinceTreatmentStart = systemicTreatmentStart?.let { survivalSincePrimaryDiagnosis - it },

            numberOfPriorTumors = entry.priorTumors.size,
            hasDoublePrimaryTumor = entry.primaryDiagnosis.hasDoublePrimaryTumor,

            basisOfDiagnosis = entry.primaryDiagnosis.basisOfDiagnosis,
            primaryTumorType = entry.primaryDiagnosis.primaryTumorType,
            primaryTumorLocation = entry.primaryDiagnosis.primaryTumorLocation,
            sidedness = entry.primaryDiagnosis.sidedness,
            anorectalVergeDistanceCategory = entry.primaryDiagnosis.anorectalVergeDistanceCategory,
            mesorectalFasciaIsClear = entry.primaryDiagnosis.mesorectalFasciaIsClear,
            distanceToMesorectalFasciaMm = entry.primaryDiagnosis.distanceToMesorectalFasciaMm,
            differentiationGrade = entry.primaryDiagnosis.differentiationGrade,
            clinicalTnmT = entry.primaryDiagnosis.clinicalTnmClassification.tnmT,
            clinicalTnmN = entry.primaryDiagnosis.clinicalTnmClassification.tnmN,
            clinicalTnmM = entry.primaryDiagnosis.clinicalTnmClassification.tnmM,
            pathologicalTnmT = entry.primaryDiagnosis.pathologicalTnmClassification.tnmT,
            pathologicalTnmN = entry.primaryDiagnosis.pathologicalTnmClassification.tnmN,
            pathologicalTnmM = entry.primaryDiagnosis.pathologicalTnmClassification.tnmM,
            clinicalTumorStage = entry.primaryDiagnosis.clinicalTumorStage,
            pathologicalTumorStage = entry.primaryDiagnosis.pathologicalTumorStage,
            investigatedLymphNodesCountPrimaryDiagnosis = entry.primaryDiagnosis.investigatedLymphNodesCount,
            positiveLymphNodesCountPrimaryDiagnosis = entry.primaryDiagnosis.positiveLymphNodesCount,
            presentedWithIleus = entry.primaryDiagnosis.presentedWithIleus,
            presentedWithPerforation = entry.primaryDiagnosis.presentedWithPerforation,
            venousInvasionDescription = entry.primaryDiagnosis.venousInvasionDescription,
            lymphaticInvasionCategory = entry.primaryDiagnosis.lymphaticInvasionCategory,
            extraMuralInvasionCategory = entry.primaryDiagnosis.extraMuralInvasionCategory,
            tumorRegression = entry.primaryDiagnosis.tumorRegression,

            daysBetweenPrimaryAndMetastaticDiagnosis = daysBetweenPrimaryAndMetastaticDiagnosis,
            hasLiverOrIntrahepaticBileDuctMetastases = Metastases.hasLiverOrIntrahepaticBileDuctMetastases(entry.metastaticDiagnosis),
            numberOfLiverMetastases = entry.metastaticDiagnosis.numberOfLiverMetastases,
            maximumSizeOfLiverMetastasisMm = entry.metastaticDiagnosis.maximumSizeOfLiverMetastasisMm,
            hasLymphNodeMetastases = Metastases.hasLymphNodeMetastases(entry.metastaticDiagnosis),
            investigatedLymphNodesCountMetastaticDiagnosis = entry.metastaticDiagnosis.investigatedLymphNodesCount,
            positiveLymphNodesCountMetastaticDiagnosis = entry.metastaticDiagnosis.positiveLymphNodesCount,
            hasPeritonealMetastases = Metastases.hasPeritonealMetastases(entry.metastaticDiagnosis),
            hasBronchusOrLungMetastases = Metastases.hasBronchusOrLungMetastases(entry.metastaticDiagnosis),
            hasBrainMetastases = Metastases.hasBrainMetastases(entry.metastaticDiagnosis),
            hasOtherMetastases = Metastases.hasOtherMetastases(entry.metastaticDiagnosis),

            whoAssessmentAtMetastaticDiagnosis = WhoAssessments.statusAtMetastaticDiagnosis(
                whoAssessments = entry.whoAssessments,
                daysBetweenPrimaryAndMetastaticDiagnosis = daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            asaAssessmentAtMetastaticDiagnosis = AsaAssessments.classificationAtMetastaticDiagnosis(
                asaAssessments = entry.asaAssessments,
                daysBetweenPrimaryAndMetastaticDiagnosis = daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            lactateDehydrogenaseAtMetastaticDiagnosis = LabMeasurements.lactateDehydrogenaseAtMetastaticDiagnosis(
                labMeasurements = entry.labMeasurements,
                daysBetweenPrimaryAndMetastaticDiagnosis = daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            alkalinePhosphataseAtMetastaticDiagnosis = LabMeasurements.alkalinePhosphataseAtMetastaticDiagnosis(
                labMeasurements = entry.labMeasurements,
                daysBetweenPrimaryAndMetastaticDiagnosis = daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            leukocytesAbsoluteAtMetastaticDiagnosis = LabMeasurements.leukocytesAbsoluteAtMetastaticDiagnosis(
                labMeasurements = entry.labMeasurements,
                daysBetweenPrimaryAndMetastaticDiagnosis = daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            carcinoembryonicAntigenAtMetastaticDiagnosis = LabMeasurements.carcinoembryonicAntigenAtMetastaticDiagnosis(
                labMeasurements = entry.labMeasurements,
                daysBetweenPrimaryAndMetastaticDiagnosis = daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            albumineAtMetastaticDiagnosis = LabMeasurements.albumineAtMetastaticDiagnosis(
                labMeasurements = entry.labMeasurements,
                daysBetweenPrimaryAndMetastaticDiagnosis = daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            neutrophilsAbsoluteAtMetastaticDiagnosis = LabMeasurements.neutrophilsAbsoluteAtMetastaticDiagnosis(
                labMeasurements = entry.labMeasurements,
                daysBetweenPrimaryAndMetastaticDiagnosis = daysBetweenPrimaryAndMetastaticDiagnosis
            ),

            hasHadPrimarySurgeryPriorToMetastaticDiagnosis = false,
            hasHadPrimarySurgeryAfterMetastaticDiagnosis = false,
            hasHadGastroenterologySurgeryPriorToMetastaticDiagnosis = false,
            hasHadGastroenterologySurgeryAfterMetastaticDiagnosis = false,
            hasHadHipecPriorToMetastaticDiagnosis = false,
            hasHadHipecAfterMetastaticDiagnosis = false,
            hasHadPrimaryRadiotherapyPriorToMetastaticDiagnosis = false,
            hasHadPrimaryRadiotherapyAfterMetastaticDiagnosis = false,

            hasHadMetastaticSurgery = false,
            hasHadMetastaticRadiotherapy = false,

            hasHadSystemicTreatmentPriorToMetastaticDiagnosis = false,
            daysBetweenMetastaticDiagnosisAndTreatmentStart = 0,
            systemicTreatmentAfterMetastaticDiagnosis = "",
            systemicTreatmentDurationDays = 0,
            systemicTreatmentDurationCycles = 0,
            hadProgressionEvent = false,
            daysBetweenTreatmentStartAndProgression = 0
        )
    }
}