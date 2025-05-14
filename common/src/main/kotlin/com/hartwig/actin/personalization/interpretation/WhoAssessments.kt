package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment

object WhoAssessments {

    fun statusAtMetastaticDiagnosis(whoAssessments: List<WhoAssessment>, daysBetweenPrimaryAndMetastaticDiagnosis: Int): Int? {
        return whoAssessments
            .filter { it.daysSinceDiagnosis <= daysBetweenPrimaryAndMetastaticDiagnosis }
            .maxByOrNull { it.daysSinceDiagnosis }?.whoStatus
    }
}