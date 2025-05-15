package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.diagnosis.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.BasisOfDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.DifferentiationGrade
import com.hartwig.actin.personalization.datamodel.diagnosis.ExtraMuralInvasionCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.LymphaticInvasionCategory
import com.hartwig.actin.personalization.datamodel.diagnosis.Sidedness
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmClassification
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmM
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmN
import com.hartwig.actin.personalization.datamodel.diagnosis.TnmT
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorLocation
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorRegression
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorStage
import com.hartwig.actin.personalization.datamodel.diagnosis.TumorType
import com.hartwig.actin.personalization.datamodel.diagnosis.VenousInvasionDescription
import com.hartwig.actin.personalization.ncr.datamodel.TestNcrRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NcrPrimaryDiagnosisExtractorTest {

    @Test
    fun `Should extract primary diagnosis for minimal NCR record`() {
        val primaryDiagnosis = NcrPrimaryDiagnosisExtractor.extract(TestNcrRecordFactory.minimalEntryRecords())

        assertThat(primaryDiagnosis).isNotNull()
    }

    @Test
    fun `Should extract primary diagnosis for proper set of NCR records`() {
        val primaryDiagnosis = NcrPrimaryDiagnosisExtractor.extract(TestNcrRecordFactory.properEntryRecords())

        with(primaryDiagnosis) {
            assertThat(basisOfDiagnosis).isEqualTo(BasisOfDiagnosis.CLINICAL_ONLY_INVESTIGATION)
            assertThat(hasDoublePrimaryTumor).isFalse()

            assertThat(primaryTumorType).isEqualTo(TumorType.CRC_ADENOCARCINOMA)
            assertThat(primaryTumorLocation).isEqualTo(TumorLocation.ASCENDING_COLON)
            assertThat(sidedness).isEqualTo(Sidedness.RIGHT)
            assertThat(anorectalVergeDistanceCategory).isEqualTo(AnorectalVergeDistanceCategory.FIVE_TO_TEN_CM)
            assertThat(mesorectalFasciaIsClear).isTrue()
            assertThat(distanceToMesorectalFasciaMm).isNull()

            assertThat(differentiationGrade).isEqualTo(DifferentiationGrade.GRADE_2_OR_MODERATELY_DIFFERENTIATED)
            assertThat(clinicalTnmClassification).isEqualTo(
                TnmClassification(
                    tnmT = TnmT.T2,
                    tnmN = TnmN.N1,
                    tnmM = TnmM.M0
                )
            )
            assertThat(pathologicalTnmClassification).isEqualTo(
                TnmClassification(
                    tnmT = TnmT.T3,
                    tnmN = TnmN.N2,
                    tnmM = null
                )
            )
            assertThat(clinicalTumorStage).isEqualTo(TumorStage.II)
            assertThat(pathologicalTumorStage).isEqualTo(TumorStage.III)
            assertThat(investigatedLymphNodesCount).isEqualTo(3)
            assertThat(positiveLymphNodesCount).isEqualTo(1)

            assertThat(presentedWithIleus).isFalse()
            assertThat(presentedWithPerforation).isTrue()
            assertThat(venousInvasionDescription).isEqualTo(VenousInvasionDescription.SUSPECT)
            assertThat(lymphaticInvasionCategory).isEqualTo(LymphaticInvasionCategory.NA)
            assertThat(extraMuralInvasionCategory).isEqualTo(ExtraMuralInvasionCategory.ABOVE_FIVE_MM)
            assertThat(tumorRegression).isEqualTo(TumorRegression.MINIMAL_REGRESSION)
        }
    }
}