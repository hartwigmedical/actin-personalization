package com.hartwig.actin.personalization.datamodel.serialization

import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.diagnosis.Location
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorBasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.old.Diagnosis
import com.hartwig.actin.personalization.datamodel.old.Episode
import com.hartwig.actin.personalization.datamodel.old.ReferencePatient
import com.hartwig.actin.personalization.datamodel.old.TumorEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists

private val DIAGNOSIS = Diagnosis(
    consolidatedTumorType = TumorType.CRC_OTHER,
    tumorLocations = emptySet(),
    hasHadTumorDirectedSystemicTherapy = false,
    ageAtDiagnosis = 50,
    observedOsFromTumorIncidenceDays = 100,
    hasHadPriorTumor = false,
    hadSurvivalEvent = false,
    priorTumors = emptyList(),
    orderOfFirstDistantMetastasesEpisode = 1,
    isMetachronous = false
)
private val EPISODE = Episode(
    id = 123,
    order = 1,
    tumorIncidenceYear = 2020,
    tumorBasisOfDiagnosis = TumorBasisOfDiagnosis.CLINICAL_AND_DIAGNOSTIC_INVESTIGATION,
    tumorLocation = Location.COLON_NOS,
    distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_START,
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
    pfsMeasures = emptyList(),
    ageAtTreatmentPlanStart = 52,
)
private val PATIENT_RECORDS = listOf(
    ReferencePatient(
        ncrId = 123,
        sex = Sex.MALE,
        isAlive = true,
        tumorEntries = listOf(TumorEntry(DIAGNOSIS, listOf(EPISODE))),
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