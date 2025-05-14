package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import io.github.oshai.kotlinlogging.KotlinLogging

object MetastasisDetection {

    private val LOGGER = KotlinLogging.logger {}

    fun determineDaysBetweenPrimaryAndMetastaticDiagnosis(entry: ReferenceEntry): Int? {
        val metastaticDiagnosis = entry.metastaticDiagnosis
        val earliestMetastasis = metastaticDiagnosis.metastases.filter { it.daysSinceDiagnosis != null }
            .sortedBy { it.daysSinceDiagnosis }.firstOrNull()

        if (earliestMetastasis == null) {
            if (metastaticDiagnosis.isMetachronous) {
                LOGGER.warn { "No metastases found with interval for metachronous entry with source ID ${entry.sourceId}" }
            } else {
                LOGGER.info { "No metastases found with interval for synchronous entry with source ID ${entry.sourceId}" }
            }
        } else {
            if (!metastaticDiagnosis.isMetachronous && earliestMetastasis.daysSinceDiagnosis!! > 0) {
                LOGGER.warn {
                    ("Synchronous entry with earliest metastasis interval after "
                            + "diagnosis for entry with source ID ${entry.sourceId}")
                }
            }
        }

        return if (!metastaticDiagnosis.isMetachronous) 0 else earliestMetastasis?.daysSinceDiagnosis
    }
}