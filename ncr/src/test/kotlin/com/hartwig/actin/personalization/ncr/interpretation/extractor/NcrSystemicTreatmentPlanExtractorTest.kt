package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.old.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.old.SystemicTreatmentSchemeDrug
import com.hartwig.actin.personalization.datamodel.v2.outcome.ResponseType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrSystemicTreatmentPlanExtractorTest {
    @Test
    fun `Should extract systemic treatment plan`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.CENSOR, null, 1),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 4),
            PfsMeasure(PfsMeasureType.DEATH, null, 100),
        )

        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, pfsMeasures, ResponseMeasure(ResponseType.PD, 3), 80)
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
        assertThat(plan.observedOsFromTreatmentStartDays).isEqualTo(79)
    }

    @Test
    fun `Should resolve treatment to OTHER when a different component is used in a later scheme`() {
        val ncrSystemicTreatment = NCR_SYSTEMIC_TREATMENT.copy(systCode7 = "L01FE02") // Add Panitumumab to last scheme
        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(ncrSystemicTreatment, emptyList(), ResponseMeasure(ResponseType.PD, 3), 80)
        assertThat(plan!!.treatment).isEqualTo(Treatment.OTHER)
    }
}