package com.hartwig.actin.personalization.database.interpretation

import com.hartwig.actin.personalization.database.datamodel.ReferenceObject
import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.interpretation.AsaInterpreter
import com.hartwig.actin.personalization.interpretation.ComorbidityInterpreter
import com.hartwig.actin.personalization.interpretation.LabInterpreter
import com.hartwig.actin.personalization.interpretation.MetastaticInterpreter
import com.hartwig.actin.personalization.interpretation.MolecularInterpreter
import com.hartwig.actin.personalization.interpretation.TreatmentInterpreter
import com.hartwig.actin.personalization.interpretation.WhoInterpreter
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.roundToInt


object ReferenceObjectFactory {

    private val LOGGER = KotlinLogging.logger {}

    fun create(entry: ReferenceEntry): ReferenceObject? {
        val metastaticInterpreter = MetastaticInterpreter(entry.metastaticDiagnosis)
        val daysBetweenPrimaryAndMetastaticDiagnosis = metastaticInterpreter.daysBetweenPrimaryAndMetastaticDiagnosis()
        if (daysBetweenPrimaryAndMetastaticDiagnosis == null) {
            LOGGER.warn { "Could not determine interval towards metastatic diagnosis for entry with source ID ${entry.sourceId}" }
            return null
        }

        val treatmentInterpreter = TreatmentInterpreter(entry.treatmentEpisodes)
        if (!treatmentInterpreter.hasMetastaticTreatment()) {
            LOGGER.warn { "No metastatic-at-start treatment episode found for entry with source ID ${entry.sourceId}" }
            return null
        }
        val comorbidityInterpreter = ComorbidityInterpreter(entry.comorbidityAssessments)
        val whoInterpreter = WhoInterpreter(entry.whoAssessments)
        val asaInterpreter = AsaInterpreter(entry.asaAssessments)
        val labInterpreter = LabInterpreter(entry.labMeasurements)
        val molecularInterpreter = MolecularInterpreter(entry.molecularResults)

        val survivalSincePrimaryDiagnosis = entry.latestSurvivalMeasurement.daysSinceDiagnosis
        val daysBetweenPrimaryDiagnosisAndTreatmentStart = treatmentInterpreter.determineMetastaticSystemicTreatmentStart()
        val daysBetweenMetastaticDiagnosisAndTreatmentStart =
            daysBetweenPrimaryDiagnosisAndTreatmentStart?.let { daysBetweenPrimaryAndMetastaticDiagnosis - it }

        return ReferenceObject(
            source = entry.source,
            sourceId = entry.sourceId,
            diagnosisYear = entry.diagnosisYear,
            ageAtDiagnosis = entry.ageAtDiagnosis,
            ageAtMetastaticDiagnosis = entry.ageAtDiagnosis + ((daysBetweenPrimaryAndMetastaticDiagnosis/365.0).roundToInt()),
            sex = entry.sex,

            hadSurvivalEvent = !entry.latestSurvivalMeasurement.isAlive,
            survivalDaysSincePrimaryDiagnosis = survivalSincePrimaryDiagnosis,
            survivalDaysSinceMetastaticDiagnosis = survivalSincePrimaryDiagnosis - daysBetweenPrimaryAndMetastaticDiagnosis,
            survivalDaysSinceTreatmentStart = daysBetweenPrimaryDiagnosisAndTreatmentStart?.let { survivalSincePrimaryDiagnosis - it },

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

            charlsonComorbidityIndex = comorbidityInterpreter.mostRecentCharlsonComorbidityIndexPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasAids = comorbidityInterpreter.mostRecentHasAidsPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasCongestiveHeartFailure = comorbidityInterpreter.mostRecentHasCongestiveHeartFailurePriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasCollagenosis = comorbidityInterpreter.mostRecentHasCollagenosisPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasCopd = comorbidityInterpreter.mostRecentHasCopdPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasCerebrovascularDisease = comorbidityInterpreter.mostRecentHasCerebrovascularDiseasePriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasDementia = comorbidityInterpreter.mostRecentHasDementiaPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasDiabetesMellitus = comorbidityInterpreter.mostRecentHasDiabetesMellitusPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasDiabetesMellitusWithEndOrganDamage = comorbidityInterpreter.mostRecentHasDiabetesMellitusWithEndOrganDamagePriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasOtherMalignancy = comorbidityInterpreter.mostRecentHasOtherMalignancyPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasOtherMetastaticSolidTumor = comorbidityInterpreter.mostRecentHasOtherMetastaticSolidTumorPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasMyocardialInfarct = comorbidityInterpreter.mostRecentHasMyocardialInfarctPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasMildLiverDisease = comorbidityInterpreter.mostRecentHasMildLiverDiseasePriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasHemiplegiaOrParaplegia = comorbidityInterpreter.mostRecentHasHemiplegiaOrParaplegiaPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasPeripheralVascularDisease = comorbidityInterpreter.mostRecentHasPeripheralVascularDiseasePriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasRenalDisease = comorbidityInterpreter.mostRecentHasRenalDiseasePriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasLiverDisease = comorbidityInterpreter.mostRecentHasLiverDiseasePriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasUlcerDisease = comorbidityInterpreter.mostRecentHasUlcerDiseasePriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),

            daysBetweenPrimaryAndMetastaticDiagnosis = daysBetweenPrimaryAndMetastaticDiagnosis,
            isMetachronous = entry.metastaticDiagnosis.isMetachronous,
            hasLiverOrIntrahepaticBileDuctMetastases = metastaticInterpreter.hasLiverOrIntrahepaticBileDuctMetastases(),
            numberOfLiverMetastases = entry.metastaticDiagnosis.numberOfLiverMetastases,
            maximumSizeOfLiverMetastasisMm = entry.metastaticDiagnosis.maximumSizeOfLiverMetastasisMm,
            hasLymphNodeMetastases = metastaticInterpreter.hasLymphNodeMetastases(),
            investigatedLymphNodesCountMetastaticDiagnosis = entry.metastaticDiagnosis.investigatedLymphNodesCount,
            positiveLymphNodesCountMetastaticDiagnosis = entry.metastaticDiagnosis.positiveLymphNodesCount,
            hasPeritonealMetastases = metastaticInterpreter.hasPeritonealMetastases(),
            hasBronchusOrLungMetastases = metastaticInterpreter.hasBronchusOrLungMetastases(),
            hasBrainMetastases = metastaticInterpreter.hasBrainMetastases(),
            hasOtherMetastases = metastaticInterpreter.hasOtherMetastases(),

            whoAssessmentAtMetastaticDiagnosis = whoInterpreter.mostRecentStatusPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            asaAssessmentAtMetastaticDiagnosis = asaInterpreter.mostRecentClassificationPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            lactateDehydrogenaseAtMetastaticDiagnosis = labInterpreter.mostRecentLactateDehydrogenasePriorTo(
                daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            alkalinePhosphataseAtMetastaticDiagnosis = labInterpreter.mostRecentAlkalinePhosphatasePriorTo(
                daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            leukocytesAbsoluteAtMetastaticDiagnosis = labInterpreter.mostRecentLeukocytesAbsolutePriorTo(
                daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            carcinoembryonicAntigenAtMetastaticDiagnosis = labInterpreter.mostRecentCarcinoembryonicAntigenPriorTo(
                daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            albumineAtMetastaticDiagnosis = labInterpreter.mostRecentAlbuminePriorTo(
                daysBetweenPrimaryAndMetastaticDiagnosis
            ),
            neutrophilsAbsoluteAtMetastaticDiagnosis = labInterpreter.mostRecentNeutrophilsAbsolutePriorTo(
                daysBetweenPrimaryAndMetastaticDiagnosis
            ),

            hasMsi = molecularInterpreter.mostRecentHasMsiPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasBrafMutation = molecularInterpreter.mostRecentHasBrafMutationPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasBrafV600EMutation = molecularInterpreter.mostRecentHasBrafV600EMutationPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasRasMutation = molecularInterpreter.mostRecentHasRasMutationPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),
            hasKrasG12CMutation = molecularInterpreter.mostRecentHasKrasG12CMutationPriorTo(daysBetweenPrimaryAndMetastaticDiagnosis),

            hasHadPrimarySurgeryPriorToMetastaticDiagnosis = treatmentInterpreter.hasPrimarySurgeryPriorToMetastaticTreatment(),
            hasHadPrimarySurgeryAfterMetastaticDiagnosis = treatmentInterpreter.hasPrimarySurgeryDuringMetastaticTreatment(),
            hasHadGastroenterologySurgeryPriorToMetastaticDiagnosis = treatmentInterpreter.hasGastroenterologySurgeryPriorToMetastaticTreatment(),
            hasHadGastroenterologySurgeryAfterMetastaticDiagnosis = treatmentInterpreter.hasGastroenterologySurgeryDuringMetastaticTreatment(),
            hasHadHipecPriorToMetastaticDiagnosis = treatmentInterpreter.hasHipecPriorToMetastaticTreatment(),
            hasHadHipecAfterMetastaticDiagnosis = treatmentInterpreter.hasHipecDuringMetastaticTreatment(),
            hasHadPrimaryRadiotherapyPriorToMetastaticDiagnosis = treatmentInterpreter.hasPrimaryRadiotherapyPriorToMetastaticTreatment(),
            hasHadPrimaryRadiotherapyAfterMetastaticDiagnosis = treatmentInterpreter.hasPrimaryRadiotherapyDuringMetastaticTreatment(),

            hasHadMetastaticSurgery = treatmentInterpreter.hasMetastaticSurgery(),
            hasHadMetastaticRadiotherapy = treatmentInterpreter.hasMetastaticRadiotherapy(),

            hasHadSystemicTreatmentPriorToMetastaticDiagnosis = treatmentInterpreter.hasSystemicTreatmentPriorToMetastaticTreatment(),
            reasonRefrainmentFromTreatment = treatmentInterpreter.reasonRefrainmentFromTreatment(),
            daysBetweenMetastaticDiagnosisAndTreatmentStart = daysBetweenMetastaticDiagnosisAndTreatmentStart,
            systemicTreatmentsAfterMetastaticDiagnosis = treatmentInterpreter.metastaticSystemicTreatmentCount(),
            firstSystemicTreatmentAfterMetastaticDiagnosis = treatmentInterpreter.firstMetastaticSystemicTreatment()?.display,
            firstSystemicTreatmentDurationDays = treatmentInterpreter.firstMetastaticSystemicTreatmentDuration(),
            hadProgressionEvent = treatmentInterpreter.hasProgressionEventAfterMetastaticSystemicTreatmentStart(),
            daysBetweenTreatmentStartAndProgression = treatmentInterpreter.daysBetweenProgressionAndMetastaticSystemicTreatmentStart()
        )
    }
}