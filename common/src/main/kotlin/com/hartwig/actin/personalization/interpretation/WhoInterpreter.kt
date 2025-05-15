package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment

class WhoInterpreter(private val whoAssessments: List<WhoAssessment>) {

    fun mostRecentStatusPriorTo(maxDaysSinceDiagnosis: Int): Int? {
        return whoAssessments
            .filter { it.daysSinceDiagnosis <= maxDaysSinceDiagnosis }
            .maxByOrNull { it.daysSinceDiagnosis }?.whoStatus
    }
}