package com.hartwig.actin.personalization.ncr.interpretation.conversion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetastaticRtIntervalConversionTest {

    @Test
    fun `Should convert metastatic radiotherapy interval codes to integers`() {
        assertThat(MetastaticRtIntervalConversion.convert("-1")).isEqualTo(-1)
        assertThat(MetastaticRtIntervalConversion.convert("1")).isEqualTo(1)

        assertThat(MetastaticRtIntervalConversion.convert(null)).isNull()
        assertThat(MetastaticRtIntervalConversion.convert(".")).isNull()
    }
}