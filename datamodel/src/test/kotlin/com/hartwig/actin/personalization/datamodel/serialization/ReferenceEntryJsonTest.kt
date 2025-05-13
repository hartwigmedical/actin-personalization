package com.hartwig.actin.personalization.datamodel.serialization

import com.hartwig.actin.personalization.datamodel.TestReferenceEntryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists

class ReferenceEntryJsonTest {

    private val entries =
        listOf(
            TestReferenceEntryFactory.emptyReferenceEntry(),
            TestReferenceEntryFactory.minimalReferenceEntry(),
            TestReferenceEntryFactory.exhaustiveReferenceEntry()
        )

    @Test
    fun `Should serialize and deserialize reference entries in memory without changing their contents`() {
        val serialized = ReferenceEntryJson.toJson(entries)
        val deserialized = ReferenceEntryJson.fromJson(serialized)
        assertThat(deserialized).isEqualTo(entries)
    }

    @Test
    fun `Should serialize and deserialize reference entries on disk without changing their contents`() {
        val tempFile = createTempFile()
        val path = tempFile.toString()
        ReferenceEntryJson.write(entries, path)
        assertThat(ReferenceEntryJson.read(path)).isEqualTo(entries)
        tempFile.deleteIfExists()
    }
}