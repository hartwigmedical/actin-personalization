package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PfsMeasureType
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.similarity.DIAGNOSIS
import com.hartwig.actin.personalization.similarity.episodeWithTreatment
import com.hartwig.actin.personalization.similarity.report.TableElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val MIN_PFS_DAYS = 100

class PercentPfsWithDaysCalculationTest {
    private val calculation = PercentPfsWithDaysCalculation(MIN_PFS_DAYS)
    private val eligibleEpisode = episodeWithTreatment(Treatment.CAPECITABINE, planStart = 10).copy(
        pfsMeasures = listOf(PfsMeasure(PfsMeasureType.CENSOR, null, 200))
    )

    @Test
    fun `Should evaluate patient with known plan start and PFS measure after threshold as eligible`() {
        assertThat(calculation.isEligible(DIAGNOSIS to eligibleEpisode)).isTrue
    }

    @Test
    fun `Should evaluate patient with unknown plan start as ineligible`() {
        val episode = episodeWithTreatment(Treatment.CAPECITABINE).copy(
            pfsMeasures = listOf(PfsMeasure(PfsMeasureType.CENSOR, null, 200))
        )
        assertThat(calculation.isEligible(DIAGNOSIS to episode)).isFalse
    }

    @Test
    fun `Should evaluate patient with known plan start but no PFS measures as ineligible`() {
        val episode = eligibleEpisode.copy(pfsMeasures = emptyList())
        assertThat(calculation.isEligible(DIAGNOSIS to episode)).isFalse
    }

    @Test
    fun `Should evaluate patient with known plan start and only PFS measures before threshold as ineligible`() {
        val episode = eligibleEpisode.copy(
            pfsMeasures = listOf(PfsMeasure(PfsMeasureType.CENSOR, null, 101))
        )
        assertThat(calculation.isEligible(DIAGNOSIS to episode)).isFalse
    }

    @Test
    fun `Should evaluate patient with known plan start and non-censor PFS measure with unknown interval as ineligible`() {
        val episode = eligibleEpisode.copy(
            pfsMeasures = listOf(
                PfsMeasure(PfsMeasureType.CENSOR, null, 200),
                PfsMeasure(PfsMeasureType.PROGRESSION, null, null)
            )
        )
        assertThat(calculation.isEligible(DIAGNOSIS to episode)).isFalse
    }

    @Test
    fun `Should calculate measurement as percentage of population`() {
        val patients = listOf(
            DIAGNOSIS to eligibleEpisode,
            DIAGNOSIS to eligibleEpisode.copy(pfsMeasures = listOf(PfsMeasure(PfsMeasureType.PROGRESSION, null, 101)))
        )
        val measurement = calculation.calculate(patients, 100)
        assertThat(measurement.value).isEqualTo(0.5)
        assertThat(measurement.numPatients).isEqualTo(2)
    }

    @Test
    fun `Should create table element with percentage of population`() {
        val measurement = Measurement(0.03, 3)
        val tableElement = TreatmentDecisionCalculation.createTableElement(measurement)
        assertThat(tableElement).isEqualTo(TableElement.regular("3.0%"))
    }
}