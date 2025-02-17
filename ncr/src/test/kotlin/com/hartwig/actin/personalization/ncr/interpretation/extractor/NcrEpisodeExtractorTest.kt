package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasure
import com.hartwig.actin.personalization.datamodel.assessment.Unit
import com.hartwig.actin.personalization.datamodel.diagnosis.ExtraMuralInvasionCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.LymphaticInvasionCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.diagnosis.NumberOfLiverMetastases
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmN
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmT
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorDifferentiationGrade
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorRegression
import com.hartwig.actin.personalization.datamodel.diagnosis.VenousInvasionDescription
import com.hartwig.actin.personalization.datamodel.old.Episode
import com.hartwig.actin.personalization.datamodel.old.LabMeasurement
import com.hartwig.actin.personalization.datamodel.old.Metastasis
import com.hartwig.actin.personalization.datamodel.old.PfsMeasure
import com.hartwig.actin.personalization.datamodel.old.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.outcome.ResponseType
import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTumorDirectedTreatment
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrEpisodeExtractorTest {

    private val baseRecord = TestNcrRecordFactory.properDiagnosisRecord()
    private val treatmentRecord = baseRecord.copy(
        treatment = baseRecord.treatment.copy(
            systemicTreatment = baseRecord.treatment.systemicTreatment.copy(
                systStartInt1 = 721,
                systStartInt2 = 722,
                systStartInt3 = 723,
                systStartInt4 = 724,
                systStartInt5 = 725,
                systStartInt6 = 726,
                systStartInt7 = 727
            )
        )
    )

    @Test
    fun `Should extract episode from NCR record`() {
        val episode = NcrEpisodeExtractor(NcrSystemicTreatmentPlanExtractor()).extractEpisode(treatmentRecord, 80)
        assertThat(episode.systemicTreatmentPlan).isNotNull
        assertThat(episode.copy(systemicTreatmentPlan = null)).isEqualTo(expectedEpisode)
    }

    @Test
    fun `Should filter out invalid lab measurements (9999, null, or out of extreme ranges)`() {
        val modifiedNcrRecord = treatmentRecord.copy(
            labValues = treatmentRecord.labValues.copy(
                ldh1 = null,
                ldh2 = 9999,
                ldh3 = 5000,
                albumine1 = 9999.0,
                albumine2 = 90.0,
                albumine3 = 40.5,
                neutro1 = 500.0,
                neutro2 = 30.5,
                leuko1 = 50.5,
                prechirCea = 9999.0,
                postchirCea = null
            )
        )

        val expectedEpisodeInvalidLabMeasurements = expectedEpisode.copy(
            labMeasurements = listOf(
                LabMeasurement(LabMeasure.ALKALINE_PHOSPHATASE, 20.0, Unit.UNIT_PER_LITER, 2, null, null),
                LabMeasurement(LabMeasure.NEUTROPHILS_ABSOLUTE, 30.5, Unit.BILLIONS_PER_LITER, null, null, null),
                LabMeasurement(LabMeasure.ALBUMINE, 40.5, Unit.GRAM_PER_LITER, null, null, null),
                LabMeasurement(LabMeasure.LEUKOCYTES_ABSOLUTE, 50.5, Unit.BILLIONS_PER_LITER, 5, null, null)
            )
        )

        val episode = NcrEpisodeExtractor(NcrSystemicTreatmentPlanExtractor()).extractEpisode(modifiedNcrRecord, 80)

        assertThat(episode.systemicTreatmentPlan).isNotNull
        assertThat(episode.copy(systemicTreatmentPlan = null)).isEqualTo(expectedEpisodeInvalidLabMeasurements)
    }


    @Test
    fun `Should set maximumSizeOfLiverMetastasisMm to null when value is 999`() {
        val modifiedNcrRecord = treatmentRecord.copy(
            metastaticDiagnosis = treatmentRecord.metastaticDiagnosis.copy(
                metaLeverAfm = 999
            )
        )

        val expectedEpisodeWithNullMetastasisSize = expectedEpisode.copy(
            maximumSizeOfLiverMetastasisMm = null
        )

        val episode = NcrEpisodeExtractor(NcrSystemicTreatmentPlanExtractor()).extractEpisode(modifiedNcrRecord, 80)

        assertThat(episode.systemicTreatmentPlan).isNotNull
        assertThat(episode.copy(systemicTreatmentPlan = null)).isEqualTo(expectedEpisodeWithNullMetastasisSize)
    }

    companion object {
        private val expectedEpisode = Episode(
            id = 123,
            order = 2,
            whoStatusPreTreatmentStart = 1,
            asaClassificationPreSurgeryOrEndoscopy = AsaClassification.V,
            tumorIncidenceYear = 2020,
            tumorLocation = TumorLocation.ASCENDING_COLON,
            tumorBasisOfDiagnosis = TumorBasisOfDiagnosis.SPEC_BIOCHEMICAL_IMMUNOLOGICAL_LAB_INVESTIGATION,
            tumorDifferentiationGrade = TumorDifferentiationGrade.GRADE_2_OR_MODERATELY_DIFFERENTIATED,
            tnmCT = TnmT.T0,
            tnmCN = TnmN.N1A,
            tnmCM = TnmM.M_MINUS,
            tnmPT = TnmT.T4A,
            tnmPN = TnmN.X,
            tnmPM = null,
//            stageCTNM = StageTnm.NA,
//            stagePTNM = StageTnm.M,
//            stageTNM = StageTnm.IIC,
            investigatedLymphNodesNumber = 3,
            positiveLymphNodesNumber = 1,
            distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_PROGRESSION,
            metastases = listOf(Metastasis(TumorLocation.ADRENAL_CORTEX, 20, true)),
            numberOfLiverMetastases = NumberOfLiverMetastases.FIVE_OR_MORE,
            maximumSizeOfLiverMetastasisMm = 15,
            hasDoublePrimaryTumor = false,
            mesorectalFasciaIsClear = true,
            distanceToMesorectalFasciaMm = null,
            venousInvasionDescription = VenousInvasionDescription.SUSPECT,
            lymphaticInvasionCategory = LymphaticInvasionCategory.NA,
            extraMuralInvasionCategory = ExtraMuralInvasionCategory.ABOVE_FIVE_MM,
            tumorRegression = TumorRegression.MINIMAL_REGRESSION,
            labMeasurements = listOf(
                LabMeasurement(LabMeasure.LACTATE_DEHYDROGENASE, 10.0, Unit.UNIT_PER_LITER, 1, null, null),
                LabMeasurement(LabMeasure.ALKALINE_PHOSPHATASE, 20.0, Unit.UNIT_PER_LITER, 2, null, null),
                LabMeasurement(LabMeasure.NEUTROPHILS_ABSOLUTE, 30.5, Unit.BILLIONS_PER_LITER, 3, null, null),
                LabMeasurement(LabMeasure.ALBUMINE, 40.5, Unit.GRAM_PER_LITER, 4, null, null),
                LabMeasurement(LabMeasure.LEUKOCYTES_ABSOLUTE, 50.5, Unit.BILLIONS_PER_LITER, 5, null, null),
                LabMeasurement(LabMeasure.CARCINOEMBRYONIC_ANTIGEN, 0.1, Unit.MICROGRAM_PER_LITER, null, true, false),
                LabMeasurement(LabMeasure.CARCINOEMBRYONIC_ANTIGEN, 0.2, Unit.MICROGRAM_PER_LITER, null, false, true)
            ),
            hasReceivedTumorDirectedTreatment = false,
            reasonRefrainmentFromTumorDirectedTreatment =
            ReasonRefrainmentFromTumorDirectedTreatment.EXPECTED_FAST_PROGRESSION_OR_HIGH_TUMOR_LOAD,
            hasParticipatedInTrial = true,
            gastroenterologyResections = emptyList(),
            surgeries = emptyList(),
            metastasesSurgeries = emptyList(),
            radiotherapies = emptyList(),
            metastasesRadiotherapies = emptyList(),
            hasHadHipecTreatment = false,
            intervalTumorIncidenceHipecTreatmentDays = null,
            hasHadPreSurgeryRadiotherapy = false,
            hasHadPostSurgeryRadiotherapy = false,
            hasHadPreSurgeryChemoRadiotherapy = false,
            hasHadPostSurgeryChemoRadiotherapy = false,
            hasHadPreSurgerySystemicChemotherapy = true,
            hasHadPostSurgerySystemicChemotherapy = false,
            hasHadPreSurgerySystemicTargetedTherapy = false,
            hasHadPostSurgerySystemicTargetedTherapy = true,
            responseMeasure = ResponseMeasure(ResponseType.PD, 5),
            systemicTreatmentPlan = null,
            pfsMeasures = listOf(
                PfsMeasure(ProgressionMeasureType.PROGRESSION, ProgressionMeasureFollowUpEvent.LOCAL_ONLY, 4),
                PfsMeasure(ProgressionMeasureType.DEATH, ProgressionMeasureFollowUpEvent.REGIONAL, 80),
            ),
            ageAtTreatmentPlanStart = 52
        )
    }

}