package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.outcome.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ResponseType
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
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrTreatmentEpisodeExtractorTest {

    @Test
    fun `Should extract treatment episodes from minimal NCR record`() {
        val treatmentEpisodes = NcrTreatmentEpisodeExtractor.extract(listOf(TestNcrRecordFactory.minimalDiagnosisRecord()))

        assertThat(treatmentEpisodes).hasSize(1)
        assertThat(treatmentEpisodes[0].reasonRefrainmentFromTreatment)
            .isEqualTo(ReasonRefrainmentFromTreatment.LIMITED_TUMOR_LOAD_OR_FEW_COMPLAINTS)
    }

    @Test
    fun `Should extract treatment episodes from proper NCR record`() {
        val treatmentEpisodes = NcrTreatmentEpisodeExtractor.extract(TestNcrRecordFactory.properTumorRecords())

        assertThat(treatmentEpisodes).isEqualTo(expectedTreatmentEpisodes())
    }

    private fun expectedTreatmentEpisodes(): List<TreatmentEpisode> {
        return listOf(
            emptyTreatmentEpisode().copy(
                metastaticPresence = MetastaticPresence.ABSENT,
                gastroenterologyResections = listOf(
                    GastroenterologyResection(
                        daysSinceDiagnosis = 10,
                        resectionType = GastroenterologyResectionType.ENDOSCOPIC_INTERMUSCULAR_DISSECTION
                    )
                ),
                primarySurgeries = listOf(
                    PrimarySurgery(
                        daysSinceDiagnosis = 20,
                        type = SurgeryType.SIGMOID_RESECTION,
                        technique = null,
                        urgency = null,
                        radicality = null,
                        circumferentialResectionMargin = null,
                        anastomoticLeakageAfterSurgery = null,
                        hospitalizationDurationDays = null
                    )
                ),
                primaryRadiotherapies = listOf(
                    PrimaryRadiotherapy(
                        daysBetweenDiagnosisAndStart = 24,
                        daysBetweenDiagnosisAndStop = 28,
                        type = RadiotherapyType.SHORT_DURATION,
                        totalDosage = 5.0
                    )
                )
            ),
            emptyTreatmentEpisode().copy(
                metastaticPresence = MetastaticPresence.ABSENT,
                hipecTreatments = listOf(HipecTreatment(daysSinceDiagnosis = 50))
            ),
            emptyTreatmentEpisode().copy(
                metastaticPresence = MetastaticPresence.AT_START,
                metastaticSurgeries = listOf(
                    MetastaticSurgery(
                        daysSinceDiagnosis = 110,
                        type = MetastaticSurgeryType.METASTASECTOMY_PERITONEUM,
                        radicality = SurgeryRadicality.MICROSCOPIC_IRRADICAL
                    )
                ),
                metastaticRadiotherapies = listOf(
                    MetastaticRadiotherapy(
                        daysBetweenDiagnosisAndStart = 120,
                        daysBetweenDiagnosisAndStop = 130,
                        type = MetastaticRadiotherapyType.RADIOTHERAPY_ON_LUNG_METASTASES
                    )
                ),
                responseMeasures = listOf(ResponseMeasure(daysSinceDiagnosis = 5, response = ResponseType.PD)),
                progressionMeasures = listOf(
                    ProgressionMeasure(
                        daysSinceDiagnosis = 400,
                        type = ProgressionMeasureType.PROGRESSION,
                        followUpEvent = ProgressionMeasureFollowUpEvent.LOCAL_ONLY
                    ),
                    ProgressionMeasure(
                        daysSinceDiagnosis = 850,
                        type = ProgressionMeasureType.DEATH,
                        followUpEvent = ProgressionMeasureFollowUpEvent.REGIONAL
                    )
                )
            )
        )
    }

    private fun emptyTreatmentEpisode(): TreatmentEpisode {
        return TreatmentEpisode(
            metastaticPresence = MetastaticPresence.ABSENT,
            reasonRefrainmentFromTreatment = ReasonRefrainmentFromTreatment.NOT_APPLICABLE,
            gastroenterologyResections = emptyList(),
            primarySurgeries = emptyList(),
            metastaticSurgeries = emptyList(),
            hipecTreatments = emptyList(),
            primaryRadiotherapies = emptyList(),
            metastaticRadiotherapies = emptyList(),
            systemicTreatments = emptyList(),
            responseMeasures = emptyList(),
            progressionMeasures = emptyList()
        )
    }
}