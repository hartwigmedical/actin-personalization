package com.hartwig.actin.personalization.datamodel.treatment

enum class Treatment(val drugs: Set<Drug>?, val display: String, val treatmentGroup: TreatmentGroup) {
    CAPECITABINE(setOf(Drug.CAPECITABINE), "Capecitabine", TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL),
    CAPECITABINE_BEVACIZUMAB(setOf(Drug.CAPECITABINE, Drug.BEVACIZUMAB), "Capecitabine-B", TreatmentGroup.CAPECITABINE_B_OR_FLUOROURACIL_B),
    CAPOX(setOf(Drug.CAPECITABINE, Drug.OXALIPLATIN), "CAPOX", TreatmentGroup.CAPOX_OR_FOLFOX),
    CAPOX_B(setOf(Drug.CAPECITABINE, Drug.OXALIPLATIN, Drug.BEVACIZUMAB), "CAPOX-B", TreatmentGroup.CAPOX_B_OR_FOLFOX_B),
    FOLFIRI(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN), "FOLFIRI", TreatmentGroup.FOLFIRI),
    FOLFIRI_B(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN, Drug.BEVACIZUMAB), "FOLFIRI-B", TreatmentGroup.FOLFIRI_B),
    FOLFIRI_P(setOf(Drug.FLUOROURACIL, Drug.IRINOTECAN, Drug.PANITUMUMAB), "FOLFIRI-P", TreatmentGroup.FOLFIRI_P),
    FOLFOX(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN), "FOLFOX", TreatmentGroup.CAPOX_OR_FOLFOX),
    FOLFOX_B(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.BEVACIZUMAB), "FOLFOX-B", TreatmentGroup.CAPOX_B_OR_FOLFOX_B),
    FOLFOX_P(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.PANITUMUMAB), "FOLFOX-P", TreatmentGroup.FOLFOX_P),
    FOLFOXIRI(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.IRINOTECAN), "FOLFOXIRI", TreatmentGroup.FOLFOXIRI),
    FOLFOXIRI_B(setOf(Drug.FLUOROURACIL, Drug.OXALIPLATIN, Drug.IRINOTECAN, Drug.BEVACIZUMAB), "FOLFOXIRI-B", TreatmentGroup.FOLFOXIRI_B),
    FLUOROURACIL(setOf(Drug.FLUOROURACIL), "Fluorouracil", TreatmentGroup.CAPECITABINE_OR_FLUOROURACIL),
    FLUOROURACIL_BEVACIZUMAB(setOf(Drug.FLUOROURACIL, Drug.BEVACIZUMAB), "Fluorouracil-B", TreatmentGroup.CAPECITABINE_B_OR_FLUOROURACIL_B),
    IRINOTECAN(setOf(Drug.IRINOTECAN), "Irinotecan", TreatmentGroup.IRINOTECAN),
    NIVOLUMAB(setOf(Drug.NIVOLUMAB), "Nivolumab", TreatmentGroup.NIVOLUMAB),
    OTHER(null, "Other", TreatmentGroup.OTHER),
    PEMBROLIZUMAB(setOf(Drug.PEMBROLIZUMAB), "Pembrolizumab", TreatmentGroup.PEMBROLIZUMAB);
    companion object {
        private val plansByDrugSet = Treatment.entries.filterNot { it.drugs == null }.associateBy(Treatment::drugs)

        fun findForDrugs(drugs: Set<Drug>): Treatment {
            return plansByDrugSet[drugs] ?: OTHER
        }
    }
}