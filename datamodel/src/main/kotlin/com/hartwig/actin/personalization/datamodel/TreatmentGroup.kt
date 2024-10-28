package com.hartwig.actin.personalization.datamodel

enum class TreatmentGroup(val display: String) {
    CAPECITABINE_OR_FLUOROURACIL("Capecitabine / Fluorouracil"),
    CAPECITABINE_B_OR_FLUOROURACIL_B("Capecitabine-B / Fluorouracil-B"),
    CAPOX_OR_FOLFOX("CAPOX / FOLFOX"),
    CAPOX_B_OR_FOLFOX_B("CAPOX-B / FOLFOX-B"),
    FOLFIRI("FOLFIRI"),
    FOLFIRI_B("FOLFIRI-B"),
    FOLFIRI_P("FOLFIRI-P"),
    FOLFOX_P("FOLFOX-P"),
    FOLFOXIRI("FOLFOXIRI"),
    FOLFOXIRI_B("FOLFOXIRI-B"),
    IRINOTECAN("Irinotecan"),
    NIVOLUMAB("Nivolumab"),
    OTHER("Other"),
    PEMBROLIZUMAB("Pembrolizumab"),
    NONE("None");
}