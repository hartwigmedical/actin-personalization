package com.hartwig.actin.personalization.interpretation

import com.hartwig.actin.personalization.datamodel.assessment.LabMeasure
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasurement

object LabMeasurements {

    fun lactateDehydrogenaseAtMetastaticDiagnosis(
        labMeasurements: List<LabMeasurement>,
        daysBetweenPrimaryAndMetastaticDiagnosis: Int
    ): Double? {
        return closestBeforeInterval(labMeasurements, LabMeasure.LACTATE_DEHYDROGENASE, daysBetweenPrimaryAndMetastaticDiagnosis)
    }

    fun alkalinePhosphataseAtMetastaticDiagnosis(
        labMeasurements: List<LabMeasurement>,
        daysBetweenPrimaryAndMetastaticDiagnosis: Int
    ): Double? {
        return closestBeforeInterval(labMeasurements, LabMeasure.ALKALINE_PHOSPHATASE, daysBetweenPrimaryAndMetastaticDiagnosis)
    }

    fun leukocytesAbsoluteAtMetastaticDiagnosis(
        labMeasurements: List<LabMeasurement>,
        daysBetweenPrimaryAndMetastaticDiagnosis: Int
    ): Double? {
        return closestBeforeInterval(labMeasurements, LabMeasure.LEUKOCYTES_ABSOLUTE, daysBetweenPrimaryAndMetastaticDiagnosis)
    }

    fun carcinoembryonicAntigenAtMetastaticDiagnosis(
        labMeasurements: List<LabMeasurement>,
        daysBetweenPrimaryAndMetastaticDiagnosis: Int
    ): Double? {
        return closestBeforeInterval(labMeasurements, LabMeasure.CARCINOEMBRYONIC_ANTIGEN, daysBetweenPrimaryAndMetastaticDiagnosis)
    }

    fun albumineAtMetastaticDiagnosis(
        labMeasurements: List<LabMeasurement>,
        daysBetweenPrimaryAndMetastaticDiagnosis: Int
    ): Double? {
        return closestBeforeInterval(labMeasurements, LabMeasure.ALBUMINE, daysBetweenPrimaryAndMetastaticDiagnosis)
    }

    fun neutrophilsAbsoluteAtMetastaticDiagnosis(
        labMeasurements: List<LabMeasurement>,
        daysBetweenPrimaryAndMetastaticDiagnosis: Int
    ): Double? {
        return closestBeforeInterval(labMeasurements, LabMeasure.NEUTROPHILS_ABSOLUTE, daysBetweenPrimaryAndMetastaticDiagnosis)
    }
    
    private fun closestBeforeInterval(
        labMeasurements: List<LabMeasurement>,
        measure: LabMeasure,
        daysBetweenPrimaryAndMetastaticDiagnosis: Int
    ): Double? {
        return labMeasurements
            .filter { it.name == measure && it.daysSinceDiagnosis <= daysBetweenPrimaryAndMetastaticDiagnosis }
            .maxByOrNull { it.daysSinceDiagnosis }?.value
    }
}