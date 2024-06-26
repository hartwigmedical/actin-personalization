package com.hartwig.actin.personalization.datamodel

enum class Treatment(val drugs: Set<Drug>?, val display: String, private val parentGroup: Treatment? = null) {
    CAPECITABINE_OR_FLUOROURACIL(null, "Capecitabine / Fluorouracil"),
    CAPECITABINE_B_OR_FLUOROURACIL_B(null, "Capecitabine-B / Fluorouracil-B"),
    CAPOX_OR_FOLFOX(null, "CAPOX / FOLFOX"),
    CAPOX_B_OR_FOLFOX_B(null, "CAPOX-B / FOLFOX-B"),
    CAPECITABINE(setOf(Drug.CAPECITABINE), "Capecitabine", CAPECITABINE_OR_FLUOROURACIL),
    CAPECITABINE_BEVACIZUMAB(setOf(Drug.CAPECITABINE, Drug.BEVACIZUMAB), "Capecitabine-B", CAPECITABINE_B_OR_FLUOROURACIL_B),
    CAPOX(setOf(Drug.CAPECITABINE, Drug.OXALIPLATIN), "CAPOX", CAPOX_OR_FOLFOX),
    CAPOX_B(setOf(Drug.CAPECITABINE, Drug.OXALIPLATIN, Drug.BEVACIZUMAB), "CAPOX-B", CAPOX_B_OR_FOLFOX_B),
    FOLFIRI(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN), "FOLFIRI"),
    FOLFIRI_B(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN, Drug.BEVACIZUMAB), "FOLFIRI-B"),
    FOLFIRI_P(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN, Drug.PANITUMUMAB), "FOLFIRI-P"),
    FOLFOX(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN), "FOLFOX", CAPOX_OR_FOLFOX),
    FOLFOX_B(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.BEVACIZUMAB), "FOLFOX-B", CAPOX_B_OR_FOLFOX_B),
    FOLFOX_P(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.PANITUMUMAB), "FOLFOX-P"),
    FOLFOXIRI(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.IRINOTECAN), "FOLFOXIRI"),
    FOLFOXIRI_B(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.IRINOTECAN, Drug.BEVACIZUMAB), "FOLFOXIRI-B"),
    FLUOROURACIL(setOf(Drug.FLUOROURACIL), "Fluorouracil", CAPECITABINE_OR_FLUOROURACIL),
    FLUOROURACIL_BEVACIZUMAB(setOf(Drug.FLUOROURACIL, Drug.BEVACIZUMAB), "Fluorouracil-B", CAPECITABINE_B_OR_FLUOROURACIL_B),
    IRINOTECAN(setOf(Drug.IRINOTECAN), "Irinotecan"),
    NIVOLUMAB(setOf(Drug.NIVOLUMAB), "Nivolumab"),
    OTHER(null, "Other"),
    PEMBROLIZUMAB(setOf(Drug.PEMBROLIZUMAB), "Pembrolizumab");

    fun group(): Treatment = parentGroup ?: this

    companion object {
        private val plansByDrugSet = Treatment.entries.filterNot { it.drugs == null }.associateBy(Treatment::drugs)

        fun findForDrugs(drugs: Set<Drug>): Treatment {
            return plansByDrugSet[drugs] ?: OTHER
        }
    }
}