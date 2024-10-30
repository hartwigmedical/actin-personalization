package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.datamodel.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.ResponseType
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentSchemeDrug
import com.hartwig.actin.personalization.datamodel.Treatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrSystemicTreatmentPlanExtractorTest {
    @Test
    fun `Should extract systemic treatment plan`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 100),
            PfsMeasure(PfsMeasureType.DEATH, null, 200),
        )

        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, pfsMeasures, ResponseMeasure(ResponseType.PD, 3), 300)
        assertThat(plan!!.treatment).isEqualTo(Treatment.FOLFOXIRI_B)

        val expectedDrugs = mapOf(
            1 to listOf(Drug.OXALIPLATIN, Drug.BEVACIZUMAB, Drug.IRINOTECAN, Drug.FLUOROURACIL),
            2 to listOf(Drug.CAPECITABINE, Drug.IRINOTECAN),
            3 to listOf(Drug.TEGAFUR_OR_GIMERACIL_OR_OTERACIL)
        )
        val drugsByScheme =
            plan.systemicTreatmentSchemes.associate { it.schemeNumber to it.treatmentComponents.map(SystemicTreatmentSchemeDrug::drug) }
        assertThat(drugsByScheme).isEqualTo(expectedDrugs)

        assertThat(plan.intervalTumorIncidenceTreatmentPlanStartDays).isEqualTo(1)
        assertThat(plan.intervalTumorIncidenceTreatmentPlanStopDays).isEqualTo(7)
        assertThat(plan.intervalTreatmentPlanStartResponseDays).isEqualTo(2)
        assertThat(plan.observedOsFromTreatmentStartDays).isEqualTo(299)
        assertThat(plan.observedPfsDays).isEqualTo(99)
        assertThat(plan.hadProgressionEvent).isTrue()
    }

    @Test
    fun `Should ignore pfs value if measure occurred before treatment plan start`() {
        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, listOf(PfsMeasure(PfsMeasureType.PROGRESSION, null, 0)), null, 300)
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

    @Test
    fun `Should resolve treatment to OTHER when a different component is used in a later scheme`() {
        val ncrSystemicTreatment = NCR_SYSTEMIC_TREATMENT.copy(systCode7 = "L01FE02") // Add Panitumumab to last scheme
        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(ncrSystemicTreatment, emptyList(), ResponseMeasure(ResponseType.PD, 3), 80)
        assertThat(plan!!.treatment).isEqualTo(Treatment.OTHER)
    }
}