package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.AsaClassificationPreSurgeryOrEndoscopy
import com.hartwig.actin.personalization.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.ExtraMuralInvasionCategory
import com.hartwig.actin.personalization.datamodel.LabMeasure
import com.hartwig.actin.personalization.datamodel.LabMeasureUnit
import com.hartwig.actin.personalization.datamodel.LabMeasurement
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.LymphaticInvasionCategory
import com.hartwig.actin.personalization.datamodel.Metastasis
import com.hartwig.actin.personalization.datamodel.NumberOfLiverMetastases
import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.datamodel.ReasonRefrainmentFromTumorDirectedTreatment
import com.hartwig.actin.personalization.datamodel.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.ResponseMeasureType
import com.hartwig.actin.personalization.datamodel.StageTnm
import com.hartwig.actin.personalization.datamodel.TnmM
import com.hartwig.actin.personalization.datamodel.TnmN
import com.hartwig.actin.personalization.datamodel.TnmT
import com.hartwig.actin.personalization.datamodel.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.TumorDifferentiationGrade
import com.hartwig.actin.personalization.datamodel.TumorRegression
import com.hartwig.actin.personalization.datamodel.VenousInvasionCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class NcrEpisodeExtractorTest {

    @Test
    fun `Should extract episode from NCR record`() {
        val expectedEpisode = Episode(
            id = EPISODE_ID,
            order = EPISODE_ORDER,
            whoStatusPreTreatmentStart = WHO_STATUS,
            asaClassificationPreSurgeryOrEndoscopy = AsaClassificationPreSurgeryOrEndoscopy.V,
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
            numberOfInvestigatedLymphNodes = INVESTIGATED_LYMPH_NODES,
            numberOfPositiveLymphNodes = POSITIVE_LYMPH_NODES,
            distantMetastasesStatus = DistantMetastasesStatus.AT_START,
            metastases = listOf(Metastasis(Location.ADRENAL_CORTEX, 20, true)),
            numberOfLiverMetastases = NumberOfLiverMetastases.FIVE_OR_MORE,
            maximumSizeOfLiverMetastasisInMm = 15,
            hasDoublePrimaryTumor = false,
            mesorectalFasciaIsClear = true,
            distanceToMesorectalFascia = null,
            venousInvasionCategory = VenousInvasionCategory.SUSPECT,
            lymphaticInvasionCategory = LymphaticInvasionCategory.NA,
            extraMuralInvasionCategory = ExtraMuralInvasionCategory.ABOVE_FIVE_MM,
            tumorRegression = TumorRegression.MINIMAL_REGRESSION,
            labMeasurements = listOf(
                LabMeasurement(LabMeasure.LACTATE_DEHYDROGENASE, 10.0, LabMeasureUnit.UNIT_PER_LITER, 1, null, null),
                LabMeasurement(LabMeasure.ALKALINE_PHOSPHATASE, 20.0, LabMeasureUnit.UNIT_PER_LITER, 2, null, null),
                LabMeasurement(LabMeasure.NEUTROPHILS_ABSOLUTE, 30.5, LabMeasureUnit.BILLIONS_PER_LITER, 3, null, null),
                LabMeasurement(LabMeasure.ALBUMINE, 40.5, LabMeasureUnit.GRAM_PER_LITER, 4, null, null),
                LabMeasurement(LabMeasure.LEUKOCYTES_ABSOLUTE, 50.5, LabMeasureUnit.BILLIONS_PER_LITER, 5, null, null),
                LabMeasurement(LabMeasure.CARCINOEMBRYONIC_ANTIGEN, 0.1, LabMeasureUnit.MICROGRAM_PER_LITER, null, true, false),
                LabMeasurement(LabMeasure.CARCINOEMBRYONIC_ANTIGEN, 0.2, LabMeasureUnit.MICROGRAM_PER_LITER, null, false, true)
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
            intervalTumorIncidenceHipecTreatment = null,
            hasHadPreSurgeryRadiotherapy = false,
            hasHadPostSurgeryRadiotherapy = false,
            hasHadPreSurgeryChemoRadiotherapy = false,
            hasHadPostSurgeryChemoRadiotherapy = false,
            hasHadPreSurgerySystemicChemotherapy = true,
            hasHadPostSurgerySystemicChemotherapy = false,
            hasHadPreSurgerySystemicTargetedTherapy = false,
            hasHadPostSurgerySystemicTargetedTherapy = true,
            responseMeasure = ResponseMeasure(ResponseMeasureType.PD, 5),
            systemicTreatmentPlan = null,
            pfsMeasures = listOf(
                PfsMeasure(PfsMeasureType.PROGRESSION, PfsMeasureFollowUpEvent.LOCAL_ONLY, 4),
                PfsMeasure(PfsMeasureType.DEATH, PfsMeasureFollowUpEvent.REGIONAL, 80),
            )
        )

        val episode = NcrEpisodeExtractor(NcrSystemicTreatmentPlanExtractor()).extractEpisode(NCR_RECORD, 80)
        assertThat(episode.systemicTreatmentPlan).isNotNull
        assertThat(episode.copy(systemicTreatmentPlan = null)).isEqualTo(expectedEpisode)
    }
}