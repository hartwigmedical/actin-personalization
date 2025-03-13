package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureFollowUpEvent
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.outcome.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ResponseType
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
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatmentDrug
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatmentScheme
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentIntent
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
                systemicTreatments = listOf(expectedFollowup2SystemicTreatment()),
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

    private fun expectedFollowup2SystemicTreatment(): SystemicTreatment {
        return SystemicTreatment(
            daysBetweenDiagnosisAndStart = 100,
            daysBetweenDiagnosisAndStop = 780,
            treatment = Treatment.FOLFOXIRI_B,
            schemes = listOf(
                SystemicTreatmentScheme(
                    minDaysBetweenDiagnosisAndStart = 100,
                    maxDaysBetweenDiagnosisAndStart = 400,
                    minDaysBetweenDiagnosisAndStop = 180,
                    maxDaysBetweenDiagnosisAndStop = 480,
                    components = listOf(
                        SystemicTreatmentDrug(
                            daysBetweenDiagnosisAndStart = 100,
                            daysBetweenDiagnosisAndStop = 180,
                            drug = Drug.OXALIPLATIN,
                            numberOfCycles = 1,
                            intent = null,
                            drugTreatmentIsOngoing = false,
                            isAdministeredPreSurgery = true,
                            isAdministeredPostSurgery = false
                        ),
                        SystemicTreatmentDrug(
                            daysBetweenDiagnosisAndStart = 200,
                            daysBetweenDiagnosisAndStop = 280,
                            drug = Drug.BEVACIZUMAB,
                            numberOfCycles = 2,
                            intent = null,
                            drugTreatmentIsOngoing = false,
                            isAdministeredPreSurgery = false,
                            isAdministeredPostSurgery = true
                        ),
                        SystemicTreatmentDrug(
                            daysBetweenDiagnosisAndStart = 300,
                            daysBetweenDiagnosisAndStop = 380,
                            drug = Drug.IRINOTECAN,
                            numberOfCycles = 3,
                            intent = null,
                            drugTreatmentIsOngoing = false,
                            isAdministeredPreSurgery = true,
                            isAdministeredPostSurgery = true
                        ),
                        SystemicTreatmentDrug(
                            daysBetweenDiagnosisAndStart = 400,
                            daysBetweenDiagnosisAndStop = 480,
                            drug = Drug.FLUOROURACIL,
                            numberOfCycles = null,
                            intent = TreatmentIntent.SENSITIZER,
                            drugTreatmentIsOngoing = null,
                            isAdministeredPreSurgery = false,
                            isAdministeredPostSurgery = false
                        )
                    )
                ),
                SystemicTreatmentScheme(
                    minDaysBetweenDiagnosisAndStart = 500,
                    maxDaysBetweenDiagnosisAndStart = 600,
                    minDaysBetweenDiagnosisAndStop = 580,
                    maxDaysBetweenDiagnosisAndStop = 680,
                    components = listOf(
                        SystemicTreatmentDrug(
                            daysBetweenDiagnosisAndStart = 500,
                            daysBetweenDiagnosisAndStop = 580,
                            drug = Drug.CAPECITABINE,
                            numberOfCycles = null,
                            intent = TreatmentIntent.MAINTENANCE,
                            drugTreatmentIsOngoing = null,
                            isAdministeredPreSurgery = false,
                            isAdministeredPostSurgery = false
                        ),
                        SystemicTreatmentDrug(
                            daysBetweenDiagnosisAndStart = 600,
                            daysBetweenDiagnosisAndStop = 680,
                            drug = Drug.IRINOTECAN,
                            numberOfCycles = null,
                            intent = null,
                            drugTreatmentIsOngoing = true,
                            isAdministeredPreSurgery = null,
                            isAdministeredPostSurgery = null
                        )
                    )
                ),
                SystemicTreatmentScheme(
                    minDaysBetweenDiagnosisAndStart = 700,
                    maxDaysBetweenDiagnosisAndStart = 700,
                    minDaysBetweenDiagnosisAndStop = 780,
                    maxDaysBetweenDiagnosisAndStop = 780,
                    components = listOf(
                        SystemicTreatmentDrug(
                            daysBetweenDiagnosisAndStart = 700,
                            daysBetweenDiagnosisAndStop = 780,
                            drug = Drug.TEGAFUR_OR_GIMERACIL_OR_OTERACIL,
                            numberOfCycles = null,
                            intent = null,
                            drugTreatmentIsOngoing = null,
                            isAdministeredPreSurgery = false,
                            isAdministeredPostSurgery = false
                        )
                    )
                )
            )
        )
    }
}