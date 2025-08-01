package com.hartwig.actin.personalization.datamodel.assessment

enum class LabMeasure(val unit: Unit, val upperBound: Double) {
    ALBUMINE(Unit.GRAM_PER_LITER, 70.0),
    ALKALINE_PHOSPHATASE(Unit.UNIT_PER_LITER, 1500.0),
    CARCINOEMBRYONIC_ANTIGEN(Unit.MICROGRAM_PER_LITER, 10000.0),
    LACTATE_DEHYDROGENASE(Unit.UNIT_PER_LITER, 3000.0),
    LEUKOCYTES_ABSOLUTE(Unit.BILLIONS_PER_LITER, 100.0),
    NEUTROPHILS_ABSOLUTE(Unit.BILLIONS_PER_LITER, 50.0)
}
