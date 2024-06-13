package com.hartwig.actin.personalization.datamodel

enum class SystemicTreatmentPlanName(val drugs: Set<Drug>?) {
    CAPOX(setOf(Drug.CAPECITABINE, Drug.OXALIPLATIN)),
    CAPOX_B(setOf(Drug.CAPECITABINE, Drug.OXALIPLATIN, Drug.BEVACIZUMAB)),
    CAPECITABINE(setOf(Drug.CAPECITABINE)),
    CAPECITABINE_BEVACIZUMAB(setOf(Drug.CAPECITABINE, Drug.BEVACIZUMAB)),
    FOLFIRI(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN)),
    FOLFIRI_B(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN, Drug.BEVACIZUMAB)),
    FOLFIRI_P(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN, Drug.PANITUMUMAB)),
    FOLFOX(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN)),
    FOLFOX_B(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.BEVACIZUMAB)),
    FOLFOX_P(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.PANITUMUMAB)),
    FOLFOXIRI(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.IRINOTECAN)),
    FOLFOXIRI_B(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.IRINOTECAN, Drug.BEVACIZUMAB)),
    FLUOROURACIL(setOf(Drug.FLUOROURACIL)),
    FLUOROURACIL_BEVACIZUMAB(setOf(Drug.FLUOROURACIL, Drug.BEVACIZUMAB)),
    IRINOTECAN(setOf(Drug.IRINOTECAN)),
    NIVOLUMAB(setOf(Drug.NIVOLUMAB)),
    OTHER(null),
    PEMBROLIZUMAB(setOf(Drug.PEMBROLIZUMAB));

    companion object {
        private val plansByDrugSet = values().associateBy(SystemicTreatmentPlanName::drugs)

        fun findForDrugs(drugs: Set<Drug>?): SystemicTreatmentPlanName {
            return plansByDrugSet[drugs] ?: OTHER
        }
    }
}