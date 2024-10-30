package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrObservedPfsDeterminationTest {

    @Test
    fun `Should ignore pfs measure if measure occurred before treatment plan start`() {
        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, listOf(PfsMeasure(PfsMeasureType.PROGRESSION, null, 0)), null, 300)
        assertThat(plan!!.observedPfsDays).isNull()
        assertThat(plan.hadProgressionEvent).isNull()
    }

    @Test
    fun `Should not determine observedPfsDays or hadProgressionEvent in case at least one interval is null`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 5),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, null),
        )

        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, pfsMeasures, null, 300)
        assertThat(plan!!.observedPfsDays).isNull()
        assertThat(plan.hadProgressionEvent).isNull()
    }

    @Test
    fun `Should interpret censor pfs value if measure occurred after treatment plan start`() {
        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, listOf(PfsMeasure(PfsMeasureType.CENSOR, null, 50)), null, 300)
        assertThat(plan!!.observedPfsDays).isEqualTo(49)
        assertThat(plan.hadProgressionEvent).isFalse()
    }

    @Test
    fun `Should interpret progression pfs value if measure occurred after treatment plan start`() {
        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, listOf(PfsMeasure(PfsMeasureType.PROGRESSION, null, 50)), null, 300)
        assertThat(plan!!.observedPfsDays).isEqualTo(49)
        assertThat(plan.hadProgressionEvent).isTrue()
    }

    @Test
    fun `Should interpret second pfs value if one occurred before and one after start in case of n=2 progression measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 5),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 10),
        )

        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, pfsMeasures, null, 300)
        assertThat(plan!!.observedPfsDays).isEqualTo(9)
        assertThat(plan.hadProgressionEvent).isTrue()
    }

    @Test
    fun `Should interpret second pfs value if both occurred before end date in case of n=2 progression measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 2),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 4),
        )

        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, pfsMeasures, null, 300)
        assertThat(plan!!.observedPfsDays).isEqualTo(3)
        assertThat(plan.hadProgressionEvent).isTrue()
    }

    @Test
    fun `Should interpret first pfs value if both occurred after treatment end date in case of n=2 progression measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 12),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 16),
        )

        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, pfsMeasures, null, 300)
        assertThat(plan!!.observedPfsDays).isEqualTo(11)
        assertThat(plan.hadProgressionEvent).isTrue()
    }

    @Test
    fun `Should interpret first one after treatment plan stop date in case more than 2 progression measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 4),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 20),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 21),
        )

        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, pfsMeasures, null, 300)
        assertThat(plan!!.observedPfsDays).isEqualTo(19)
        assertThat(plan.hadProgressionEvent).isTrue()
    }

    @Test
    fun `Should resolve to null in case stop date is null and there are two progression measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 4),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 20),
        )

        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(
                NCR_SYSTEMIC_TREATMENT.copy(
                    systStopInt1 = null,
                    systStopInt2 = null,
                    systStopInt3 = null,
                    systStopInt4 = null,
                    systStopInt5 = null,
                    systStopInt6 = null,
                    systStopInt7 = null
                ), pfsMeasures, null, 300
            )
        assertThat(plan!!.observedPfsDays).isNull()
        assertThat(plan.hadProgressionEvent).isNull()
    }

    @Test
    fun `Should interpret last pfs measure if all before stop date in case more than 2 progression measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 2),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 4),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 6),
        )

        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, pfsMeasures, null, 300)
        assertThat(plan!!.observedPfsDays).isEqualTo(5)
        assertThat(plan.hadProgressionEvent).isTrue()
    }
}