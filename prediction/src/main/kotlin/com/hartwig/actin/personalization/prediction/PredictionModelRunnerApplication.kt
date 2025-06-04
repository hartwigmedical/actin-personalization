package com.hartwig.actin.personalization.prediction

import io.github.oshai.kotlinlogging.KotlinLogging

class PredictionModelRunnerApplication {

    private val logger = KotlinLogging.logger {}
    
    fun run() {
        logger.info { "Running prediction model runner application" }    
    }
}

fun main() {
    PredictionModelRunnerApplication().run()
} 