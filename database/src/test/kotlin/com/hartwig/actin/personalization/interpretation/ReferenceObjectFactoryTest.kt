package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.database.interpretation.ReferenceObjectFactory
import com.hartwig.actin.personalization.datamodel.TestReferenceEntryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReferenceObjectFactoryTest {
    
    @Test
    fun `Should create object for empty reference entry`() {
        assertThat(ReferenceObjectFactory.create(TestReferenceEntryFactory.empty())).isNotNull()
    }

    @Test
    fun `Should create object for minimal reference entry`() {
        assertThat(ReferenceObjectFactory.create(TestReferenceEntryFactory.minimal())).isNotNull()
    }

    @Test
    fun `Should create object for exhaustive reference entry`() {
        assertThat(ReferenceObjectFactory.create(TestReferenceEntryFactory.exhaustive())).isNotNull()
    }
}