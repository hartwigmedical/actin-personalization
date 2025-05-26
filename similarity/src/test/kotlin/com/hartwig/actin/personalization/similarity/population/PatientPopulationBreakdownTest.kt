package com.hartwig.actin.personalization.similarity.population

import com.hartwig.actin.personalization.datamodel.TestDatamodelFactory
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PatientPopulationBreakdownTest {

    @Test
    fun `Should analyze treatments for each sub-population`() {
        val fluorouracilEntry = TestDatamodelFactory.entry(
            ageAtDiagnosis = 50,
            isAlive = false,
            daysBetweenDiagnosisAndSurvivalMeasurement = 300,
            systemicTreatment = Treatment.FLUOROURACIL,
            daysBetweenDiagnosisAndProgression = 70,
            hasProgressionEvent = true
        )
        val capecitabineEntry = TestDatamodelFactory.entry(
            ageAtDiagnosis = 50,
            isAlive = false,
            daysBetweenDiagnosisAndSurvivalMeasurement = 350,
            systemicTreatment = Treatment.CAPECITABINE,
            hasProgressionEvent = true,
            daysBetweenDiagnosisAndProgression = null
        )
        val capoxEntry = TestDatamodelFactory.entry(
            ageAtDiagnosis = 85,
            isAlive = false,
            daysBetweenDiagnosisAndSurvivalMeasurement = 400,
            systemicTreatment = Treatment.CAPOX,
            hasProgressionEvent = true,
            daysBetweenDiagnosisAndProgression = 100
        )
        val entriesByTreatment = listOf(
            TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL to listOf(fluorouracilEntry, capecitabineEntry),
            TreatmentGroup.CAPOX_OR_FOLFOX to listOf(capoxEntry)
        )
        val ageSubPopulation = "Age 45-55"
        val populationDefinitions = listOf(
            PopulationDefinition(ALL_PATIENTS_POPULATION_NAME) { true },
            PopulationDefinition(ageSubPopulation) { it.ageAtDiagnosis in 45..55 }
        )
        val measurementTypes = listOf(
            MeasurementType.TREATMENT_DECISION,
            MeasurementType.PROGRESSION_FREE_SURVIVAL,
            MeasurementType.OVERALL_SURVIVAL
        )
        val analysis = PatientPopulationBreakdown(entriesByTreatment, populationDefinitions, measurementTypes).analyze()

        assertThat(analysis.populations).containsExactlyInAnyOrder(
            Population(
                ALL_PATIENTS_POPULATION_NAME, mapOf(
                    MeasurementType.TREATMENT_DECISION to listOf(fluorouracilEntry, capecitabineEntry, capoxEntry),
                    MeasurementType.PROGRESSION_FREE_SURVIVAL to listOf(fluorouracilEntry, capoxEntry),
                    MeasurementType.OVERALL_SURVIVAL to listOf(fluorouracilEntry, capecitabineEntry, capoxEntry)
                )
            ),
            Population(
                ageSubPopulation, mapOf(
                    MeasurementType.TREATMENT_DECISION to listOf(fluorouracilEntry, capecitabineEntry),
                    MeasurementType.PROGRESSION_FREE_SURVIVAL to listOf(fluorouracilEntry),
                    MeasurementType.OVERALL_SURVIVAL to listOf(fluorouracilEntry, capecitabineEntry)
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
