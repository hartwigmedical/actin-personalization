package com.hartwig.actin.personalization.datamodel.serialization

import com.hartwig.actin.personalization.datamodel.ReferencePatient
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocationCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorStage
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasure
import com.hartwig.actin.personalization.datamodel.treatment.Drug
import com.hartwig.actin.personalization.datamodel.treatment.HipecTreatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists

private val GIST_PRIOR_TUMOR = PriorTumor(
    daysBeforeDiagnosis = 732,
    primaryTumorType = TumorType.GASTROINTESTINAL_STROMAL_TUMOR,
    primaryTumorLocation = TumorLocation.ABDOMEN_NOS,
    primaryTumorLocationCategory = TumorLocationCategory.DIGESTIVE_TRACT,
    primaryTumorStage = TumorStage.II,
    systemicDrugsReceived = listOf(Drug.IMATINIB)
)

private val PRIMARY_DIAGNOSIS = PrimaryDiagnosis(
    basisOfDiagnosis = TumorBasisOfDiagnosis.HISTOLOGICAL_CONFIRMATION,
    hasDoublePrimaryTumor = false,
    primaryTumorType = TumorType.CRC_OTHER,
    primaryTumorLocation = TumorLocation.DESCENDING_COLON
)

private val METASTATIC_DIAGNOSIS = MetastaticDiagnosis(
    distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_START,
    metastases = emptyList()
)

private val TUMOR = Tumor(
    diagnosisYear = 1961,
    ageAtDiagnosis = 73,
    latestSurvivalStatus = SurvivalMeasure(daysSinceDiagnosis = 151, isAlive = true),

    priorTumors = listOf(GIST_PRIOR_TUMOR),

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