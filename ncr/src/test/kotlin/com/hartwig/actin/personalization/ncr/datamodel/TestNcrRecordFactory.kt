package com.hartwig.actin.personalization.ncr.datamodel

object TestNcrRecordFactory {

    fun minimalFollowupRecord(): NcrRecord {
        return minimalRecord()
            .copy(identification = minimalIdentification().copy(epis = "VERB"))
    }

    fun minimalDiagnosisRecord(): NcrRecord {
        return minimalRecord()
            .copy(identification = minimalIdentification().copy(epis = "DIA"))
            .copy(patientCharacteristics = minimalPatientCharacteristics().copy(vitStat = 0, vitStatInt = 200))
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

    private fun minimalIdentification(): NcrIdentification {
        return NcrIdentification(
            keyNkr = 1,
            keyZid = 1,
            keyEid = 1,
            epis = "",
            metaEpis = 0,
            teller = 0
        )
    }

    private fun minimalPatientCharacteristics(): NcrPatientCharacteristics {
        return NcrPatientCharacteristics(
            gesl = 2,
            leeft = 75,
            vitStat = null,
            vitStatInt = null,
            perfStat = null,
            asa = null
        )
    }

    private fun minimalClinicalCharacteristics(): NcrClinicalCharacteristics {
        return NcrClinicalCharacteristics(
            dubbeltum = null,
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

    private fun minimalMolecularCharacteristics(): NcrMolecularCharacteristics {
        return NcrMolecularCharacteristics(
            msiStat = null,
            brafMut = null,
            rasMut = null
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

    private fun minimalPrimaryDiagnosis(): NcrPrimaryDiagnosis {
        return NcrPrimaryDiagnosis(
            incjr = 2021,
            topoSublok = "C189",
            morfCat = null,
            diagBasis = 1,
            diffgrad = "2",
            ct = null,
            cn = null,
            cm = null,
            pt = null,
            pn = null,
            pm = null,
            cstadium = null,
            pstadium = null,
            stadium = null,
            ondLymf = null,
            posLymf = null
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

    private fun minimalTreatment(): NcrTreatment {
        return NcrTreatment(
            deelnameStudie = null,
            tumgerichtTher = null,
            geenTherReden = null,
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
                rt = 6,
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
}