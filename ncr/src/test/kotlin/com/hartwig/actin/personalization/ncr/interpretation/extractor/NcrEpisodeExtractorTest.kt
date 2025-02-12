package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.LabMeasurement
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.Metastasis
import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.datamodel.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.StageTnm
import com.hartwig.actin.personalization.datamodel.v2.assessment.AsaClassification
import com.hartwig.actin.personalization.datamodel.v2.assessment.LabMeasure
import com.hartwig.actin.personalization.datamodel.v2.assessment.Unit
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.ExtraMuralInvasionCategory
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.LymphaticInvasionCategory
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.NumberOfLiverMetastases
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.TnmN
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.TnmT
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.TumorDifferentiationGrade
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.TumorRegression
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.VenousInvasionDescription
import com.hartwig.actin.personalization.datamodel.v2.outcome.ProgressionMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.v2.outcome.ResponseType
import com.hartwig.actin.personalization.datamodel.v2.treatment.ReasonRefrainmentFromTumorDirectedTreatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class NcrEpisodeExtractorTest {
    private val treatmentRecord = NCR_RECORD.copy(
        treatment = NCR_RECORD.treatment.copy(systemicTreatment = NCR_SYSTEMIC_TREATMENT.copy(systStartInt1 = 721, systStartInt2 = 722, systStartInt3 = 723, systStartInt4 = 724, systStartInt5 = 725, systStartInt6 = 726, systStartInt7 = 727))
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
            labValues = NCR_LAB_VALUES.copy(
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
            metastaticDiagnosis = NCR_METASTATIC_DIAGNOSIS.copy(
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
            id = EPISODE_ID,
            order = EPISODE_ORDER,
            whoStatusPreTreatmentStart = WHO_STATUS,
            asaClassificationPreSurgeryOrEndoscopy = AsaClassification.V,
            tumorIncidenceYear = INCIDENCE_YEAR,
            tumorLocation = Location.ASCENDING_COLON,
            tumorBasisOfDiagnosis = TumorBasisOfDiagnosis.SPEC_BIOCHEMICAL_IMMUNOLOGICAL_LAB_INVESTIGATION,
            tumorDifferentiationGrade = TumorDifferentiationGrade.GRADE_2_OR_MODERATELY_DIFFERENTIATED,
            tnmCT = TnmT.T0,
            tnmCN = TnmN.N1A,
            tnmCM = TnmM.M_MINUS,
            tnmPT = TnmT.T4A,
            tnmPN = TnmN.X,
            tnmPM = null,
            stageCTNM = StageTnm.NA,
            stagePTNM = StageTnm.M,
            stageTNM = StageTnm.IIC,
            investigatedLymphNodesNumber = INVESTIGATED_LYMPH_NODES,
            positiveLymphNodesNumber = POSITIVE_LYMPH_NODES,
            distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_PROGRESSION,
            metastases = listOf(Metastasis(Location.ADRENAL_CORTEX, 20, true)),
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
                PfsMeasure(PfsMeasureType.PROGRESSION, ProgressionMeasureFollowUpEvent.LOCAL_ONLY, 4),
                PfsMeasure(PfsMeasureType.DEATH, ProgressionMeasureFollowUpEvent.REGIONAL, 80),
            ),
            ageAtTreatmentPlanStart = 52
        )
    }

}