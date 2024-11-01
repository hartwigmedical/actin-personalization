package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.ncr.datamodel.NcrSystemicTreatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrPfsInterpreterTest {
    private val interval = 300
    private val response = null
    private val treatment = NcrSystemicTreatment(
        chemo = 1,
        target = 1,
        systCode1 = "L01XA03",
        systPrepost1 = 1,
        systSchemanum1 = 1,
        systKuren1 = 1,
        systStartInt1 = 5,
        systStopInt1 = 20
    )

    @Test
    fun `Should ignore pfs measure if measure occurred before treatment plan start`() {
        val pfsMeasures = listOf(PfsMeasure(PfsMeasureType.PROGRESSION, null, 4))
        val plan = NcrSystemicTreatmentPlanExtractor().extractSystemicTreatmentPlan(treatment, pfsMeasures, response, interval)
        assertThat(plan!!.observedPfsDays).isNull()
        assertThat(plan.hadProgressionEvent).isNull()
    }

    @Test
    fun `Should not determine observedPfsDays or hadProgressionEvent in case at least one interval is null`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 5),
            PfsMeasure(PfsMeasureType.CENSOR, null, null),
        )
        val plan = NcrSystemicTreatmentPlanExtractor().extractSystemicTreatmentPlan(treatment, pfsMeasures, response, interval)
        assertThat(plan!!.observedPfsDays).isNull()
        assertThat(plan.hadProgressionEvent).isNull()
    }

    @Test
    fun `Should interpret censor pfs measure correctly if measure occurred after treatment plan start`() {
        val pfsMeasures = listOf(PfsMeasure(PfsMeasureType.CENSOR, null, 50))
        val plan = NcrSystemicTreatmentPlanExtractor().extractSystemicTreatmentPlan(treatment, pfsMeasures, response, interval)
        assertThat(plan!!.observedPfsDays).isEqualTo(45)
        assertThat(plan.hadProgressionEvent).isFalse()
    }

    @Test
    fun `Should interpret progression pfs value correctly if measure occurred after treatment plan start`() {
        val pfsMeasures = listOf(PfsMeasure(PfsMeasureType.PROGRESSION, null, 50))
        val plan = NcrSystemicTreatmentPlanExtractor().extractSystemicTreatmentPlan(treatment, pfsMeasures, response, interval)
        assertThat(plan!!.observedPfsDays).isEqualTo(45)
        assertThat(plan.hadProgressionEvent).isTrue()
    }

    @Test
    fun `Should determine observedPfsDays or hadProgressionEvent in case missing stop date if there is only one progression measure`() {
        val pfsMeasures = listOf(PfsMeasure(PfsMeasureType.PROGRESSION, null, 50))
        val plan = NcrSystemicTreatmentPlanExtractor().extractSystemicTreatmentPlan(
            treatment.copy(systStopInt1 = null),
            pfsMeasures,
            response,
            interval
        )
        assertThat(plan!!.observedPfsDays).isEqualTo(45)
        assertThat(plan.hadProgressionEvent).isTrue()
    }

    @Test
    fun `Should not determine observedPfsDays or hadProgressionEvent in case missing stop date and there are multiple progression measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 10),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 50),
        )
        val plan = NcrSystemicTreatmentPlanExtractor().extractSystemicTreatmentPlan(
            treatment.copy(systStopInt1 = null),
            pfsMeasures,
            response,
            interval
        )
        assertThat(plan!!.observedPfsDays).isNull()
        assertThat(plan.hadProgressionEvent).isNull()
    }

    @Test
    fun `Should interpret first pfs measure after treatment end if there is at least one measure after treatment end`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 2),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 6),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 22),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 25),
        )

        val plan = NcrSystemicTreatmentPlanExtractor().extractSystemicTreatmentPlan(treatment, pfsMeasures, response, interval)
        assertThat(plan!!.observedPfsDays).isEqualTo(17)
        assertThat(plan.hadProgressionEvent).isTrue()
    }

    @Test
    fun `Should interpret last pfs measure if there is no measure after treatment end`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 2),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 6),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 14),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 18),
        )

        val plan = NcrSystemicTreatmentPlanExtractor().extractSystemicTreatmentPlan(treatment, pfsMeasures, response, interval)
        assertThat(plan!!.observedPfsDays).isEqualTo(13)
        assertThat(plan.hadProgressionEvent).isTrue()
    }

    @Test
    fun `Should not interpret any pfs measure if there are both censor and progression measures`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.CENSOR, null, 2),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 6),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 14),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 18),
        )

        val plan = NcrSystemicTreatmentPlanExtractor().extractSystemicTreatmentPlan(treatment, pfsMeasures, response, interval)
        assertThat(plan!!.observedPfsDays).isNull()
        assertThat(plan.hadProgressionEvent).isNull()
    }
}