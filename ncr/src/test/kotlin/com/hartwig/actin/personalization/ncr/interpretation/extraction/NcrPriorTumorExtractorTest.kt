package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorStage
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.treatment.Drug
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrPriorTumorExtractorTest {

    @Test
    fun `Should find no prior tumors on minimal NCR record`() {
        val priorTumors = NcrPriorTumorExtractor.extract(listOf(TestNcrRecordFactory.minimalDiagnosisRecord()))

        assertThat(priorTumors).isEmpty()
    }

    @Test
    fun `Should extract prior tumor from proper tumor NCR records`() {
        val priorTumors = NcrPriorTumorExtractor.extract(TestNcrRecordFactory.properEntryRecords())

        assertThat(priorTumors).containsExactly(
            PriorTumor(
                daysBeforeDiagnosis = 206,
                primaryTumorType = TumorType.MELANOMA,
                primaryTumorLocation = TumorLocation.SKIN_SHOULDER_ARM_HAND,
                primaryTumorLocationCategory = TumorLocationCategory.SKIN,
                primaryTumorStage = TumorStage.IIC,
                systemicDrugsReceived = listOf(Drug.SYSTEMIC_CHEMOTHERAPY, Drug.STUDY_MEDICATION_IMMUNOTHERAPY)
            )
        )
    }

    @Test
    fun `Should extract prior tumors from exhaustive NCR`() {
        val baseRecord = TestNcrRecordFactory.minimalDiagnosisRecord()
        val exhaustiveRecord = baseRecord.copy(
            priorMalignancies = baseRecord.priorMalignancies.copy(
                mal1Int = -10,
                mal2Int = -20,
                mal3Int = -30,
                mal4Int = -40,
                mal1TopoSublok = "C444",
                mal2TopoSublok = "C445",
                mal3TopoSublok = "C446",
                mal4TopoSublok = "C447",
                mal1Morf = 8240,
                mal2Morf = 8240,
                mal3Morf = 8240,
                mal4Morf = 8240,
                mal1Tumsoort = 300000,
                mal2Tumsoort = 300000,
                mal3Tumsoort = 300000,
                mal4Tumsoort = 300000,
                mal1Syst = 0,
                mal2Syst = 0,
                mal3Syst = 0,
                mal4Syst = 0,
            )
        )

        val priorTumors = NcrPriorTumorExtractor.extract(listOf(exhaustiveRecord))

        assertThat(priorTumors).hasSize(4)
    }
}

