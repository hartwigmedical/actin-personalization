package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasureType
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTreatment
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private const val SYSTEMIC_TREATMENT_START_DAYS = 150

class TreatmentInterpreterTest {

    private val preMetastaticTreatment = TestDatamodelFactory.treatmentEpisode(
        metastaticPresence = MetastaticPresence.ABSENT
    )

    private val noMetastaticTreatment = TestDatamodelFactory.treatmentEpisode(
        metastaticPresence = MetastaticPresence.AT_START,
        reasonRefrainmentFromTreatment = ReasonRefrainmentFromTreatment.WISH_OR_REFUSAL_FROM_PATIENT_OR_FAMILY
    )
    private val noMetastaticTreatmentInterpreter = TreatmentInterpreter(listOf(preMetastaticTreatment, noMetastaticTreatment))

    private val systemicOnlyMetastaticTreatment = TestDatamodelFactory.treatmentEpisode(
        metastaticPresence = MetastaticPresence.AT_START,
        reasonRefrainmentFromTreatment = ReasonRefrainmentFromTreatment.NOT_APPLICABLE,
        systemicTreatments = listOf(
            TestDatamodelFactory.systemicTreatment(
                treatment = Treatment.CAPOX,
                schemes = listOf(
                    listOf(
                        TestDatamodelFactory.drugTreatment(daysBetweenDiagnosisAndStart = null),
                        TestDatamodelFactory.drugTreatment(
                            daysBetweenDiagnosisAndStart = SYSTEMIC_TREATMENT_START_DAYS + 50,
                            daysBetweenDiagnosisAndStop = SYSTEMIC_TREATMENT_START_DAYS + 100
                        )
                    ),
                    listOf(
                        TestDatamodelFactory.drugTreatment(daysBetweenDiagnosisAndStart = SYSTEMIC_TREATMENT_START_DAYS),
                        TestDatamodelFactory.drugTreatment(daysBetweenDiagnosisAndStart = null)
                    )
                )
            )
        )
    )
    private val systemicOnlyMetastaticInterpreter = TreatmentInterpreter(listOf(preMetastaticTreatment, systemicOnlyMetastaticTreatment))

    @Test
    fun `Should determine timing of metastatic treatment`() {
        assertThat(noMetastaticTreatmentInterpreter.isMetastaticPriorToMetastaticTreatmentDecision()).isTrue()
        assertThat(systemicOnlyMetastaticInterpreter.isMetastaticPriorToMetastaticTreatmentDecision()).isTrue()

        val noMetastaticTreatmentInterpreter =
            TreatmentInterpreter(
                listOf(
                    TestDatamodelFactory.treatmentEpisode(metastaticPresence = MetastaticPresence.ABSENT),
                    TestDatamodelFactory.treatmentEpisode(metastaticPresence = MetastaticPresence.AT_PROGRESSION)
                )
            )
        assertThat(noMetastaticTreatmentInterpreter.isMetastaticPriorToMetastaticTreatmentDecision()).isFalse()
    }

    @Test
    fun `Should throw exception with two metastatic treatment episodes`() {
        val exceptionInterpreter = TreatmentInterpreter(listOf(noMetastaticTreatment, systemicOnlyMetastaticTreatment))
        assertThrows<IllegalStateException> { exceptionInterpreter.determineMetastaticSystemicTreatmentStart() }
    }

    @Test
    fun `Should determine start of metastatic systemic treatment`() {
        assertThat(noMetastaticTreatmentInterpreter.determineMetastaticSystemicTreatmentStart()).isNull()
        assertThat(systemicOnlyMetastaticInterpreter.determineMetastaticSystemicTreatmentStart()).isEqualTo(SYSTEMIC_TREATMENT_START_DAYS)
    }

    @Test
    fun `Should determine reason refrainment for treatment`() {
        assertThat(noMetastaticTreatmentInterpreter.reasonRefrainmentFromMetastaticTreatment())
            .isEqualTo(ReasonRefrainmentFromTreatment.WISH_OR_REFUSAL_FROM_PATIENT_OR_FAMILY.name)
        assertThat(systemicOnlyMetastaticInterpreter.reasonRefrainmentFromMetastaticTreatment())
            .isEqualTo(ReasonRefrainmentFromTreatment.NOT_APPLICABLE.name)
    }

    @Test
    fun `Should determine whether progression has occurred`() {
        assertThat(noMetastaticTreatmentInterpreter.hasProgressionEventAfterMetastaticSystemicTreatmentStart()).isNull()
        assertThat(systemicOnlyMetastaticInterpreter.hasProgressionEventAfterMetastaticSystemicTreatmentStart()).isFalse()
    }

    @Test
    fun `Should determine primary surgery timings`() {
        assertThat(noMetastaticTreatmentInterpreter.hasPrimarySurgeryDuringMetastaticTreatment()).isFalse()
        assertThat(noMetastaticTreatmentInterpreter.hasPrimarySurgeryDuringMetastaticTreatment()).isFalse()

        assertThat(systemicOnlyMetastaticInterpreter.hasPrimarySurgeryDuringMetastaticTreatment()).isFalse()
        assertThat(systemicOnlyMetastaticInterpreter.hasPrimarySurgeryDuringMetastaticTreatment()).isFalse()

        val preTreatmentPrimarySurgery1 = preMetastaticTreatment.copy(primarySurgeries = listOf(TestDatamodelFactory.primarySurgery()))
        val prePrimarySurgeryInterpreter1 = TreatmentInterpreter(listOf(preTreatmentPrimarySurgery1, systemicOnlyMetastaticTreatment))
        assertThat(prePrimarySurgeryInterpreter1.hasPrimarySurgeryPriorToMetastaticTreatment()).isTrue()
        assertThat(prePrimarySurgeryInterpreter1.hasPrimarySurgeryDuringMetastaticTreatment()).isFalse()

        val unknownTreatmentPrimarySurgery =
            systemicOnlyMetastaticTreatment.copy(primarySurgeries = listOf(TestDatamodelFactory.primarySurgery(daysSinceDiagnosis = null)))
        val unknownPrimarySurgeryInterpreter = TreatmentInterpreter(listOf(unknownTreatmentPrimarySurgery))
        assertThat(unknownPrimarySurgeryInterpreter.hasPrimarySurgeryPriorToMetastaticTreatment()).isTrue()
        assertThat(unknownPrimarySurgeryInterpreter.hasPrimarySurgeryDuringMetastaticTreatment()).isTrue()

        val preTreatmentPrimarySurgery2 =
            systemicOnlyMetastaticTreatment.copy(
                primarySurgeries = listOf(TestDatamodelFactory.primarySurgery(daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS - 50))
            )
        val prePrimarySurgeryInterpreter2 = TreatmentInterpreter(listOf(preTreatmentPrimarySurgery2))
        assertThat(prePrimarySurgeryInterpreter2.hasPrimarySurgeryPriorToMetastaticTreatment()).isTrue()
        assertThat(prePrimarySurgeryInterpreter2.hasPrimarySurgeryDuringMetastaticTreatment()).isFalse()

        val surgeryDuringTreatment =
            systemicOnlyMetastaticTreatment.copy(
                primarySurgeries = listOf(
                    TestDatamodelFactory.primarySurgery(daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS + 50)
                )
            )
        val duringPrimarySurgeryInterpreter = TreatmentInterpreter(listOf(surgeryDuringTreatment))
        assertThat(duringPrimarySurgeryInterpreter.hasPrimarySurgeryPriorToMetastaticTreatment()).isFalse()
        assertThat(duringPrimarySurgeryInterpreter.hasPrimarySurgeryDuringMetastaticTreatment()).isTrue()
    }

    @Test
    fun `Should determine gastroenterology surgery timings`() {
        assertThat(noMetastaticTreatmentInterpreter.hasGastroenterologySurgeryPriorToMetastaticTreatment()).isFalse()
        assertThat(noMetastaticTreatmentInterpreter.hasGastroenterologySurgeryDuringMetastaticTreatment()).isFalse()

        assertThat(systemicOnlyMetastaticInterpreter.hasGastroenterologySurgeryPriorToMetastaticTreatment()).isFalse()
        assertThat(systemicOnlyMetastaticInterpreter.hasGastroenterologySurgeryDuringMetastaticTreatment()).isFalse()

        val gastroenterologySurgeryDuringTreatment =
            systemicOnlyMetastaticTreatment.copy(
                gastroenterologyResections = listOf(
                    TestDatamodelFactory.gastroenterologyResection(daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS + 50)
                )
            )
        val duringGastroenterologySurgeryInterpreter = TreatmentInterpreter(listOf(gastroenterologySurgeryDuringTreatment))
        assertThat(duringGastroenterologySurgeryInterpreter.hasGastroenterologySurgeryPriorToMetastaticTreatment()).isFalse()
        assertThat(duringGastroenterologySurgeryInterpreter.hasGastroenterologySurgeryDuringMetastaticTreatment()).isTrue()
    }

    @Test
    fun `Should determine hipec timings`() {
        assertThat(noMetastaticTreatmentInterpreter.hasHipecPriorToMetastaticTreatment()).isFalse()
        assertThat(noMetastaticTreatmentInterpreter.hasHipecDuringMetastaticTreatment()).isFalse()

        assertThat(systemicOnlyMetastaticInterpreter.hasHipecPriorToMetastaticTreatment()).isFalse()
        assertThat(systemicOnlyMetastaticInterpreter.hasHipecDuringMetastaticTreatment()).isFalse()

        val hipecDuringTreatment =
            systemicOnlyMetastaticTreatment.copy(
                hipecTreatments = listOf(
                    TestDatamodelFactory.hipecTreatment(daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS - 50),
                    TestDatamodelFactory.hipecTreatment(daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS + 50)
                )
            )
        val duringHipecInterpreter = TreatmentInterpreter(listOf(hipecDuringTreatment))
        assertThat(duringHipecInterpreter.hasHipecPriorToMetastaticTreatment()).isTrue()
        assertThat(duringHipecInterpreter.hasHipecDuringMetastaticTreatment()).isTrue()
    }

    @Test
    fun `Should determine primary radiotherapy timings`() {
        assertThat(noMetastaticTreatmentInterpreter.hasPrimaryRadiotherapyPriorToMetastaticTreatment()).isFalse()
        assertThat(noMetastaticTreatmentInterpreter.hasPrimaryRadiotherapyDuringMetastaticTreatment()).isFalse()

        assertThat(systemicOnlyMetastaticInterpreter.hasPrimaryRadiotherapyPriorToMetastaticTreatment()).isFalse()
        assertThat(systemicOnlyMetastaticInterpreter.hasPrimaryRadiotherapyDuringMetastaticTreatment()).isFalse()

        val primaryRadiotherapyDuringTreatment =
            systemicOnlyMetastaticTreatment.copy(
                primaryRadiotherapies = listOf(
                    TestDatamodelFactory.primaryRadiotherapy(
                        daysBetweenDiagnosisAndStart = null,
                        daysBetweenDiagnosisAndStop = SYSTEMIC_TREATMENT_START_DAYS - 50
                    ),
                    TestDatamodelFactory.primaryRadiotherapy(
                        daysBetweenDiagnosisAndStart = SYSTEMIC_TREATMENT_START_DAYS - 50,
                        daysBetweenDiagnosisAndStop = SYSTEMIC_TREATMENT_START_DAYS + 50
                    )
                )
            )
        val duringPrimaryRadiotherapyInterpreter = TreatmentInterpreter(listOf(primaryRadiotherapyDuringTreatment))
        assertThat(duringPrimaryRadiotherapyInterpreter.hasPrimaryRadiotherapyPriorToMetastaticTreatment()).isTrue()
        assertThat(duringPrimaryRadiotherapyInterpreter.hasPrimaryRadiotherapyDuringMetastaticTreatment()).isTrue()
    }

    @Test
    fun `Should determine other systemic therapy timings`() {
        assertThat(noMetastaticTreatmentInterpreter.hasSystemicTreatmentPriorToMetastaticTreatment()).isFalse()
        assertThat(systemicOnlyMetastaticInterpreter.hasSystemicTreatmentPriorToMetastaticTreatment()).isFalse()

        val preSystemicTreatment = preMetastaticTreatment.copy(systemicTreatments = listOf(TestDatamodelFactory.systemicTreatment()))
        val preSystemicInterpreter = TreatmentInterpreter(listOf(preSystemicTreatment, systemicOnlyMetastaticTreatment))
        assertThat(preSystemicInterpreter.hasSystemicTreatmentPriorToMetastaticTreatment()).isTrue()
    }

    @Test
    fun `Should determine metastatic surgery timings`() {
        assertThat(noMetastaticTreatmentInterpreter.hasMetastaticSurgery()).isFalse()
        assertThat(systemicOnlyMetastaticInterpreter.hasMetastaticSurgery()).isFalse()

        val metastaticSurgeryTreatment =
            systemicOnlyMetastaticTreatment.copy(metastaticSurgeries = listOf(TestDatamodelFactory.metastaticSurgery()))
        val metastaticSurgeryInterpreter = TreatmentInterpreter(listOf(metastaticSurgeryTreatment))
        assertThat(metastaticSurgeryInterpreter.hasMetastaticSurgery()).isTrue()
    }

    @Test
    fun `Should determine metastatic radiotherapy timings`() {
        assertThat(noMetastaticTreatmentInterpreter.hasMetastaticRadiotherapy()).isFalse()
        assertThat(systemicOnlyMetastaticInterpreter.hasMetastaticRadiotherapy()).isFalse()

        val metastaticRadiotherapyTreatment =
            systemicOnlyMetastaticTreatment.copy(metastaticRadiotherapies = listOf(TestDatamodelFactory.metastaticRadiotherapy()))
        val metastaticRadiotherapyInterpreter = TreatmentInterpreter(listOf(metastaticRadiotherapyTreatment))
        assertThat(metastaticRadiotherapyInterpreter.hasMetastaticRadiotherapy()).isTrue()
    }

    @Test
    fun `Should determine number of metastatic systemic treatments`() {
        assertThat(noMetastaticTreatmentInterpreter.metastaticSystemicTreatmentCount()).isEqualTo(0)
        assertThat(systemicOnlyMetastaticInterpreter.metastaticSystemicTreatmentCount()).isEqualTo(1)
    }

    @Test
    fun `Should determine first metastatic systemic treatment`() {
        assertThat(noMetastaticTreatmentInterpreter.firstMetastaticSystemicTreatment()).isNull()
        assertThat(noMetastaticTreatmentInterpreter.firstMetastaticSystemicTreatmentGroup()).isNull()

        assertThat(systemicOnlyMetastaticInterpreter.firstMetastaticSystemicTreatment()).isEqualTo(Treatment.CAPOX)
        assertThat(systemicOnlyMetastaticInterpreter.firstMetastaticSystemicTreatmentGroup()).isEqualTo(TreatmentGroup.CAPOX_OR_FOLFOX)
    }

    @Test
    fun `Should determine systemic treatment duration days`() {
        assertThat(noMetastaticTreatmentInterpreter.firstMetastaticSystemicTreatmentDurationDays()).isNull()
        assertThat(systemicOnlyMetastaticInterpreter.firstMetastaticSystemicTreatmentDurationDays()).isEqualTo(101)

        val treatmentWithoutEnd = noMetastaticTreatment.copy(
            systemicTreatments = listOf(
                TestDatamodelFactory.systemicTreatment(
                    schemes = listOf(
                        listOf(TestDatamodelFactory.drugTreatment(daysBetweenDiagnosisAndStart = 100, daysBetweenDiagnosisAndStop = null))
                    )
                )
            )
        )
        
        val interpreterWithoutEnd = TreatmentInterpreter(listOf(treatmentWithoutEnd))
        assertThat(interpreterWithoutEnd.firstMetastaticSystemicTreatmentDurationDays()).isNull()
    }

    @Test
    fun `Can determine whether has post metastatic treatment with systemic treatment only`() {
        assertThat(noMetastaticTreatmentInterpreter.hasPostMetastaticTreatmentWithSystemicTreatmentOnly()).isFalse()
        assertThat(systemicOnlyMetastaticInterpreter.hasPostMetastaticTreatmentWithSystemicTreatmentOnly()).isTrue()
    }

    @Test
    fun `Should determine progression event timings`() {
        assertThat(noMetastaticTreatmentInterpreter.hasProgressionEventAfterMetastaticSystemicTreatmentStart()).isNull()
        assertThat(noMetastaticTreatmentInterpreter.daysBetweenProgressionAndMetastaticSystemicTreatmentStart()).isNull()
        assertThat(systemicOnlyMetastaticInterpreter.hasProgressionEventAfterMetastaticSystemicTreatmentStart()).isFalse()
        assertThat(systemicOnlyMetastaticInterpreter.daysBetweenProgressionAndMetastaticSystemicTreatmentStart()).isNull()

        val progressionPriorToTreatment =
            systemicOnlyMetastaticTreatment.copy(
                progressionMeasures = listOf(
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS - 10)
                )
            )

        val progressionPriorToTreatmentInterpreter = TreatmentInterpreter(listOf(progressionPriorToTreatment))
        assertThat(progressionPriorToTreatmentInterpreter.hasProgressionEventAfterMetastaticSystemicTreatmentStart()).isFalse()
        assertThat(progressionPriorToTreatmentInterpreter.daysBetweenProgressionAndMetastaticSystemicTreatmentStart()).isNull()

        val progressionPostToTreatment =
            systemicOnlyMetastaticTreatment.copy(
                progressionMeasures = listOf(
                    TestDatamodelFactory.progressionMeasure(daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS + 10)
                )
            )

        val progressionPostToTreatmentInterpreter = TreatmentInterpreter(listOf(progressionPostToTreatment))
        assertThat(progressionPostToTreatmentInterpreter.hasProgressionEventAfterMetastaticSystemicTreatmentStart()).isTrue()
        assertThat(progressionPostToTreatmentInterpreter.daysBetweenProgressionAndMetastaticSystemicTreatmentStart()).isEqualTo(10)
    }

    @Test
    fun `Should return first progression measure when multiple after treatment start`() {
        val treatmentWithMultipleProgression =
            systemicOnlyMetastaticTreatment.copy(
                progressionMeasures = listOf(
                    TestDatamodelFactory.progressionMeasure(
                        daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS + 15,
                        type = ProgressionMeasureType.PROGRESSION
                    ),
                    TestDatamodelFactory.progressionMeasure(
                        daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS + 18,
                        type = ProgressionMeasureType.PROGRESSION
                    ),
                    TestDatamodelFactory.progressionMeasure(
                        daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS + 12,
                        type = ProgressionMeasureType.PROGRESSION
                    ),
                    TestDatamodelFactory.progressionMeasure(
                        daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS + 11,
                        type = ProgressionMeasureType.CENSOR
                    ),
                    TestDatamodelFactory.progressionMeasure(
                        daysSinceDiagnosis = SYSTEMIC_TREATMENT_START_DAYS - 8,
                        type = ProgressionMeasureType.PROGRESSION
                    )
                )
            )

        val interpreter = TreatmentInterpreter(listOf(treatmentWithMultipleProgression))

        assertThat(interpreter.daysBetweenProgressionAndPrimaryDiagnosis()).isEqualTo(SYSTEMIC_TREATMENT_START_DAYS + 12)
        assertThat(interpreter.daysBetweenProgressionAndMetastaticSystemicTreatmentStart()).isEqualTo(12)
    }
}