package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.v2.diagnosis.TumorRegression

object NcrTumorRegressionMapper : NcrIntCodeMapper<TumorRegression?> {

    override fun resolve(code: Int): TumorRegression? {
        return when (code) {
            0 -> TumorRegression.CANNOT_BE_DETERMINED
            1 -> TumorRegression.FULL_REGRESSION
            2 -> TumorRegression.MINIMAL_FOCI
            3 -> TumorRegression.MODERATE_REGRESSION
            4 -> TumorRegression.MINIMAL_REGRESSION
            5 -> TumorRegression.NO_SIGNS_OF_REGRESSION
            8 -> TumorRegression.NA
            9 -> null
            else -> throw IllegalArgumentException("Unknown TumorRegression code: $code")
        }
    }
}
