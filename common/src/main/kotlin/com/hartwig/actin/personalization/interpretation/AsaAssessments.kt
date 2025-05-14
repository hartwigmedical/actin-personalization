package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.AsaClassification

object AsaAssessments {

    fun classificationAtMetastaticDiagnosis(
        asaAssessments: List<AsaAssessment>,
        daysBetweenPrimaryAndMetastaticDiagnosis: Int
    ): AsaClassification? {
        return asaAssessments
            .filter { it.daysSinceDiagnosis <= daysBetweenPrimaryAndMetastaticDiagnosis }
            .maxByOrNull { it.daysSinceDiagnosis }?.classification
    }
}