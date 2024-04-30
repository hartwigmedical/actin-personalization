package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.CciNumberOfCategories

object NcrCciNumberOfCategoriesMapper : NcrIntCodeMapper<CciNumberOfCategories> {

    override fun resolve(code: Int): CciNumberOfCategories {
        return when (code) {
            0 -> CciNumberOfCategories.ZERO_CATEGORIES
            1 -> CciNumberOfCategories.ONE_CATEGORY
            2 -> CciNumberOfCategories.TWO_OR_MORE_CATEGORIES
            else -> throw IllegalArgumentException("Unknown CciNumberOfCategories code: $code")
        }
    }
}
