package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrPfsInterpreterTest {
    private val daysUntilPlanStart = 5
    private val daysUntilPlanEnd = 20

    @Test
    fun `Should ignore pfs measure if measure occurred before treatment plan start`() {
        val pfsMeasures = listOf(PfsMeasure(PfsMeasureType.PROGRESSION, null, 4))
        val (pfs, event) = NcrPfsInterpreter().determineObservedPfsAndProgressionEvent(daysUntilPlanStart, daysUntilPlanEnd, pfsMeasures)
        assertThat(pfs).isNull()
        assertThat(event).isNull()
    }

    @Test
    fun `Should not determine observedPfsDays or hadProgressionEvent in case at least one interval is null`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 5),
            PfsMeasure(PfsMeasureType.CENSOR, null, null),
        )
        val (pfs, event) = NcrPfsInterpreter().determineObservedPfsAndProgressionEvent(daysUntilPlanStart, daysUntilPlanEnd, pfsMeasures)
        assertThat(pfs).isNull()
        assertThat(event).isNull()
    }

    @Test
    fun `Should interpret censor pfs measure correctly if measure occurred after treatment plan start`() {
        val pfsMeasures = listOf(PfsMeasure(PfsMeasureType.CENSOR, null, 50))
        val (pfs, event) = NcrPfsInterpreter().determineObservedPfsAndProgressionEvent(daysUntilPlanStart, daysUntilPlanEnd, pfsMeasures)
        assertThat(pfs).isEqualTo(45)
        assertThat(event).isFalse()
    }

    @Test
    fun `Should interpret progression pfs value correctly if measure occurred after treatment plan start`() {
        val pfsMeasures = listOf(PfsMeasure(PfsMeasureType.PROGRESSION, null, 50))
        val (pfs, event) = NcrPfsInterpreter().determineObservedPfsAndProgressionEvent(daysUntilPlanStart, daysUntilPlanEnd, pfsMeasures)
        assertThat(pfs).isEqualTo(45)
        assertThat(event).isTrue()
    }

    @Test
    fun `Should determine observedPfsDays or hadProgressionEvent in case missing stop date if there is only one progression measure`() {
        val pfsMeasures = listOf(PfsMeasure(PfsMeasureType.PROGRESSION, null, 50))
        val (pfs, event) = NcrPfsInterpreter().determineObservedPfsAndProgressionEvent(daysUntilPlanStart, null, pfsMeasures)
        assertThat(pfs).isEqualTo(45)
        assertThat(event).isTrue()
    }

    @Test
    fun `Should not determine observedPfsDays or hadProgressionEvent in case missing stop date and there are multiple progression measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 10),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 50),
        )
        val (pfs, event) = NcrPfsInterpreter().determineObservedPfsAndProgressionEvent(daysUntilPlanStart, null, pfsMeasures)
        assertThat(pfs).isNull()
        assertThat(event).isNull()
    }

    @Test
    fun `Should interpret first pfs measure after treatment end if there is at least one measure after treatment end`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 2),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 6),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 22),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 25),
        )
        val (pfs, event) = NcrPfsInterpreter().determineObservedPfsAndProgressionEvent(daysUntilPlanStart, daysUntilPlanEnd, pfsMeasures)
        assertThat(pfs).isEqualTo(17)
        assertThat(event).isTrue()
    }

    @Test
    fun `Should interpret last pfs measure if there is no measure after treatment end`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 2),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 6),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 14),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 18),
        )
        val (pfs, event) = NcrPfsInterpreter().determineObservedPfsAndProgressionEvent(daysUntilPlanStart, daysUntilPlanEnd, pfsMeasures)
        assertThat(pfs).isEqualTo(13)
        assertThat(event).isTrue()
    }

    @Test
    fun `Should not interpret any pfs measure if there are both censor and progression measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.CENSOR, null, 2),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 6),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 14),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 18),
        )
        val (pfs, event) = NcrPfsInterpreter().determineObservedPfsAndProgressionEvent(daysUntilPlanStart, daysUntilPlanEnd, pfsMeasures)
        assertThat(pfs).isNull()
        assertThat(event).isNull()
    }

    @Test
    fun `Should not interpret any pfs measure if there are multiple censor measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.CENSOR, null, 2),
            PfsMeasure(PfsMeasureType.CENSOR, null, 6),
            PfsMeasure(PfsMeasureType.CENSOR, null, 14),
        )
        val (pfs, event) = NcrPfsInterpreter().determineObservedPfsAndProgressionEvent(daysUntilPlanStart, daysUntilPlanEnd, pfsMeasures)
        assertThat(pfs).isNull()
        assertThat(event).isNull()
    }
}