package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.diagnosis.MetastasesDetectionStatus
import com.hartwig.actin.personalization.datamodel.old.DiagnosisEpisode
import com.hartwig.actin.personalization.datamodel.old.Surgery
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersonalizedDataInterpreterTest {

    @Test
    fun `Should create interpreter with filtered and grouped patient records`() {
        val fluorouracilEpisode = tumorWithTreatment(treatment = Treatment.FLUOROURACIL)
        val capecitabineEpisode = tumorWithTreatment(treatment = Treatment.CAPECITABINE)
        val capoxEpisode = tumorWithTreatment(treatment = Treatment.CAPOX)

//        val patients = listOf(
//            patientWithTumor(fluorouracilEpisode),
//            patientWithTumor(fluorouracilEpisode.copy(distantMetastasesDetectionStatus = MetastasesDetectionStatus.AT_PROGRESSION)),
//            patientWithTumor(tumorWithTreatment(treatment = Treatment.OTHER).episode),
//            patientWithTumor(fluorouracilEpisode.copy(systemicTreatmentPlan = null)),
//            patientWithTumor(fluorouracilEpisode.copy(surgeries = listOf(Surgery(SurgeryType.NOS_OR_OTHER)))),
//            patientWithTumor(fluorouracilEpisode.copy(hasHadPostSurgerySystemicChemotherapy = true)),
//            patientWithTumor(capecitabineEpisode),
//            patientWithTumor(capoxEpisode)
//        )
//
//        val interpreter = PersonalizedDataInterpreter.createFromReferencePatients(patients)
//
//        val expectedDiagnosisEpisodes = mapOf(
//            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(
//                DiagnosisEpisode(DIAGNOSIS, fluorouracilEpisode),
//                DiagnosisEpisode(DIAGNOSIS, capecitabineEpisode)
//            ),
//            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(
//                DiagnosisEpisode(DIAGNOSIS, capoxEpisode)
//            )
//        )
//
//        assertThat(interpreter.tumorsByTreatment).containsExactlyInAnyOrder(
//            *expectedDiagnosisEpisodes.entries.map { it.toPair() }.toTypedArray()
//        )
    }
}