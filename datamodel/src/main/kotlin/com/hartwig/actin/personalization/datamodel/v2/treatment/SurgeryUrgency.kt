package com.hartwig.actin.personalization.datamodel.v2.treatment

enum class SurgeryUrgency {
    ELECTIVE,
    URGENT_AT_LEAST_TWELVE_HOURS_BEFORE_PLANNED,
    URGENT_LESS_THAN_TWELVE_HOURS_PLANNED,
    PLACEMENT_STENT_OR_STOMA_LATER_FOLLOWED_BY_PLANNED_SURGERY,
}