package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.datamodel.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.ResponseMeasureType
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentComponent
import com.hartwig.actin.personalization.datamodel.Treatment
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
            .extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, pfsMeasures, ResponseMeasure(ResponseMeasureType.PD, 3), 80)
        assertThat(plan!!.treatment).isEqualTo(Treatment.FOLFOXIRI_B)

        val expectedDrugs = mapOf(
            1 to listOf(Drug.OXALIPLATIN, Drug.BEVACIZUMAB, Drug.IRINOTECAN, Drug.FLUOROURACIL),
            2 to listOf(Drug.CAPECITABINE, Drug.IRINOTECAN),
            3 to listOf(Drug.TEGAFUR_OR_GIMERACIL_OR_OTERACIL)
        )
        val drugsByScheme =
            plan.systemicTreatmentSchemes.associate { it.schemeNumber to it.treatmentComponents.map(SystemicTreatmentComponent::drug) }
        assertThat(drugsByScheme).isEqualTo(expectedDrugs)

        assertThat(plan.intervalTumorIncidenceTreatmentPlanStart).isEqualTo(1)
        assertThat(plan.intervalTumorIncidenceTreatmentPlanStop).isEqualTo(7)
        assertThat(plan.intervalTreatmentPlanStartLatestAliveStatus).isEqualTo(79)
        assertThat(plan.pfs).isEqualTo(3)
        assertThat(plan.intervalTreatmentPlanStartResponseDate).isEqualTo(2)
    }

    @Test
    fun `Should resolve treatment to OTHER when a different component is used in a later scheme`() {
        val ncrSystemicTreatment = NCR_SYSTEMIC_TREATMENT.copy(systCode7 = "L01FE02") // Add Panitumumab to last scheme
        val plan = NcrSystemicTreatmentPlanExtractor()
            .extractSystemicTreatmentPlan(ncrSystemicTreatment, emptyList(), ResponseMeasure(ResponseMeasureType.PD, 3), 80)
        assertThat(plan!!.treatment).isEqualTo(Treatment.OTHER)
    }
}