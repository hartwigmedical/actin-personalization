package com.hartwig.actin.personalization.datamodel.serialization

import com.hartwig.actin.personalization.datamodel.TestReferencePatientFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists

class ReferencePatientJsonTest {

    private val records =
        listOf(
            TestReferencePatientFactory.emptyReferencePatient(),
            TestReferencePatientFactory.minimalReferencePatient(),
            TestReferencePatientFactory.exhaustiveReferencePatient()
        )

    @Test
    fun `Should serialize and deserialize patient records in memory without changing their contents`() {
        val serialized = ReferencePatientJson.toJson(records)
        val deserialized = ReferencePatientJson.fromJson(serialized)
        assertThat(deserialized).isEqualTo(records)
    }

    @Test
    fun `Should serialize and deserialize patient records on disk without changing their contents`() {
        val tempFile = createTempFile()
        val path = tempFile.toString()
        ReferencePatientJson.write(records, path)
        assertThat(ReferencePatientJson.read(path)).isEqualTo(records)
        tempFile.deleteIfExists()
    }
}