package com.hartwig.actin.personalization.datamodel

enum class LabMeasure(val unit: LabMeasureUnit) {
    ALBUMINE(LabMeasureUnit.GRAM_PER_LITER),
    ALKALINE_PHOSPHATASE(LabMeasureUnit.UNIT_PER_LITER),
    CARCINOEMBRYONIC_ANTIGEN(LabMeasureUnit.MICROGRAM_PER_LITER),
    LACTATE_DEHYDROGENASE(LabMeasureUnit.UNIT_PER_LITER),
    LEUKOCYTES_ABSOLUTE(LabMeasureUnit.BILLIONS_PER_LITER),
    NEUTROPHILS_ABSOLUTE(LabMeasureUnit.BILLIONS_PER_LITER)
}