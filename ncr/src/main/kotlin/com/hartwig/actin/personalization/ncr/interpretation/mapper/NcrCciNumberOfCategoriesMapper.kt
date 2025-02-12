package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.old.NumberOfCciCategories

object NcrCciNumberOfCategoriesMapper : NcrIntCodeMapper<NumberOfCciCategories> {

    override fun resolve(code: Int): NumberOfCciCategories {
        return when (code) {
            0 -> NumberOfCciCategories.ZERO_CATEGORIES
            1 -> NumberOfCciCategories.ONE_CATEGORY
            2 -> NumberOfCciCategories.TWO_OR_MORE_CATEGORIES
            else -> throw IllegalArgumentException("Unknown CciNumberOfCategories code: $code")
        }
    }
}
