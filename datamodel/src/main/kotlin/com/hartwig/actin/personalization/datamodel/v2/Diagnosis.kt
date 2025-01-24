package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.AnorectalVergeDistanceCategory
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.Sidedness
import com.hartwig.actin.personalization.datamodel.TumorType

data class Diagnosis(
    val ageAtDiagnosis: Int,

    val consolidatedTumorType: TumorType,
    val tumorLocations: Set<Location>,
    val sidedness: Sidedness? = determineSidedness(tumorLocations),
    val isMetachronous: Boolean,


    val presentedWithIleus: Boolean? = null,
    val presentedWithPerforation: Boolean? = null,
    val anorectalVergeDistanceCategory: AnorectalVergeDistanceCategory? = null,
)

private fun determineSidedness(locations: Set<Location>): Sidedness? {
    val LOCATIONS_INDICATING_LEFT_SIDEDNESS =
        setOf(Location.FLEXURA_LIENALIS, Location.DESCENDING_COLON, Location.RECTOSIGMOID, Location.SIGMOID_COLON, Location.RECTUM)
    val LOCATIONS_INDICATING_RIGHT_SIDEDNESS =
        setOf(Location.APPENDIX, Location.COECUM, Location.ASCENDING_COLON, Location.FLEXURA_HEPATICA)

    val containsLeft = locations.any { it in LOCATIONS_INDICATING_LEFT_SIDEDNESS }
    val containsRight = locations.any { it in LOCATIONS_INDICATING_RIGHT_SIDEDNESS }

    return when {
        containsLeft && !containsRight -> Sidedness.LEFT
        containsRight && !containsLeft -> Sidedness.RIGHT
        else -> null
    }
}
