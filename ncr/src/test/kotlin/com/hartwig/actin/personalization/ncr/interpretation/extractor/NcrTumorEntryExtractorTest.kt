package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.datamodel.ChronicityMetastases
import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.NumberOfCciCategories
import com.hartwig.actin.personalization.datamodel.PriorTumor
import com.hartwig.actin.personalization.datamodel.StageTnm
import com.hartwig.actin.personalization.datamodel.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.TumorType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrTumorEntryExtractorTest {

    private val diagnosisRecord = NCR_RECORD.copy(identification = NCR_IDENTIFICATION.copy(keyEid = 101, teller = 1, epis = "DIA"))
    private val followupRecord = NCR_RECORD

    @Test
    fun `Should extract diagnosis and episodes from NCR records`() {
        val records = listOf(diagnosisRecord, followupRecord)
        val (diagnosis, episodes) =
            NcrTumorEntryExtractor(NcrEpisodeExtractor(NcrSystemicTreatmentPlanExtractor())).extractTumorEntry(records)
        assertThat(episodes).hasSize(2)
        assertThat(diagnosis).isEqualTo(
            Diagnosis(
                consolidatedTumorType = TumorType.CRC_ADENOCARCINOMA,
                tumorLocations = setOf(Location.ASCENDING_COLON),
                hasHadTumorDirectedSystemicTherapy = false,
                ageAtDiagnosis = 50,
                observedOsFromTumorIncidenceDays = 80,
                hadSurvivalEvent = false,
                hasHadPriorTumor = true,
                priorTumors = listOf(
                    PriorTumor(
                        consolidatedTumorType = TumorType.MELANOMA,
                        tumorLocations = setOf(Location.SKIN_SHOULDER_ARM_HAND),
                        hasHadTumorDirectedSystemicTherapy = true,
                        intervalTumorIncidencePriorTumorDays = 20,
                        tumorPriorId = 1,
                        tumorLocationCategory = TumorLocationCategory.SKIN,
                        stageTNM = StageTnm.IIC,
                        systemicTreatments = listOf(Drug.EXTERNAL_RADIOTHERAPY_WITH_SENSITIZER)
                    )
                ),
                cci = 1,
                cciNumberOfCategories = NumberOfCciCategories.ONE_CATEGORY,
                cciHasAids = null,
                cciHasCongestiveHeartFailure = true,
                cciHasCollagenosis = null,
                cciHasCopd = false,
                cciHasCerebrovascularDisease = null,
                cciHasDementia = null,
                cciHasDiabetesMellitus = null,
                cciHasDiabetesMellitusWithEndOrganDamage = null,
                cciHasOtherMalignancy = null,
                cciHasOtherMetastaticSolidTumor = null,
                cciHasMyocardialInfarct = null,
                cciHasMildLiverDisease = null,
                cciHasHemiplegiaOrParaplegia = null,
                cciHasPeripheralVascularDisease = null,
                cciHasRenalDisease = null,
                cciHasLiverDisease = null,
                cciHasUlcerDisease = null,
                chronicityMetastases = ChronicityMetastases.METACHRONOUS,
                presentedWithIleus = false,
                presentedWithPerforation = true,
                anorectalVergeDistanceCategory = AnorectalVergeDistanceCategory.FIVE_TO_TEN_CM,
                hasMsi = true,
                hasBrafMutation = true,
                hasBrafV600EMutation = null,
                hasRasMutation = true,
                hasKrasG12CMutation = true
            )
        )
    }

    @Test
    fun `Should not determine chronicity in case of diagnosis episode only and unexpected stage`() {
        val records = listOf(diagnosisRecord)
        val (diagnosis) = NcrTumorEntryExtractor(NcrEpisodeExtractor(NcrSystemicTreatmentPlanExtractor())).extractTumorEntry(records)
        assertThat(diagnosis.chronicityMetastases == null)
    }

    @Test
    fun `Should resolve to synchronous chronicity in case diagnosis episode with expected stage`() {
        val diagnosisRecord = diagnosisRecord.copy(primaryDiagnosis = NCR_PRIMARY_DIAGNOSIS.copy(stadium = "4"))
        val records = listOf(diagnosisRecord)

        val (diagnosis) = NcrTumorEntryExtractor(NcrEpisodeExtractor(NcrSystemicTreatmentPlanExtractor())).extractTumorEntry(records)
        assertThat(diagnosis.chronicityMetastases == ChronicityMetastases.SYNCHRONOUS)
    }
}