package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification

class AsaInterpreter(private val asaAssessments : List<AsaAssessment>) {

    fun mostRecentClassificationPriorTo(maxDaysSinceDiagnosis: Int): AsaClassification? {
        return asaAssessments
            .filter { it.daysSinceDiagnosis <= maxDaysSinceDiagnosis }
            .maxByOrNull { it.daysSinceDiagnosis }?.classification
    }
}