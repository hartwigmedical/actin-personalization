package com.hartwig.actin.personalization.datamodel.serialization

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.DistantMetastasesStatus
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.TumorEntry
import com.hartwig.actin.personalization.datamodel.TumorType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists

private val DIAGNOSIS = Diagnosis(
    consolidatedTumorType = TumorType.CRC_OTHER,
    tumorLocations = emptySet(),
    hasHadTumorDirectedSystemicTherapy = false,
    ageAtDiagnosis = 50,
    intervalTumorIncidenceLatestAliveStatus = 100,
    hasHadPriorTumor = false,
    priorTumors = emptyList()
)
private val EPISODE = Episode(
    id = 123,
    order = 1,
    tumorIncidenceYear = 2020,
    tumorBasisOfDiagnosis = TumorBasisOfDiagnosis.CLINICAL_AND_DIAGNOSTIC_INVESTIGATION,
    tumorLocation = Location.COLON_NOS,
    distantMetastasesStatus = DistantMetastasesStatus.AT_START,
    metastases = emptyList(),
    hasReceivedTumorDirectedTreatment = false,
    hasHadHipecTreatment = false,
    hasHadPreSurgeryRadiotherapy = false,
    hasHadPostSurgeryRadiotherapy = false,
    hasHadPreSurgeryChemoRadiotherapy = false,
    hasHadPostSurgeryChemoRadiotherapy = false,
    hasHadPreSurgerySystemicChemotherapy = false,
    hasHadPostSurgerySystemicChemotherapy = false,
    hasHadPreSurgerySystemicTargetedTherapy = false,
    hasHadPostSurgerySystemicTargetedTherapy = false,
    pfsMeasures = emptyList()
)
private val PATIENT_RECORDS = listOf(
    PatientRecord(
        ncrId = 123,
        sex = Sex.MALE,
        isAlive = true,
        tumorEntries = listOf(TumorEntry(DIAGNOSIS, listOf(EPISODE))),
    )
)

class PatientRecordJsonTest {

    @Test
    fun `Should serialize and deserialize patient records in memory without changing their contents`() {
        val serialized = PatientRecordJson.toJson(PATIENT_RECORDS)
        val deserialized = PatientRecordJson.fromJson(serialized)
        Assertions.assertThat(deserialized).isEqualTo(PATIENT_RECORDS)
    }

    @Test
    fun `Should serialize and deserialize patient records on disk without changing their contents`() {
        val tempFile = createTempFile()
        val path = tempFile.toString()
        PatientRecordJson.write(PATIENT_RECORDS, path)
        Assertions.assertThat(PatientRecordJson.read(path)).isEqualTo(PATIENT_RECORDS)
        tempFile.deleteIfExists()
    }
}