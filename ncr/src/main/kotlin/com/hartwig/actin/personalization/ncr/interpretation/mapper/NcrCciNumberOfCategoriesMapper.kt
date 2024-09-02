package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.NumberOfCategories

object NcrCciNumberOfCategoriesMapper : NcrIntCodeMapper<NumberOfCategories> {

    override fun resolve(code: Int): NumberOfCategories {
        return when (code) {
            0 -> NumberOfCategories.ZERO_CATEGORIES
            1 -> NumberOfCategories.ONE_CATEGORY
            2 -> NumberOfCategories.TWO_OR_MORE_CATEGORIES
            else -> throw IllegalArgumentException("Unknown CciNumberOfCategories code: $code")
        }
    }
}
