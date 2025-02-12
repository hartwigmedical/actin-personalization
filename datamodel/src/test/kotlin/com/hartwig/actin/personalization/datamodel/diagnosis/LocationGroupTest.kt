package com.hartwig.actin.personalization.datamodel.diagnosis

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocationGroupTest {

    @Test
    fun `Should resolve top-level group as self for all top-level groups`() {
        listOf(
            LocationGroup.BRAIN,
            LocationGroup.BRONCHUS_AND_LUNG,
            LocationGroup.LIVER_AND_INTRAHEPATIC_BILE_DUCTS,
            LocationGroup.LYMPH_NODES,
            LocationGroup.PERITONEUM
        ).forEach { group ->
            assertThat(group.topLevelGroup()).isEqualTo(group)
        }
    }

    @Test
    fun `Should resolve top-level group as peritoneum for retroperitoneum and peritoneum group`() {
        assertThat(LocationGroup.RETROPERITONEUM_AND_PERITONEUM.topLevelGroup()).isEqualTo(LocationGroup.PERITONEUM)
    }

    @Test
    fun `Should resolve top-level group as other for other groups`() {
        assertThat(LocationGroup.ADRENAL.topLevelGroup()).isEqualTo(LocationGroup.OTHER)
        assertThat(LocationGroup.COLON.topLevelGroup()).isEqualTo(LocationGroup.OTHER)
        assertThat(LocationGroup.OTHER.topLevelGroup()).isEqualTo(LocationGroup.OTHER)
    }
}