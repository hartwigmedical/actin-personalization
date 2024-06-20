package com.hartwig.actin.personalization.datamodel

enum class Treatment(val drugs: Set<Drug>?, val display: String) {
    CAPOX(setOf(Drug.CAPECITABINE, Drug.OXALIPLATIN), "CAPOX"),
    CAPOX_B(setOf(Drug.CAPECITABINE, Drug.OXALIPLATIN, Drug.BEVACIZUMAB), "CAPOX-B"),
    CAPECITABINE(setOf(Drug.CAPECITABINE), "Capecitabine"),
    CAPECITABINE_BEVACIZUMAB(setOf(Drug.CAPECITABINE, Drug.BEVACIZUMAB), "Capecitabine-B"),
    FOLFIRI(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN), "FOLFIRI"),
    FOLFIRI_B(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN, Drug.BEVACIZUMAB), "FOLFIRI-B"),
    FOLFIRI_P(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN, Drug.PANITUMUMAB), "FOLFIRI-P"),
    FOLFOX(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN), "FOLFOX"),
    FOLFOX_B(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.BEVACIZUMAB), "FOLFOX-B"),
    FOLFOX_P(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.PANITUMUMAB), "FOLFOX-P"),
    FOLFOXIRI(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.IRINOTECAN), "FOLFOXIRI"),
    FOLFOXIRI_B(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.IRINOTECAN, Drug.BEVACIZUMAB), "FOLFOXIRI-B"),
    FLUOROURACIL(setOf(Drug.FLUOROURACIL), "Fluorouracil"),
    FLUOROURACIL_BEVACIZUMAB(setOf(Drug.FLUOROURACIL, Drug.BEVACIZUMAB), "Fluorouracil-B"),
    IRINOTECAN(setOf(Drug.IRINOTECAN), "Irinotecan"),
    NIVOLUMAB(setOf(Drug.NIVOLUMAB), "Nivolumab"),
    OTHER(null, "Other"),
    PEMBROLIZUMAB(setOf(Drug.PEMBROLIZUMAB), "Pembrolizumab");

    companion object {
        private val plansByDrugSet = Treatment.entries.associateBy(Treatment::drugs)

        fun findForDrugs(drugs: Set<Drug>?): Treatment {
            return plansByDrugSet[drugs] ?: OTHER
        }
    }
}