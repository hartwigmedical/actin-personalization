package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetastaticDiagnosisRecordFilterTest {
    private val filter = MetastaticDiagnosisRecordFilter(true)

    @Test
    fun `Should return true for one or zero metastatic detection`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            identification = TestNcrRecordFactory.minimalDiagnosisRecord().identification.copy(metaEpis = 1)
        ))
        assertThat(filter.hasOnlyOneMetastaticDetection(records)).isTrue()
    }

    @Test
    fun `Should return false for more than one metastatic detection`() {
        val records = listOf(
            TestNcrRecordFactory.minimalDiagnosisRecord().copy(identification = TestNcrRecordFactory.minimalDiagnosisRecord().identification.copy(metaEpis = 1)),
            TestNcrRecordFactory.minimalDiagnosisRecord().copy(identification = TestNcrRecordFactory.minimalDiagnosisRecord().identification.copy(metaEpis = 2))
        )
        assertThat(filter.hasOnlyOneMetastaticDetection(records)).isFalse()
    }

    @Test
    fun `Should return true when metastatic fields are empty if detection not present`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            identification = TestNcrRecordFactory.minimalDiagnosisRecord().identification.copy(metaEpis = 0),
            metastaticDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord().metastaticDiagnosis.copy(),
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                metastaticSurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.metastaticSurgery.copy(),
                metastaticRadiotherapy = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.metastaticRadiotherapy.copy()
            )
        ))
        assertThat(filter.hasEmptyMetastaticFieldIfDetectionNotPresent(records)).isTrue()
    }

    @Test
    fun `Should return false when metastatic fields are not empty if detection not present`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            identification = TestNcrRecordFactory.minimalDiagnosisRecord().identification.copy(metaEpis = 0),
            metastaticDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord().metastaticDiagnosis.copy(metaProg1 = 1),
            treatment = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.copy(
                metastaticSurgery = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.metastaticSurgery.copy(metaChirCode1 = "surgery"),
                metastaticRadiotherapy = TestNcrRecordFactory.minimalDiagnosisRecord().treatment.metastaticRadiotherapy.copy(metaRtCode1 = "radiotherapy")
            )
        ))
        assertThat(filter.hasEmptyMetastaticFieldIfDetectionNotPresent(records)).isFalse()
    }

    @Test
    fun `Should return true for consistent progression`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            identification = TestNcrRecordFactory.minimalDiagnosisRecord().identification.copy(metaEpis = 1),
            metastaticDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord().metastaticDiagnosis.copy(
                metaProg1 = 0, metaProg2 = 0, metaProg3 = 0, metaProg4 = 0, metaProg5 = 0, metaProg6 = 0, metaProg7 = 0, metaProg8 = 0, metaProg9 = 0, metaProg10 = 0
            )
        ))
        assertThat(filter.hasConsistentMetastaticProgression(records)).isTrue()
    }

    @Test
    fun `Should return false for inconsistent progression with at_progression`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            identification = TestNcrRecordFactory.minimalDiagnosisRecord().identification.copy(metaEpis = 2),
            metastaticDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord().metastaticDiagnosis.copy(
                metaProg1 = 0, metaProg2 = 0, metaProg3 = 0, metaProg4 = 0, metaProg5 = 0, metaProg6 = 0, metaProg7 = 0, metaProg8 = 0, metaProg9 = 0, metaProg10 = 0
            )
        ))
        assertThat(filter.hasConsistentMetastaticProgression(records)).isFalse()
    }
    
    @Test
    fun `Should return true for inconsistent progression with at_progression`() {
        val records = listOf(TestNcrRecordFactory.minimalDiagnosisRecord().copy(
            identification = TestNcrRecordFactory.minimalDiagnosisRecord().identification.copy(metaEpis = 2),
            metastaticDiagnosis = TestNcrRecordFactory.minimalDiagnosisRecord().metastaticDiagnosis.copy(
                metaProg1 = 1, metaProg2 = 0, metaProg3 = 0, metaProg4 = 0, metaProg5 = 0, metaProg6 = 0, metaProg7 = 0, metaProg8 = 0, metaProg9 = 0, metaProg10 = 0
            )
        ))
        assertThat(filter.hasConsistentMetastaticProgression(records)).isTrue()
    }
}
