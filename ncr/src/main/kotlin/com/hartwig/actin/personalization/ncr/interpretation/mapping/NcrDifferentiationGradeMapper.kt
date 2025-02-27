package com.hartwig.actin.personalization.ncr.interpretation.mapping

import com.hartwig.actin.personalization.datamodel.diagnosis.DifferentiationGrade

object NcrDifferentiationGradeMapper : NcrIntCodeMapper<DifferentiationGrade?> {

    override fun resolve(code: Int): DifferentiationGrade? {
        return when (code) {
            1 -> DifferentiationGrade.GRADE_1_OR_WELL_DIFFERENTIATED
            2 -> DifferentiationGrade.GRADE_2_OR_MODERATELY_DIFFERENTIATED
            3 -> DifferentiationGrade.GRADE_3_OR_POORLY_DIFFERENTIATED
            4 -> DifferentiationGrade.GRADE_4_OR_UNDIFFERENTIATED_OR_ANAPLASTIC_OR_GGG4
            9 -> null
            else -> throw IllegalArgumentException("Unknown TumorDifferentiationGrade code: $code")
        }
    }
}
