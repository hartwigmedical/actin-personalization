package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.diagnosis.LocationGroup
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis

object Metastases {

    fun daysBetweenPrimaryAndMetastaticDiagnosis(metastaticDiagnosis: MetastaticDiagnosis): Int? {
        val earliestMetastasis = metastaticDiagnosis.metastases.filter { it.daysSinceDiagnosis != null }
            .sortedBy { it.daysSinceDiagnosis }.firstOrNull()

        return if (!metastaticDiagnosis.isMetachronous) 0 else earliestMetastasis?.daysSinceDiagnosis
    }

    fun hasLiverOrIntrahepaticBileDuctMetastases(metastaticDiagnosis: MetastaticDiagnosis): Boolean {
        return metastaticDiagnosis.metastases.any { it.location.locationGroup == LocationGroup.LIVER_AND_INTRAHEPATIC_BILE_DUCTS }
    }

    fun hasLymphNodeMetastases(metastaticDiagnosis: MetastaticDiagnosis): Boolean {
        return metastaticDiagnosis.metastases.any { it.location.locationGroup == LocationGroup.LYMPH_NODES }
    }

    fun hasPeritonealMetastases(metastaticDiagnosis: MetastaticDiagnosis): Boolean {
        return metastaticDiagnosis.metastases.any { it.location.locationGroup == LocationGroup.PERITONEUM }
    }

    fun hasBronchusOrLungMetastases(metastaticDiagnosis: MetastaticDiagnosis): Boolean {
        return metastaticDiagnosis.metastases.any { it.location.locationGroup == LocationGroup.BRONCHUS_AND_LUNG }
    }

    fun hasBrainMetastases(metastaticDiagnosis: MetastaticDiagnosis): Boolean {
        return metastaticDiagnosis.metastases.any { it.location.locationGroup == LocationGroup.BRAIN }
    }

    fun hasOtherMetastases(metastaticDiagnosis: MetastaticDiagnosis): Boolean {
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