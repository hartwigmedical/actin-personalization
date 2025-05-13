package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.ReferenceSource
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification
import com.hartwig.actin.personalization.datamodel.assessment.ComorbidityAssessment
import com.hartwig.actin.personalization.datamodel.assessment.MolecularResult
import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrReferenceEntryExtractorTest {

    @Test
    fun `Should extract reference entry from minimal diagnosis NCR record`() {
        val entry = NcrReferenceEntryExtractor.extract(TestNcrRecordFactory.minimalEntryRecords())

        with(entry) {
            assertThat(source).isEqualTo(ReferenceSource.NCR)
            assertThat(sourceId).isEqualTo(2)
            
            assertThat(diagnosisYear).isEqualTo(2020)
            assertThat(ageAtDiagnosis).isEqualTo(75)
            assertThat(sex).isEqualTo(Sex.FEMALE)

            assertThat(latestSurvivalMeasurement.daysSinceDiagnosis).isEqualTo(563)
            assertThat(latestSurvivalMeasurement.isAlive).isTrue()

            assertThat(whoAssessments).isEmpty()
            assertThat(asaAssessments).isEmpty()
            assertThat(comorbidityAssessments).isEmpty()
            assertThat(molecularResults).isEmpty()
        }
    }

    @Test
    fun `Should extract reference entry from proper tumor NCR records`() {
        val entry = NcrReferenceEntryExtractor.extract(TestNcrRecordFactory.properEntryRecords())

        with(entry) {
            assertThat(source).isEqualTo(ReferenceSource.NCR)
            assertThat(sourceId).isEqualTo(2)
            
            assertThat(diagnosisYear).isEqualTo(2020)
            assertThat(ageAtDiagnosis).isEqualTo(75)
            assertThat(sex).isEqualTo(Sex.FEMALE)

            assertThat(latestSurvivalMeasurement.daysSinceDiagnosis).isEqualTo(563)
            assertThat(latestSurvivalMeasurement.isAlive).isTrue()

            assertThat(whoAssessments).containsExactly(
                WhoAssessment(daysSinceDiagnosis = 0, whoStatus = 1),
                WhoAssessment(daysSinceDiagnosis = 50, whoStatus = 1),
                WhoAssessment(daysSinceDiagnosis = 100, whoStatus = 2)
            )
            assertThat(asaAssessments).containsExactly(
                AsaAssessment(daysSinceDiagnosis = 0, classification = AsaClassification.V),
                AsaAssessment(daysSinceDiagnosis = 50, classification = AsaClassification.V),
                AsaAssessment(daysSinceDiagnosis = 100, classification = AsaClassification.VI)
            )

            assertThat(comorbidityAssessments).containsExactly(expectedComorbidityAssessment())
            assertThat(molecularResults).containsExactly(expectedMolecularResult())
        }
    }

    private fun expectedComorbidityAssessment(): ComorbidityAssessment {
        return ComorbidityAssessment(
            daysSinceDiagnosis = 0,
            charlsonComorbidityIndex = 2,
            hasAids = false,
            hasCongestiveHeartFailure = true,
            hasCollagenosis = false,
            hasCopd = false,
            hasCerebrovascularDisease = false,
            hasDementia = true,
            hasDiabetesMellitus = false,
            hasDiabetesMellitusWithEndOrganDamage = false,
            hasOtherMalignancy = false,
            hasOtherMetastaticSolidTumor = false,
            hasMyocardialInfarct = false,
            hasMildLiverDisease = false,
            hasHemiplegiaOrParaplegia = false,
            hasPeripheralVascularDisease = false,
            hasRenalDisease = false,
            hasLiverDisease = false,
            hasUlcerDisease = false
        )
    }

    private fun expectedMolecularResult(): MolecularResult {
        return MolecularResult(
            daysSinceDiagnosis = 0,
            hasMsi = false,
            hasBrafMutation = true,
            hasBrafV600EMutation = null,
            hasRasMutation = true,
            hasKrasG12CMutation = true
        )
    }
}
