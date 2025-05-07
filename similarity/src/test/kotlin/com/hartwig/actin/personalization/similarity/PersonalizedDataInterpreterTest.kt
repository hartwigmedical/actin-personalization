package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.SurgeryType
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersonalizedDataInterpreterTest {

    @Test
    fun `Should create interpreter with filtered and grouped patient records`() {
        val fluorouracilTumor = TestDatamodelFactory.tumor(systemicTreatment = Treatment.FLUOROURACIL)
        val capecitabineTumor = TestDatamodelFactory.tumor(systemicTreatment = Treatment.CAPECITABINE)
        val capoxTumor = TestDatamodelFactory.tumor(systemicTreatment = Treatment.CAPOX)

        val patients = listOf(
            TestDatamodelFactory.patient(fluorouracilTumor),
            TestDatamodelFactory.patient(
                TestDatamodelFactory.tumor(
                    systemicTreatment = Treatment.FLUOROURACIL,
                    metastaticPresenceUnderSystemicTreatment = MetastaticPresence.AT_PROGRESSION
                )
            ),
            TestDatamodelFactory.patient(TestDatamodelFactory.tumor(systemicTreatment = Treatment.OTHER)),
            TestDatamodelFactory.patient(TestDatamodelFactory.tumor(systemicTreatment = null)),
            TestDatamodelFactory.patient(
                TestDatamodelFactory.tumor(systemicTreatment = Treatment.FLUOROURACIL, primarySurgeryType = SurgeryType.NOS_OR_OTHER)
            ),
            TestDatamodelFactory.patient(capecitabineTumor),
            TestDatamodelFactory.patient(capoxTumor)
        )

        val interpreter = PersonalizedDataInterpreter.createFromReferencePatients(patients)

        val expectedGroupedTumors = mapOf(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(
                fluorouracilTumor,
                capecitabineTumor
            ),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(
                capoxTumor
            )
        )

        assertThat(interpreter.tumorsByTreatment).containsExactlyInAnyOrder(
            *expectedGroupedTumors.entries.map { it.toPair() }.toTypedArray()
        )
    }
}