package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import com.hartwig.actin.personalization.similarity.patientWithTreatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PatientPopulationBreakdownTest {

    @Test
    fun `Should analyze treatments for each sub-population`() {
        val fluorouracilPatient = patientWithTreatment(
            treatment = Treatment.FLUOROURACIL,
            pfsDays = 70,
            osDays = 300,
            hadSurvivalEvent = true,
            hadProgressionEvent = true
        )
        val capecitabinePatient = patientWithTreatment(
            treatment = Treatment.CAPECITABINE,
            pfsDays = null,
            osDays = 350,
            hadSurvivalEvent = true,
            hadProgressionEvent = true
        )
        val capoxPatient = patientWithTreatment(
            treatment = Treatment.CAPOX,
            pfsDays = 100,
            osDays = 400,
            hadSurvivalEvent = true,
            hadProgressionEvent = true,
            ageAtDiagnosis = 85
        )
        val patientsByTreatment = listOf(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(fluorouracilPatient, capecitabinePatient),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(capoxPatient)
        )
        val ageSubPopulation = "Age 45-55"
        val populationDefinitions = listOf(
            PopulationDefinition(ALL_PATIENTS_POPULATION_NAME) { true },
            PopulationDefinition(ageSubPopulation) { it.diagnosis.ageAtDiagnosis in 45..55 }
        )
        val measurementTypes = listOf(
            MeasurementType.TREATMENT_DECISION,
            MeasurementType.PROGRESSION_FREE_SURVIVAL,
            MeasurementType.OVERALL_SURVIVAL
        )
        val analysis = PatientPopulationBreakdown(patientsByTreatment, populationDefinitions, measurementTypes).analyze()

        assertThat(analysis.populations).containsExactlyInAnyOrder(
            Population(
                ALL_PATIENTS_POPULATION_NAME, mapOf(
                    MeasurementType.TREATMENT_DECISION to listOf(fluorouracilPatient, capecitabinePatient, capoxPatient),
                    MeasurementType.PROGRESSION_FREE_SURVIVAL to listOf(fluorouracilPatient, capoxPatient),
                    MeasurementType.OVERALL_SURVIVAL to listOf(fluorouracilPatient, capecitabinePatient, capoxPatient)
                )
            ),
            Population(
                ageSubPopulation, mapOf(
                    MeasurementType.TREATMENT_DECISION to listOf(fluorouracilPatient, capecitabinePatient),
                    MeasurementType.PROGRESSION_FREE_SURVIVAL to listOf(fluorouracilPatient),
                    MeasurementType.OVERALL_SURVIVAL to listOf(fluorouracilPatient, capecitabinePatient)
                )
            )
        )

        assertThat(analysis.treatmentAnalyses).containsExactlyInAnyOrder(
            TreatmentAnalysis(
                TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL, mapOf(
                    MeasurementType.TREATMENT_DECISION to mapOf(
                        ALL_PATIENTS_POPULATION_NAME to Measurement(2.0 / 3, 2),
                        ageSubPopulation to Measurement(2.0 / 2, 2)
                    ),
                    MeasurementType.PROGRESSION_FREE_SURVIVAL to mapOf(
                        ALL_PATIENTS_POPULATION_NAME to Measurement(70.0, 1, 70, 70, 0.0),
                        ageSubPopulation to Measurement(70.0, 1, 70, 70, 0.0)
                    ),
                    MeasurementType.OVERALL_SURVIVAL to mapOf(
                        ALL_PATIENTS_POPULATION_NAME to Measurement(300.0, 2, 300, 350, 50.0),
                        ageSubPopulation to Measurement(300.0, 2, 300, 350, 50.0)
                    )
                )
            ),
            TreatmentAnalysis(
                TreatmentGroup.CAPOX_OR_FOLFOX, mapOf(
                    MeasurementType.TREATMENT_DECISION to mapOf(
                        ALL_PATIENTS_POPULATION_NAME to Measurement(1.0 / 3, 1),
                        ageSubPopulation to Measurement(0.0, 0)
                    ),
                    MeasurementType.PROGRESSION_FREE_SURVIVAL to mapOf(
                        ALL_PATIENTS_POPULATION_NAME to Measurement(100.0, 1, 100, 100, 0.0),
                        ageSubPopulation to Measurement(Double.NaN, 0, null, null, Double.NaN)
                    ),
                    MeasurementType.OVERALL_SURVIVAL to mapOf(
                        ALL_PATIENTS_POPULATION_NAME to Measurement(400.0, 1, 400, 400, 0.0),
                        ageSubPopulation to Measurement(Double.NaN, 0, null, null, Double.NaN)
                    )
                )
            )
        )
    }
}
