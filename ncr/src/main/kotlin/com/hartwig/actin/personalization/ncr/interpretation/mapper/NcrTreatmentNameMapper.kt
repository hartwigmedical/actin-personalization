package com.hartwig.actin.personalization.ncr.interpretation.mapper

import com.hartwig.actin.personalization.datamodel.Drug

object NcrTreatmentNameMapper : NcrStringCodeMapper<Drug> {
    override fun resolve(code: String): Drug {
        return when (code) {
            "214000" -> Drug.EXTERNAL_RADIOTHERAPY_WITH_SENSITIZER
            "420INT" -> Drug.INTENSIVE_CHEMOTHERAPY
            "420000" -> Drug.SYSTEMIC_CHEMOTHERAPY
            "421000" -> Drug.TAXANE_CONTAINING_CHEMOTHERAPY
            "422000" -> Drug.PLATINUM_CONTAINING_CHEMOTHERAPY
            "426000" -> Drug.ANTHRACYCLINE_CONTAINING_CHEMOTHERAPY
            "427000" -> Drug.ANTHRACYCLINE_AND_TAXANE_CONTAINING_CHEMOTHERAPY
            "690420" -> Drug.CHEMOTHERAPY_ABROAD
            "L01AA01" -> Drug.CYCLOPHOSPHAMIDE
            "L01AA02" -> Drug.CHLORAMBUCIL_LEUKERAN
            "L01AA03" -> Drug.MELPHALAN
            "L01AA09" -> Drug.BENDAMUSTINE
            "L01BA01" -> Drug.METHOTREXATE
            "L01BA03" -> Drug.RALTITREXED
            "L01BA04" -> Drug.PEMETREXED
            "L01BB04" -> Drug.CLADRIBINE
            "L01BB05" -> Drug.CLADRIBINE  // TODO - Fludarabine
            "L01BC01" -> Drug.CYTARABINE
            "L01BC02" -> Drug.FLUOROURACIL
            "L01BC03" -> Drug.TEGAFUR
            "L01BC05" -> Drug.GEMCITABINE_SYSTEMIC
            "L01BC06" -> Drug.CAPECITABINE
            "L01BC53" -> Drug.TEGAFUR_OR_GIMERACIL_OR_OTERACIL
            "L01BC59" -> Drug.TRIFLURIDINE_AND_TIPIRACIL
            "L01CA02" -> Drug.VINCRISTINE
            "L01CB01" -> Drug.ETOPOSIDE
            "L01CD01" -> Drug.PACLITAXEL
            "L01CD02" -> Drug.DOCETAXEL
            "L01CE02" -> Drug.IRINOTECAN
            "L01DB01" -> Drug.DOXORUBICIN_ADRIAMYCIN
            "L01DB02" -> Drug.DAUNORUBICIN
            "L01DB03" -> Drug.EPIRUBICIN_SYSTEMIC
            "L01DB06" -> Drug.EPIRUBICIN_SYSTEMIC  // TODO - Idarubicine
            "L01DC03" -> Drug.MITOMYCIN_SYSTEMIC
            "L01XA01" -> Drug.CISPLATIN
            "L01XA02" -> Drug.CARBOPLATIN
            "L01XA03" -> Drug.OXALIPLATIN
            "L01XX05" -> Drug.HYDROREA_OR_HYDROXYUREA
            "L01XX27" -> Drug.ATO_ARSENIC_TRIOXIDE
            "500000" -> Drug.HORMONAL_THERAPY_NNO
            "510000" -> Drug.HORMONAL_THERAPY_THROUGH_SURGERY
            "540000" -> Drug.DRUG_HORMONAL_THERAPY
            "H01CB02" -> Drug.OCTREOTIDE
            "L02AB01" -> Drug.MEGESTROL_MEGACE
            "L02AE" -> Drug.GONADORELIN_AGONISTS_LHRH
            "L02AE02" -> Drug.LEUPRORELIN_ELIGARD_OR_LUCRIN
            "L02AE03" -> Drug.GOSERELIN_ZOLADEX
            "L02AE04" -> Drug.TRIPTORELIN_PAMORELIN
            "L02BA01" -> Drug.TAMOXIFEN
            "L02BB" -> Drug.ANTI_ANDROGENS
            "L02BB03" -> Drug.BICALUTAMIDE_BILURON_OR_CASODEX
            "L02BG" -> Drug.AROMATASE_INHIBITORS
            "L02BG03" -> Drug.ANASTROZOLE
            "L02BG04" -> Drug.LETROZOLE_FEMARA
            "L02BX" -> Drug.GONADORELIN_ANTAGONISTS_LHRH
            "L02BX03", "L02BX53" -> Drug.ABIRATERONE_ZYTIGA_AND_PREDNISONE  // TODO
            "H02AB02" -> Drug.DEXAMETHASONE
            "H02AB07" -> Drug.PREDNISONE
            "L03AB04" -> Drug.INTERFERON_ALPHA_2A
            "430000" -> Drug.TARGETED_THERAPY
            "433000" -> Drug.ANGIOGENESIS_INHIBITOR
            "445000" -> Drug.BEVACIZUMAB  // TODO - Bevacizumab/interferon alfa-2a
            "690430" -> Drug.TARGETED_THERAPY_ABROAD
            "699005" -> Drug.STUDY_MEDICATION_IMMUNOTHERAPY
            "L01E" -> Drug.PROTEIN_KINASE_INHIBITORS
            "L01EA01" -> Drug.IMATINIB
            "L01EB02" -> Drug.ERLOTINIB
            "L01EB04" -> Drug.ERLOTINIB  // TODO - OSIMERTINIB
            "L01EC01" -> Drug.VEMURAFENIB
            "L01EC02" -> Drug.DABRAFENIB
            "L01EC03" -> Drug.ENCORAFENIB
            "L01EE01" -> Drug.TRAMETINIB
            "L01EE03" -> Drug.BINIMETINIB
            "L01EJ01" -> Drug.RUXOLITINIB
            "L01EK01" -> Drug.AXITINIB
            "L01EL01" -> Drug.IBRUTINIB
            "L01EX03" -> Drug.PAZOPANIB
            "L01EX07" -> Drug.CABOZANTINIB
            "L01EX17" -> Drug.CABOZANTINIB  // TODO - Capmatinib
            "L01F" -> Drug.MONOCLONAL_ANTIBODIES
            "L01FA01" -> Drug.RITUXIMAB
            "L01FC01" -> Drug.TARGETED_THERAPY  // TODO - DARATUMUMAB
            "L01FD01" -> Drug.TRASTUZUMAB_HERCEPTIN
            "L01FD02" -> Drug.PERTUZUMAB
            "L01FE01" -> Drug.CETUXIMAB
            "L01FE02" -> Drug.PANITUMUMAB
            "L01FF01" -> Drug.NIVOLUMAB
            "L01FF02" -> Drug.PEMBROLIZUMAB
            "L01FF03" -> Drug.DURVALUMAB
            "L01FF05" -> Drug.ATEZOLIZUMAB
            "L01FG01" -> Drug.BEVACIZUMAB
            "L01FX04" -> Drug.IPILIMUMAB
            "L01FX20" -> Drug.TREMELIMUMAB
            "L01XG01" -> Drug.BORTEZOMIB
            "L04AX02" -> Drug.THALIDOMIDE
            "L04AX04" -> Drug.LENALIDOMIDE
            else -> throw IllegalArgumentException("Unknown treatment code: $code")
        }
    }
}