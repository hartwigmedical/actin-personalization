package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.ncr.datamodel.NcrCharlsonComorbidities
import com.hartwig.actin.personalization.ncr.datamodel.NcrClinicalCharacteristics
import com.hartwig.actin.personalization.ncr.datamodel.NcrGastroenterologyResection
import com.hartwig.actin.personalization.ncr.datamodel.NcrHipec
import com.hartwig.actin.personalization.ncr.datamodel.NcrIdentification
import com.hartwig.actin.personalization.ncr.datamodel.NcrLabValues
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticDiagnosis
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticRadiotherapy
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticSurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrMolecularCharacteristics
import com.hartwig.actin.personalization.ncr.datamodel.NcrPatientCharacteristics
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimaryDiagnosis
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimaryRadiotherapy
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimarySurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrPriorMalignancies
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.datamodel.NcrSystemicTreatment
import com.hartwig.actin.personalization.ncr.datamodel.NcrTreatment
import com.hartwig.actin.personalization.ncr.datamodel.NcrTreatmentResponse

const val EPISODE_ID = 123
const val EPISODE_ORDER = 2
const val WHO_STATUS = 1
const val INCIDENCE_YEAR = 2020
const val INVESTIGATED_LYMPH_NODES = 3
const val POSITIVE_LYMPH_NODES = 1

val NCR_IDENTIFICATION = NcrIdentification(
    keyNkr = 5,
    keyZid = 6,
    keyEid = EPISODE_ID,
    teller = EPISODE_ORDER,
    epis = "EPI",
    metaEpis = 1
)

val NCR_LAB_VALUES = NcrLabValues(
    prechirCea = 0.1,
    postchirCea = 0.2,
    ldh1 = 10,
    ldhInt1 = 1,
    af1 = 20,
    afInt1 = 2,
    neutro1 = 30.5,
    neutroInt1 = 3,
    albumine1 = 40.5,
    albumineInt1 = 4,
    leuko1 = 50.5,
    leukoInt1 = 5
)

val NCR_SYSTEMIC_TREATMENT = NcrSystemicTreatment(
    chemo = 1,
    target = 2,
    systCode1 = "L01XA03",
    systCode2 = "L01FG01",
    systCode3 = "L01CE02",
    systCode4 = "L01BC02",
    systCode5 = "L01BC06",
    systCode6 = "L01CE02",
    systCode7 = "L01BC53",
    systPrepost1 = 1,
    systPrepost2 = 2,
    systPrepost3 = 3,
    systPrepost4 = 4,
    systPrepost5 = 0,
    systPrepost6 = null,
    systPrepost7 = 0,
    systSchemanum1 = 1,
    systSchemanum2 = 1,
    systSchemanum3 = 1,
    systSchemanum4 = 1,
    systSchemanum5 = 2,
    systSchemanum6 = 2,
    systSchemanum7 = 3,
    systKuren1 = 1,
    systKuren2 = 2,
    systKuren3 = 3,
    systKuren4 = 4,
    systKuren5 = 5,
    systKuren6 = 6,
    systKuren7 = 7,
    systStartInt1 = 1,
    systStartInt2 = 2,
    systStartInt3 = 3,
    systStartInt4 = 4,
    systStartInt5 = 5,
    systStartInt6 = 6,
    systStartInt7 = 7,
    systStopInt1 = 1,
    systStopInt2 = 2,
    systStopInt3 = 3,
    systStopInt4 = 4,
    systStopInt5 = 5,
    systStopInt6 = 6,
    systStopInt7 = 7,
)

val NCR_METASTATIC_DIAGNOSIS = NcrMetastaticDiagnosis(
    metaTopoSublok1 = "C740",
    metaInt1 = 20,
    metaProg1 = 1,
    metaLeverAantal = 5,
    metaLeverAfm = 15,
)

val NCR_PRIOR_MALIGNANCIES = NcrPriorMalignancies(
    mal1Morf = 8720,
    mal1TopoSublok = "C446",
    mal1Syst = 1,
    mal1Int = 20,
    mal1Tumsoort = 400000,
    mal1Stadium = "2C",
    mal1SystCode1 = "214000"
)

val NCR_CHARLSON_COMORBIDITIES = NcrCharlsonComorbidities(
    cci = 1,
    cciCat = 1,
    cciChf = 1,
    cciCopd = 0
)

val NCR_RECORD = NcrRecord(
    identification = NCR_IDENTIFICATION,
    patientCharacteristics = NcrPatientCharacteristics(
        gesl = 1,
        leeft = 50,
        vitStat = 0,
        vitStatInt = 80,
        perfStat = WHO_STATUS,
        asa = 5
    ),
    clinicalCharacteristics = NcrClinicalCharacteristics(
        dubbeltum = 0,
        ileus = 0,
        perforatie = 1,
        anusAfst = 2,
        mrfAfst = 111,
        veneusInvas = 5,
        lymfInvas = 8,
        emi = 1,
        tumregres = 4
    ),
    molecularCharacteristics = NcrMolecularCharacteristics(
        msiStat = 1,
        brafMut = 1,
        rasMut = 3
    ),
    priorMalignancies = NCR_PRIOR_MALIGNANCIES,
    primaryDiagnosis = NcrPrimaryDiagnosis(
        incjr = INCIDENCE_YEAR,
        topoSublok = "C182",
        morfCat = 1,
        diagBasis = 4,
        diffgrad = "2",
        ct = "0",
        cn = "1A",
        cm = "-",
        pt = "4A",
        pn = "X",
        pm = null,
        cstadium = "NVT",
        pstadium = "M",
        stadium = "2C",
        ondLymf = INVESTIGATED_LYMPH_NODES,
        posLymf = POSITIVE_LYMPH_NODES
    ),
    metastaticDiagnosis = NCR_METASTATIC_DIAGNOSIS,
    comorbidities = NCR_CHARLSON_COMORBIDITIES,
    labValues = NCR_LAB_VALUES,
    treatment = NcrTreatment(
        deelnameStudie = 1,
        tumgerichtTher = 0,
        geenTherReden = 12,
        gastroenterologyResection = NcrGastroenterologyResection(),
        primarySurgery = NcrPrimarySurgery(),
        metastaticSurgery = NcrMetastaticSurgery(),
        primaryRadiotherapy = NcrPrimaryRadiotherapy(4),
        metastaticRadiotherapy = NcrMetastaticRadiotherapy(),
        systemicTreatment = NCR_SYSTEMIC_TREATMENT,
        hipec = NcrHipec(0, null)
    ),
    treatmentResponse = NcrTreatmentResponse(
        responsUitslag = "PD",
        responsInt = 5,
        pfsEvent1 = 1,
        pfsEvent2 = 2,
        fupEventType1 = 1,
        fupEventType2 = 2,
        pfsInt1 = 4,
        pfsInt2 = 80,
    )
)
