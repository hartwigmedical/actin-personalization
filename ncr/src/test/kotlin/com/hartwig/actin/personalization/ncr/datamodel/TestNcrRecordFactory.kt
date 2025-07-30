package com.hartwig.actin.personalization.ncr.datamodel

object TestNcrRecordFactory {

    private const val DIAGNOSIS_EPIS = "DIA"
    private const val FOLLOWUP_EPIS = "VERB"

    fun minimalEntryRecords(): List<NcrRecord> {
        val baseRecord = minimalDiagnosisRecord()
        return listOf(baseRecord.copy(identification = baseRecord.identification.copy(metaEpis = 1)))
    }

    fun properEntryRecords(): List<NcrRecord> {
        return listOf(properDiagnosisRecord(), properFollowupRecord1(), properFollowupRecord2())
    }

    fun minimalDiagnosisRecord(): NcrRecord {
        val baseRecord = minimalRecord()
        return baseRecord
            .copy(
                identification = baseRecord.identification.copy(epis = DIAGNOSIS_EPIS),
                clinicalCharacteristics = baseRecord.clinicalCharacteristics.copy(anusAfst = 9),
                patientCharacteristics = baseRecord.patientCharacteristics.copy(vitStat = 0, vitStatInt = 563)
            )
    }

    fun minimalFollowupRecord(): NcrRecord {
        val baseRecord = minimalRecord()
        return baseRecord
            .copy(identification = baseRecord.identification.copy(epis = FOLLOWUP_EPIS, metaEpis = 1))
            .copy(patientCharacteristics = baseRecord.patientCharacteristics.copy(vitStat = null, vitStatInt = null))
            .copy(primaryDiagnosis = baseRecord.primaryDiagnosis.copy(morfCat = null))
            .copy(molecularCharacteristics = baseRecord.molecularCharacteristics.copy(brafMut = 0, rasMut = 0, msiStat = 0))
    }

    fun properDiagnosisRecord(): NcrRecord {
        val baseRecord = properRecord()
        return baseRecord
            .copy(identification = baseRecord.identification.copy(epis = DIAGNOSIS_EPIS, metaEpis = 0, teller = 1))
            .copy(metastaticDiagnosis = minimalMetastaticDiagnosis())
            .copy(treatmentResponse = minimalTreatmentResponse())
    }

    fun properFollowupRecord1(): NcrRecord {
        val baseRecord = properRecord()
        return baseRecord
            .copy(identification = baseRecord.identification.copy(epis = FOLLOWUP_EPIS, metaEpis = 0, teller = 2))
            .copy(
                patientCharacteristics = baseRecord.patientCharacteristics.copy(
                    vitStat = null,
                    vitStatInt = null
                )
            )
            .copy(clinicalCharacteristics = minimalClinicalCharacteristics())
            .copy(molecularCharacteristics = properMolecularCharacteristics())
            .copy(priorMalignancies = minimalPriorMalignancies())
            .copy(primaryDiagnosis = properPrimaryDiagnosisForMetastaticEpisode())
            .copy(metastaticDiagnosis = minimalMetastaticDiagnosis())
            .copy(comorbidities = minimalComorbidities())
            .copy(labValues = properFollowup1LabValues())
            .copy(treatment = properFollowup1Treatment())
            .copy(treatmentResponse = minimalTreatmentResponse())
    }

    fun properFollowupRecord2(): NcrRecord {
        val baseRecord = properRecord()
        return baseRecord
            .copy(identification = baseRecord.identification.copy(epis = FOLLOWUP_EPIS, metaEpis = 1, teller = 3))
            .copy(
                patientCharacteristics = baseRecord.patientCharacteristics.copy(
                    vitStat = null,
                    vitStatInt = null,
                    perfStat = 2,
                    asa = 6
                )
            )
            .copy(clinicalCharacteristics = minimalClinicalCharacteristics())
            .copy(molecularCharacteristics = properMolecularCharacteristics())
            .copy(priorMalignancies = minimalPriorMalignancies())
            .copy(primaryDiagnosis = properPrimaryDiagnosisForMetastaticEpisode())
            .copy(comorbidities = minimalComorbidities())
            .copy(labValues = minimalLabValues())
            .copy(treatment = properFollowup2Treatment())
    }

    private fun minimalRecord(): NcrRecord {
        return NcrRecord(
            identification = minimalIdentification(),
            patientCharacteristics = minimalPatientCharacteristics(),
            clinicalCharacteristics = minimalClinicalCharacteristics(),
            molecularCharacteristics = minimalMolecularCharacteristics(),
            priorMalignancies = minimalPriorMalignancies(),
            primaryDiagnosis = minimalPrimaryDiagnosis(),
            metastaticDiagnosis = minimalMetastaticDiagnosis(),
            comorbidities = minimalComorbidities(),
            labValues = minimalLabValues(),
            treatment = minimalTreatment(),
            treatmentResponse = minimalTreatmentResponse()
        )
    }

    private fun properRecord(): NcrRecord {
        return NcrRecord(
            identification = properIdentification(),
            patientCharacteristics = properPatientCharacteristics(),
            clinicalCharacteristics = properClinicalCharacteristics(),
            molecularCharacteristics = properMolecularCharacteristics(),
            priorMalignancies = properPriorMalignancies(),
            primaryDiagnosis = properPrimaryDiagnosis(),
            metastaticDiagnosis = properMetastaticDiagnosis(),
            comorbidities = properComorbidities(),
            labValues = properDiagnosisLabValues(),
            treatment = properDiagnosisTreatment(),
            treatmentResponse = properTreatmentResponse()
        )
    }

    private fun minimalIdentification(): NcrIdentification {
        return NcrIdentification(
            keyNkr = 3,
            keyZid = 2,
            keyEid = 1,
            epis = "",
            metaEpis = 0,
            teller = 0
        )
    }

    private fun properIdentification(): NcrIdentification {
        return minimalIdentification().copy(
            metaEpis = 1,
            teller = 1
        )
    }

    private fun minimalPatientCharacteristics(): NcrPatientCharacteristics {
        return NcrPatientCharacteristics(
            gesl = 2,
            leeft = 75,
            vitStat = 0,
            vitStatInt = 563,
            perfStat = null,
            asa = null
        )
    }

    private fun properPatientCharacteristics(): NcrPatientCharacteristics {
        return minimalPatientCharacteristics().copy(
            perfStat = 1,
            asa = 5
        )
    }

    private fun minimalClinicalCharacteristics(): NcrClinicalCharacteristics {
        return NcrClinicalCharacteristics(
            dubbeltum = 0,
            ileus = null,
            perforatie = null,
            anusAfst = null,
            mrfAfst = null,
            veneusInvas = null,
            lymfInvas = null,
            emi = null,
            tumregres = null
        )
    }

    private fun properClinicalCharacteristics(): NcrClinicalCharacteristics {
        return minimalClinicalCharacteristics().copy(
            ileus = 0,
            perforatie = 1,
            anusAfst = 2,
            mrfAfst = 111,
            veneusInvas = 5,
            lymfInvas = 8,
            emi = 1,
            tumregres = 4
        )
    }

    private fun minimalMolecularCharacteristics(): NcrMolecularCharacteristics {
        return NcrMolecularCharacteristics(
            msiStat = null,
            brafMut = null,
            rasMut = null
        )
    }

    private fun properMolecularCharacteristics(): NcrMolecularCharacteristics {
        return minimalMolecularCharacteristics().copy(
            msiStat = 0,
            brafMut = 1,
            rasMut = 3
        )
    }

    private fun minimalPriorMalignancies(): NcrPriorMalignancies {
        return NcrPriorMalignancies(
            mal1Int = null,
            mal2Int = null,
            mal3Int = null,
            mal4Int = null,
            mal1TopoSublok = null,
            mal2TopoSublok = null,
            mal3TopoSublok = null,
            mal4TopoSublok = null,
            mal1Morf = null,
            mal2Morf = null,
            mal3Morf = null,
            mal4Morf = null,
            mal1Tumsoort = null,
            mal2Tumsoort = null,
            mal3Tumsoort = null,
            mal4Tumsoort = null,
            mal1Stadium = null,
            mal2Stadium = null,
            mal3Stadium = null,
            mal4Stadium = null,
            mal1Syst = null,
            mal2Syst = null,
            mal3Syst = null,
            mal4Syst = null,
            mal1SystCode1 = null,
            mal1SystCode2 = null,
            mal1SystCode3 = null,
            mal1SystCode4 = null,
            mal1SystCode5 = null,
            mal1SystCode6 = null,
            mal1SystCode7 = null,
            mal1SystCode8 = null,
            mal1SystCode9 = null,
            mal2SystCode1 = null,
            mal2SystCode2 = null,
            mal2SystCode3 = null,
            mal2SystCode4 = null,
            mal2SystCode5 = null,
            mal3SystCode1 = null,
            mal3SystCode2 = null,
            mal3SystCode3 = null,
            mal3SystCode4 = null
        )
    }

    private fun properPriorMalignancies(): NcrPriorMalignancies {
        return minimalPriorMalignancies().copy(
            mal1Int = -206,
            mal1TopoSublok = "C446",
            mal1Morf = 8720,
            mal1Tumsoort = 400000,
            mal1Stadium = "2C",
            mal1Syst = 1,
            mal1SystCode1 = "420000",
            mal1SystCode2 = "699005"
        )
    }

    private fun minimalPrimaryDiagnosis(): NcrPrimaryDiagnosis {
        return NcrPrimaryDiagnosis(
            incjr = 2020,
            topoSublok = "C182",
            morfCat = 1,
            diagBasis = 1,
            diffgrad = 2,
            ct = "0",
            cn = null,
            cm = null,
            pt = null,
            pn = null,
            pm = null,
            cstadium = "2",
            pstadium = "3",
            stadium = null,
            ondLymf = null,
            posLymf = null
        )
    }

    private fun properPrimaryDiagnosis(): NcrPrimaryDiagnosis {
        return minimalPrimaryDiagnosis().copy(
            ct = "2",
            cn = "1",
            cm = "0",
            pt = "3",
            pn = "2",
            pm = null,
            stadium = "III",
            ondLymf = 3,
            posLymf = 1
        )
    }

    private fun properPrimaryDiagnosisForMetastaticEpisode(): NcrPrimaryDiagnosis {
        return minimalPrimaryDiagnosis().copy(
            morfCat = null,
            ct = null,
            cn = null,
            cm = null,
            pt = null,
            pn = null,
            pm = "1",
            stadium = "IV",
            ondLymf = 4,
            posLymf = 2
        )
    }

    private fun minimalMetastaticDiagnosis(): NcrMetastaticDiagnosis {
        return NcrMetastaticDiagnosis(
            metaTopoSublok1 = null,
            metaTopoSublok2 = null,
            metaTopoSublok3 = null,
            metaTopoSublok4 = null,
            metaTopoSublok5 = null,
            metaTopoSublok6 = null,
            metaTopoSublok7 = null,
            metaTopoSublok8 = null,
            metaTopoSublok9 = null,
            metaTopoSublok10 = null,
            metaInt1 = null,
            metaInt2 = null,
            metaInt3 = null,
            metaInt4 = null,
            metaInt5 = null,
            metaInt6 = null,
            metaInt7 = null,
            metaInt8 = null,
            metaInt9 = null,
            metaInt10 = null,
            metaProg1 = null,
            metaProg2 = null,
            metaProg3 = null,
            metaProg4 = null,
            metaProg5 = null,
            metaProg6 = null,
            metaProg7 = null,
            metaProg8 = null,
            metaProg9 = null,
            metaProg10 = null,
            metaLeverAantal = null,
            metaLeverAfm = null
        )
    }

    private fun properMetastaticDiagnosis(): NcrMetastaticDiagnosis {
        return minimalMetastaticDiagnosis().copy(
            metaTopoSublok1 = "C740",
            metaInt1 = 200,
            metaProg1 = 0,
            metaTopoSublok2 = "C719",
            metaLeverAantal = 5,
            metaLeverAfm = 15,
        )
    }

    private fun minimalComorbidities(): NcrCharlsonComorbidities {
        return NcrCharlsonComorbidities(
            cci = null,
            cciAids = null,
            cciCat = null,
            cciChf = null,
            cciCollagenosis = null,
            cciCopd = null,
            cciCvd = null,
            cciDementia = null,
            cciDm = null,
            cciEodDm = null,
            cciMalignancy = null,
            cciMetastatic = null,
            cciMi = null,
            cciMildLiver = null,
            cciPlegia = null,
            cciPvd = null,
            cciRenal = null,
            cciSevereLiver = null,
            cciUlcer = null
        )
    }

    private fun properComorbidities(): NcrCharlsonComorbidities {
        return minimalComorbidities().copy(
            cci = 2,
            cciAids = 0,
            cciCat = 0,
            cciChf = 1,
            cciCollagenosis = 0,
            cciCopd = 0,
            cciCvd = 0,
            cciDementia = 1,
            cciDm = 0,
            cciEodDm = 0,
            cciMalignancy = 0,
            cciMetastatic = 0,
            cciMi = 0,
            cciMildLiver = 0,
            cciPlegia = 0,
            cciPvd = 0,
            cciRenal = 0,
            cciSevereLiver = 0,
            cciUlcer = 0
        )
    }

    private fun minimalLabValues(): NcrLabValues {
        return NcrLabValues(
            prechirCea = null,
            postchirCea = null,
            ldh1 = null,
            ldh2 = null,
            ldh3 = null,
            ldh4 = null,
            ldhInt1 = null,
            ldhInt2 = null,
            ldhInt3 = null,
            ldhInt4 = null,
            af1 = null,
            af2 = null,
            af3 = null,
            af4 = null,
            afInt1 = null,
            afInt2 = null,
            afInt3 = null,
            afInt4 = null,
            neutro1 = null,
            neutro2 = null,
            neutro3 = null,
            neutro4 = null,
            neutroInt1 = null,
            neutroInt2 = null,
            neutroInt3 = null,
            neutroInt4 = null,
            albumine1 = null,
            albumine2 = null,
            albumine3 = null,
            albumine4 = null,
            albumineInt1 = null,
            albumineInt2 = null,
            albumineInt3 = null,
            albumineInt4 = null,
            leuko1 = null,
            leuko2 = null,
            leuko3 = null,
            leuko4 = null,
            leukoInt1 = null,
            leukoInt2 = null,
            leukoInt3 = null,
            leukoInt4 = null,
        )
    }

    private fun properDiagnosisLabValues(): NcrLabValues {
        return minimalLabValues().copy(
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
    }

    private fun properFollowup1LabValues(): NcrLabValues {
        return minimalLabValues().copy(
            prechirCea = 0.3,
            postchirCea = 0.4,
            ldh1 = 100000,
            ldhInt1 = 500,
            af1 = 20,
            afInt1 = 300,
            af2 = 30,
            afInt2 = 400,
            af3 = 9999,
            afInt3 = 500,
        )
    }

    private fun minimalTreatment(): NcrTreatment {
        return NcrTreatment(
            deelnameStudie = null,
            tumgerichtTher = null,
            geenTherReden = 14,
            gastroenterologyResection = NcrGastroenterologyResection(
                mdlRes = null,
                mdlResType1 = null,
                mdlResType2 = null,
                mdlResInt1 = null,
                mdlResInt2 = null
            ),
            primarySurgery = NcrPrimarySurgery(
                chir = null,
                chirInt1 = null,
                chirInt2 = null,
                chirOpnameduur1 = null,
                chirOpnameduur2 = null,
                chirType1 = null,
                chirType2 = null,
                chirTech1 = null,
                chirTech2 = null,
                chirUrg1 = null,
                chirUrg2 = null,
                chirRad1 = null,
                chirRad2 = null,
                chirCrm1 = null,
                chirCrm2 = null,
                chirNaadlek1 = null,
                chirNaadlek2 = null
            ),
            metastaticSurgery = NcrMetastaticSurgery(
                metaChirCode1 = null,
                metaChirCode2 = null,
                metaChirCode3 = null,
                metaChirInt1 = null,
                metaChirInt2 = null,
                metaChirInt3 = null,
                metaChirRad1 = null,
                metaChirRad2 = null,
                metaChirRad3 = null
            ),
            primaryRadiotherapy = NcrPrimaryRadiotherapy(
                rt = 0,
                chemort = null,
                rtType1 = null,
                rtType2 = null,
                rtStartInt1 = null,
                rtStartInt2 = null,
                rtStopInt1 = null,
                rtStopInt2 = null,
                rtDosis1 = null,
                rtDosis2 = null
            ),
            metastaticRadiotherapy = NcrMetastaticRadiotherapy(
                metaRtCode1 = null,
                metaRtCode2 = null,
                metaRtCode3 = null,
                metaRtCode4 = null,
                metaRtStartInt1 = null,
                metaRtStartInt2 = null,
                metaRtStartInt3 = null,
                metaRtStartInt4 = null,
                metaRtStopInt1 = null,
                metaRtStopInt2 = null,
                metaRtStopInt3 = null,
                metaRtStopInt4 = null
            ),
            systemicTreatment = NcrSystemicTreatment(
                chemo = 0,
                target = 0,
                systCode1 = null,
                systCode2 = null,
                systCode3 = null,
                systCode4 = null,
                systCode5 = null,
                systCode6 = null,
                systCode7 = null,
                systCode8 = null,
                systCode9 = null,
                systCode10 = null,
                systCode11 = null,
                systCode12 = null,
                systCode13 = null,
                systCode14 = null,
                systPrepost1 = null,
                systPrepost2 = null,
                systPrepost3 = null,
                systPrepost4 = null,
                systPrepost5 = null,
                systPrepost6 = null,
                systPrepost7 = null,
                systPrepost8 = null,
                systPrepost9 = null,
                systPrepost10 = null,
                systPrepost11 = null,
                systPrepost12 = null,
                systPrepost13 = null,
                systPrepost14 = null,
                systSchemanum1 = null,
                systSchemanum2 = null,
                systSchemanum3 = null,
                systSchemanum4 = null,
                systSchemanum5 = null,
                systSchemanum6 = null,
                systSchemanum7 = null,
                systSchemanum8 = null,
                systSchemanum9 = null,
                systSchemanum10 = null,
                systSchemanum11 = null,
                systSchemanum12 = null,
                systSchemanum13 = null,
                systSchemanum14 = null,
                systKuren1 = null,
                systKuren2 = null,
                systKuren3 = null,
                systKuren4 = null,
                systKuren5 = null,
                systKuren6 = null,
                systKuren7 = null,
                systKuren8 = null,
                systKuren9 = null,
                systKuren10 = null,
                systKuren11 = null,
                systKuren12 = null,
                systKuren13 = null,
                systKuren14 = null,
                systStartInt1 = null,
                systStartInt2 = null,
                systStartInt3 = null,
                systStartInt4 = null,
                systStartInt5 = null,
                systStartInt6 = null,
                systStartInt7 = null,
                systStartInt8 = null,
                systStartInt9 = null,
                systStartInt10 = null,
                systStartInt11 = null,
                systStartInt12 = null,
                systStartInt13 = null,
                systStartInt14 = null,
                systStopInt1 = null,
                systStopInt2 = null,
                systStopInt3 = null,
                systStopInt4 = null,
                systStopInt5 = null,
                systStopInt6 = null,
                systStopInt7 = null,
                systStopInt8 = null,
                systStopInt9 = null,
                systStopInt10 = null,
                systStopInt11 = null,
                systStopInt12 = null,
                systStopInt13 = null,
                systStopInt14 = null
            ),
            hipec = NcrHipec(
                hipec = null,
                hipecInt1 = null
            )
        )
    }

    private fun properDiagnosisTreatment(): NcrTreatment {
        return minimalTreatment().copy(
            deelnameStudie = 0,
            tumgerichtTher = 1,
            geenTherReden = null,
            gastroenterologyResection = NcrGastroenterologyResection(
                mdlRes = 1,
                mdlResType1 = 5,
                mdlResInt1 = 10
            ),
            primarySurgery = NcrPrimarySurgery(
                chir = 1,
                chirInt1 = 20,
                chirType1 = 4
            ),
            primaryRadiotherapy = NcrPrimaryRadiotherapy(
                rt = 2,
                rtType1 = 2,
                rtStartInt1 = 24,
                rtStopInt1 = 28,
                rtDosis1 = 5.0
            )
        )
    }

    private fun properFollowup1Treatment(): NcrTreatment {
        return minimalTreatment().copy(
            deelnameStudie = 0,
            tumgerichtTher = 1,
            geenTherReden = null,
            hipec = NcrHipec(
                hipec = 1,
                hipecInt1 = 50
            )
        )
    }

    private fun properFollowup2Treatment(): NcrTreatment {
        return minimalTreatment().copy(
            deelnameStudie = 0,
            tumgerichtTher = 1,
            geenTherReden = null,
            metastaticSurgery = NcrMetastaticSurgery(
                metaChirCode1 = "123C482M",
                metaChirInt1 = 110,
                metaChirRad1 = 1
            ),
            metastaticRadiotherapy = NcrMetastaticRadiotherapy(
                metaRtCode1 = "320C34",
                metaRtStartInt1 = "120",
                metaRtStopInt1 = "130"
            ),
            systemicTreatment = NcrSystemicTreatment(
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
                systKuren4 = 66,
                systKuren5 = 77,
                systKuren6 = 98,
                systKuren7 = 99,
                systStartInt1 = 100,
                systStartInt2 = 200,
                systStartInt3 = 300,
                systStartInt4 = 400,
                systStartInt5 = 500,
                systStartInt6 = 600,
                systStartInt7 = 700,
                systStopInt1 = 180,
                systStopInt2 = 280,
                systStopInt3 = 380,
                systStopInt4 = 480,
                systStopInt5 = 580,
                systStopInt6 = 680,
                systStopInt7 = 780,
            )
        )
    }

    private fun minimalTreatmentResponse(): NcrTreatmentResponse {
        return NcrTreatmentResponse(
            responsUitslag = null,
            responsInt = null,
            pfsEvent1 = null,
            pfsEvent2 = null,
            pfsEvent3 = null,
            pfsEvent4 = null,
            fupEventType1 = null,
            fupEventType2 = null,
            fupEventType3 = null,
            fupEventType4 = null,
            pfsInt1 = null,
            pfsInt2 = null,
            pfsInt3 = null,
            pfsInt4 = null
        )
    }

    private fun properTreatmentResponse(): NcrTreatmentResponse {
        return minimalTreatmentResponse().copy(
            responsUitslag = "PD",
            responsInt = 5,
            pfsEvent1 = 1,
            pfsEvent2 = 2,
            fupEventType1 = 1,
            fupEventType2 = 2,
            pfsInt1 = 400,
            pfsInt2 = 850,
        )
    }
}