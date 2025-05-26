package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.diagnosis.LocationGroup
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis

class MetastaticInterpreter(private val metastaticDiagnosis: MetastaticDiagnosis) {

    fun daysBetweenPrimaryAndMetastaticDiagnosis(): Int? {
        val earliestMetastasis = metastaticDiagnosis.metastases.filter { it.daysSinceDiagnosis != null }
            .sortedBy { it.daysSinceDiagnosis }.firstOrNull()

        return if (!metastaticDiagnosis.isMetachronous) 0 else earliestMetastasis?.daysSinceDiagnosis
    }

    fun hasLiverOrIntrahepaticBileDuctMetastases(): Boolean {
        return metastaticDiagnosis.metastases.any { it.location.locationGroup == LocationGroup.LIVER_AND_INTRAHEPATIC_BILE_DUCTS }
    }

    fun hasLymphNodeMetastases(): Boolean {
        return metastaticDiagnosis.metastases.any { it.location.locationGroup == LocationGroup.LYMPH_NODES }
    }

    fun hasPeritonealMetastases(): Boolean {
        return metastaticDiagnosis.metastases.any { it.location.locationGroup == LocationGroup.PERITONEUM }
    }

    fun hasBronchusOrLungMetastases(): Boolean {
        return metastaticDiagnosis.metastases.any { it.location.locationGroup == LocationGroup.BRONCHUS_AND_LUNG }
    }

    fun hasBrainMetastases(): Boolean {
        return metastaticDiagnosis.metastases.any { it.location.locationGroup == LocationGroup.BRAIN }
    }

    fun hasOtherMetastases(): Boolean {
        val groupsToExcludeForOther = setOf(
            LocationGroup.LIVER_AND_INTRAHEPATIC_BILE_DUCTS,
            LocationGroup.LYMPH_NODES,
            LocationGroup.PERITONEUM,
            LocationGroup.BRONCHUS_AND_LUNG,
            LocationGroup.BRAIN
        )
        
        return metastaticDiagnosis.metastases.any { !groupsToExcludeForOther.contains(it.location.locationGroup) }
    }
}