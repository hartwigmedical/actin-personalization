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
    fun `Should create interpreter with filtered and grouped reference entries`() {
        val fluorouracilEntry = TestDatamodelFactory.entry(systemicTreatment = Treatment.FLUOROURACIL)
        val capecitabineEntry = TestDatamodelFactory.entry(systemicTreatment = Treatment.CAPECITABINE)
        val capoxEntry = TestDatamodelFactory.entry(systemicTreatment = Treatment.CAPOX)

        val entries = listOf(
            fluorouracilEntry,
            TestDatamodelFactory.entry(
                systemicTreatment = Treatment.FLUOROURACIL,
                metastaticPresenceUnderSystemicTreatment = MetastaticPresence.AT_PROGRESSION
            ),
            TestDatamodelFactory.entry(systemicTreatment = Treatment.OTHER),
            TestDatamodelFactory.entry(systemicTreatment = null),
            TestDatamodelFactory.entry(systemicTreatment = Treatment.FLUOROURACIL, primarySurgeryType = SurgeryType.NOS_OR_OTHER),
            capecitabineEntry,
            capoxEntry
        )

        val interpreter = PersonalizedDataInterpreter.createFromReferenceEntries(entries)

        val expectedGroupedEntries = mapOf(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(
                fluorouracilEntry,
                capecitabineEntry
            ),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(
                capoxEntry
            )
        )

        assertThat(interpreter.entriesByTreatment).containsExactlyInAnyOrder(
            *expectedGroupedEntries.entries.map { it.toPair() }.toTypedArray()
        )
    }
}