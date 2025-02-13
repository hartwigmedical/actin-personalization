package com.hartwig.actin.personalization.datamodel.serialization

import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.diagnosis.Location
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasure
import com.hartwig.actin.personalization.datamodel.treatment.HipecTreatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists

private val PRIMARY_DIAGNOSIS = PrimaryDiagnosis(
    basisOfDiagnosis = TumorBasisOfDiagnosis.HISTOLOGICAL_CONFIRMATION,
    hasDoublePrimaryTumor = false,
    primaryTumorType = TumorType.CRC_OTHER,
    primaryTumorLocation = Location.DESCENDING_COLON
)

private val METASTATIC_DIAGNOSIS = MetastaticDiagnosis(
    distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_START,
    metastases = emptyList()
)

private val TUMOR = Tumor(
    diagnosisYear = 1961,
    ageAtDiagnosis = 73,
    latestSurvivalStatus = SurvivalMeasure(daysSinceDiagnosis = 151, isAlive = true),

    primaryDiagnosis = PRIMARY_DIAGNOSIS,
    metastaticDiagnosis = METASTATIC_DIAGNOSIS,

    hasReceivedTumorDirectedTreatment = false,
    hipecTreatment = HipecTreatment(daysSinceDiagnosis = null, hasHadHipecTreatment = false),

    responseMeasures = emptyList(),
    progressionMeasures = emptyList()
)

private val PATIENT_RECORDS = listOf(
    ReferencePatient(
        sex = Sex.MALE,
        tumors = listOf(TUMOR),
    )
)

class ReferencePatientJsonTest {

    @Test
    fun `Should serialize and deserialize patient records in memory without changing their contents`() {
        val serialized = ReferencePatientJson.toJson(PATIENT_RECORDS)
        val deserialized = ReferencePatientJson.fromJson(serialized)
        assertThat(deserialized).isEqualTo(PATIENT_RECORDS)
    }

    @Test
    fun `Should serialize and deserialize patient records on disk without changing their contents`() {
        val tempFile = createTempFile()
        val path = tempFile.toString()
        ReferencePatientJson.write(PATIENT_RECORDS, path)
        assertThat(ReferencePatientJson.read(path)).isEqualTo(PATIENT_RECORDS)
        tempFile.deleteIfExists()
    }
}