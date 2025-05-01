package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersonalizedDataInterpreterTest {

    @Test
    fun `Should create interpreter with filtered and grouped patient records`() {
        val fluorouracilTumor = tumorWithTreatment(treatment = Treatment.FLUOROURACIL)
        val capecitabineTumor = tumorWithTreatment(treatment = Treatment.CAPECITABINE)
        val capoxTumor = tumorWithTreatment(treatment = Treatment.CAPOX)
        
        val patients = listOf(
            patientWithTumor(fluorouracilTumor),
            patientWithTumor(
                tumorWithTreatment(
                    treatment = Treatment.FLUOROURACIL,
                    metastaticPresence = MetastaticPresence.AT_PROGRESSION
                )
            ),
            patientWithTumor(tumorWithTreatment(treatment = Treatment.OTHER)),
            patientWithTumor(tumorWithTreatment(treatment = null)),
            patientWithTumor(tumorWithTreatment(treatment = Treatment.FLUOROURACIL, surgeryType = SurgeryType.NOS_OR_OTHER)),
            patientWithTumor(capecitabineTumor),
            patientWithTumor(capoxTumor)
        )

        val interpreter = PersonalizedDataInterpreter.createFromReferencePatients(patients)

        val expectedDiagnosisEpisodes = mapOf(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(
                fluorouracilTumor,
                capecitabineTumor
            ),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(
                capoxTumor
            )
        )

        assertThat(interpreter.tumorsByTreatment).containsExactlyInAnyOrder(
            *expectedDiagnosisEpisodes.entries.map { it.toPair() }.toTypedArray()
        )
    }
}