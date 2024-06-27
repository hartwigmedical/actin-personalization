package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.Drug
import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.datamodel.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.ResponseMeasureType
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentComponent
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.ncr.datamodel.NcrSystemicTreatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private val NCR_SYSTEMIC_TREATMENT = NcrSystemicTreatment(
    chemo = 1,
    target = 2,
    systCode1 = "L01XA03",
    systCode2 = "L01FG01",
    systCode3 = "L01CE02",
    systCode4 = "L01BC02",
    systCode5 = "L01BC06",
    systCode6 = "L01CE02",
    systCode7 = "L01BC53",
    systCode8 = null,
    systCode9 = null,
    systCode10 = null,
    systCode11 = null,
    systCode12 = null,
    systCode13 = null,
    systCode14 = null,
    systPrepost1 = 1,
    systPrepost2 = 2,
    systPrepost3 = 3,
    systPrepost4 = 4,
    systPrepost5 = 0,
    systPrepost6 = null,
    systPrepost7 = 0,
    systPrepost8 = null,
    systPrepost9 = null,
    systPrepost10 = null,
    systPrepost11 = null,
    systPrepost12 = null,
    systPrepost13 = null,
    systPrepost14 = null,
    systSchemanum1 = 1,
    systSchemanum2 = 1,
    systSchemanum3 = 1,
    systSchemanum4 = 1,
    systSchemanum5 = 2,
    systSchemanum6 = 2,
    systSchemanum7 = 3,
    systSchemanum8 = null,
    systSchemanum9 = null,
    systSchemanum10 = null,
    systSchemanum11 = null,
    systSchemanum12 = null,
    systSchemanum13 = null,
    systSchemanum14 = null,
    systKuren1 = 1,
    systKuren2 = 2,
    systKuren3 = 3,
    systKuren4 = 4,
    systKuren5 = 5,
    systKuren6 = 6,
    systKuren7 = 7,
    systKuren8 = null,
    systKuren9 = null,
    systKuren10 = null,
    systKuren11 = null,
    systKuren12 = null,
    systKuren13 = null,
    systKuren14 = null,
    systStartInt1 = 1,
    systStartInt2 = 2,
    systStartInt3 = 3,
    systStartInt4 = 4,
    systStartInt5 = 5,
    systStartInt6 = 6,
    systStartInt7 = 7,
    systStartInt8 = null,
    systStartInt9 = null,
    systStartInt10 = null,
    systStartInt11 = null,
    systStartInt12 = null,
    systStartInt13 = null,
    systStartInt14 = null,
    systStopInt1 = 1,
    systStopInt2 = 2,
    systStopInt3 = 3,
    systStopInt4 = 4,
    systStopInt5 = 5,
    systStopInt6 = 6,
    systStopInt7 = 7,
    systStopInt8 = null,
    systStopInt9 = null,
    systStopInt10 = null,
    systStopInt11 = null,
    systStopInt12 = null,
    systStopInt13 = null,
    systStopInt14 = null,
)

class NcrSystemicTreatmentExtractorTest {
    @Test
    fun `Should extract systemic treatment plan`() {
        val pfsMeasures = listOf(
            PfsMeasure(PfsMeasureType.CENSOR, null, 1),
            PfsMeasure(PfsMeasureType.PROGRESSION, null, 4),
            PfsMeasure(PfsMeasureType.DEATH, null, 100),
        )

        val plan = extractSystemicTreatmentPlan(NCR_SYSTEMIC_TREATMENT, pfsMeasures, ResponseMeasure(ResponseMeasureType.PD, 3), 80)
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
        val plan = extractSystemicTreatmentPlan(ncrSystemicTreatment, emptyList(), ResponseMeasure(ResponseMeasureType.PD, 3), 80)
        assertThat(plan!!.treatment).isEqualTo(Treatment.OTHER)
    }
}