package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrQualityFilterTest {

    private val filter = NcrQualityFilter(logFilteredRecords = true)

    @Test
    fun `Should select properly from a set of multiple patients`() {
        val valid = TestNcrRecordFactory.minimalDiagnosisRecord()
        val validFollowup = TestNcrRecordFactory.minimalFollowupRecord()
        val invalid = valid.copy(treatment = valid.treatment.copy(tumgerichtTher = 1))

        val record1Patient1 = valid.copy(identification = valid.identification.copy(keyNkr = 1, keyZid = 1, keyEid = 1))
        val record2Patient1 = validFollowup.copy(identification = validFollowup.identification.copy(keyNkr = 1, keyZid = 1, keyEid = 2))

        val record1Patient2 = valid.copy(identification = valid.identification.copy(keyNkr = 2, keyZid = 1, keyEid = 1))
        val record2Patient2 = valid.copy(identification = valid.identification.copy(keyNkr = 2, keyZid = 2, keyEid = 1))
        val record3Patient2 = invalid.copy(identification = valid.identification.copy(keyNkr = 2, keyZid = 2, keyEid = 2))
        val record4Patient2 = invalid.copy(identification = valid.identification.copy(keyNkr = 2, keyZid = 3, keyEid = 1))

        val record1Patient3 = invalid.copy(identification = valid.identification.copy(keyNkr = 3, keyZid = 1, keyEid = 1))

        val filtered = filter.run(
            listOf(
                record1Patient1,
                record2Patient1,
                record1Patient2,
                record2Patient2,
                record3Patient2,
                record4Patient2,
                record1Patient3
            )
        )
        
        assertThat(filtered).containsExactly(record1Patient1, record2Patient1, record1Patient2)
    }

    @Test
    fun `Should select entries with valid treatment data`() {
        val baseRecord = TestNcrRecordFactory.minimalDiagnosisRecord()

        val noTreatmentButFlaggedAsHavingOne = baseRecord.copy(
            treatment = baseRecord.treatment.copy(
                tumgerichtTher = 1,
                systemicTreatment = baseRecord.treatment.systemicTreatment.copy(chemo = 0, target = 0),
                primarySurgery = baseRecord.treatment.primarySurgery.copy(chir = null),
                metastaticSurgery = baseRecord.treatment.metastaticSurgery.copy(metaChirInt1 = null),
                primaryRadiotherapy = baseRecord.treatment.primaryRadiotherapy.copy(rt = 0, chemort = 0),
                metastaticRadiotherapy = baseRecord.treatment.metastaticRadiotherapy.copy(metaRtCode1 = null),
                gastroenterologyResection = baseRecord.treatment.gastroenterologyResection.copy(mdlRes = null),
                hipec = baseRecord.treatment.hipec.copy(hipec = null)
            )
        )

        assertThat(filter.run(listOf(noTreatmentButFlaggedAsHavingOne))).isEmpty()

        val missingTumorTreatmentFlag = noTreatmentButFlaggedAsHavingOne.copy(
            treatment = noTreatmentButFlaggedAsHavingOne.treatment.copy(tumgerichtTher = null)
        )
        assertThat(filter.run(listOf(missingTumorTreatmentFlag))).containsExactly(missingTumorTreatmentFlag)

        val recordWithValidTreatmentData = noTreatmentButFlaggedAsHavingOne.copy(
            treatment = noTreatmentButFlaggedAsHavingOne.treatment.copy(
                tumgerichtTher = 1,
                geenTherReden = null,
                systemicTreatment = noTreatmentButFlaggedAsHavingOne.treatment.systemicTreatment.copy(target = 1, chemo = 1, systCode1 = "code1")
            )
        )
        assertThat(filter.run(listOf(recordWithValidTreatmentData))).containsExactly(recordWithValidTreatmentData)
    }
}