package com.hartwig.actin.personalization.prediction

import ai.djl.repository.zoo.Criteria
import com.hartwig.actin.personalization.prediction.datamodel.PredictorInput
import com.hartwig.actin.personalization.prediction.datamodel.PredictorOutput
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.io.path.Path

class PredictionModelRunnerApplication {

    private val logger = KotlinLogging.logger {}

    fun run() {
        logger.info { "Running prediction model runner application" }

        val path = Path(System.getProperty("user.home") + "/hmf/tmp/trained_models/treatment_drug/OS_DeepHitModel.pt")

        val criteria = Criteria.builder()
            .setTypes(PredictorInput::class.java, PredictorOutput::class.java)
            .optModelPath(path)
            .build()
        
        val model = criteria.loadModel()
        
        val predictor = model.newPredictor()

        val input = PredictorInput()
        val output = predictor.predict(input)
        logger.info { output }
    }
}

fun main() {
    PredictionModelRunnerApplication().run()
} 