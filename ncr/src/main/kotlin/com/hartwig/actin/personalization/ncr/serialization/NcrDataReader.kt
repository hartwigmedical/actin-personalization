package com.hartwig.actin.personalization.ncr.serialization

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
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors

object NcrDataReader {

    private const val FIELD_DELIMITER = ";"

    fun read(tsv: String): List<NcrRecord> {
        val lines = Files.readAllLines(File(tsv).toPath())
        val fields = createFields(lines[0].split(FIELD_DELIMITER).toTypedArray())

        return lines.subList(1, lines.size).parallelStream()
            .map { createRecord(fields, it.split(FIELD_DELIMITER).toTypedArray()) }
            .collect(Collectors.toList())
    }

    private fun createRecord(fields: Map<String, Int>, parts: Array<String>): NcrRecord {
        val extractor = NcrFieldExtractor(fields, parts)

        return NcrRecord(
            identification = readIdentification(extractor),
            patientCharacteristics = readPatientCharacteristics(extractor),
            clinicalCharacteristics = readClinicalCharacteristics(extractor),
            molecularCharacteristics = readMolecularCharacteristics(extractor),
            priorMalignancies = readPriorMalignancies(extractor),
            primaryDiagnosis = readPrimaryDiagnosis(extractor),
            metastaticDiagnosis = readMetastaticDiagnosis(extractor),
            comorbidities = readCharlsonComorbidities(extractor),
            labValues = readLabValues(extractor),
            treatment = readTreatment(extractor),
            treatmentResponse = readTreatmentResponse(extractor)
        )
    }

    private fun readIdentification(extractor: NcrFieldExtractor): NcrIdentification {
        return NcrIdentification(
            keyNkr = extractor.mandatoryInt("key_nkr"),
            keyZid = extractor.mandatoryInt("key_zid"),
            keyEid = extractor.mandatoryInt("key_eid"),
            epis = extractor.mandatoryString("epis"),
            metaEpis = extractor.mandatoryInt("meta_epis"),
            teller = extractor.mandatoryInt("teller")
        )
    }

    private fun readPatientCharacteristics(extractor: NcrFieldExtractor): NcrPatientCharacteristics {
        return NcrPatientCharacteristics(
            gesl = extractor.mandatoryInt("gesl"),
            leeft = extractor.mandatoryInt("leeft"),
            vitStat = extractor.optionalInt("vit_stat"),
            vitStatInt = extractor.optionalInt("vit_stat_int"),
            perfStat = extractor.optionalInt("perf_stat"),
            asa = extractor.optionalInt("asa")
        )
    }

    private fun readClinicalCharacteristics(extractor: NcrFieldExtractor): NcrClinicalCharacteristics {
        return NcrClinicalCharacteristics(
            dubbeltum = extractor.optionalInt("dubbeltum"),
            ileus = extractor.optionalInt("ileus"),
            perforatie = extractor.optionalInt("perforatie"),
            anusAfst = extractor.optionalInt("anus_afst"),
            mrfAfst = extractor.optionalInt("mrf_afst"),
            veneusInvas = extractor.optionalInt("veneus_invas"),
            lymfInvas = extractor.optionalInt("lymf_invas"),
            emi = extractor.optionalInt("emi"),
            tumregres = extractor.optionalInt("tumregres")
        )
    }

    private fun readMolecularCharacteristics(extractor: NcrFieldExtractor): NcrMolecularCharacteristics {
        return NcrMolecularCharacteristics(
            msiStat = extractor.optionalInt("msi_stat"),
            brafMut = extractor.optionalInt("braf_mut"),
            rasMut = extractor.optionalInt("ras_mut")
        )
    }

    private fun readPriorMalignancies(extractor: NcrFieldExtractor): NcrPriorMalignancies {
        return NcrPriorMalignancies(
            mal1Int = extractor.optionalInt("mal1_int"),
            mal2Int = extractor.optionalInt("mal2_int"),
            mal3Int = extractor.optionalInt("mal3_int"),
            mal4Int = extractor.optionalInt("mal4_int"),
            mal1TopoSublok = extractor.optionalString("mal1_topo_sublok"),
            mal2TopoSublok = extractor.optionalString("mal2_topo_sublok"),
            mal3TopoSublok = extractor.optionalString("mal3_topo_sublok"),
            mal4TopoSublok = extractor.optionalString("mal4_topo_sublok"),
            mal1Morf = extractor.optionalInt("mal1_morf"),
            mal2Morf = extractor.optionalInt("mal2_morf"),
            mal3Morf = extractor.optionalInt("mal3_morf"),
            mal4Morf = extractor.optionalInt("mal4_morf"),
            mal1Tumsoort = extractor.optionalInt("mal1_tumsoort"),
            mal2Tumsoort = extractor.optionalInt("mal2_tumsoort"),
            mal3Tumsoort = extractor.optionalInt("mal3_tumsoort"),
            mal4Tumsoort = extractor.optionalInt("mal4_tumsoort"),
            mal1Stadium = extractor.optionalString("mal1_stadium"),
            mal2Stadium = extractor.optionalString("mal2_stadium"),
            mal3Stadium = extractor.optionalString("mal3_stadium"),
            mal4Stadium = extractor.optionalString("mal4_stadium"),
            mal1Syst = extractor.optionalInt("mal1_syst"),
            mal2Syst = extractor.optionalInt("mal2_syst"),
            mal3Syst = extractor.optionalInt("mal3_syst"),
            mal4Syst = extractor.optionalInt("mal4_syst"),
            mal1SystCode1 = extractor.optionalString("mal1_syst_code1"),
            mal2SystCode1 = extractor.optionalString("mal2_syst_code1"),
            mal3SystCode1 = extractor.optionalString("mal3_syst_code1"),
            mal1SystCode2 = extractor.optionalString("mal1_syst_code2"),
            mal2SystCode2 = extractor.optionalString("mal2_syst_code2"),
            mal3SystCode2 = extractor.optionalString("mal3_syst_code2"),
            mal1SystCode3 = extractor.optionalString("mal1_syst_code3"),
            mal2SystCode3 = extractor.optionalString("mal2_syst_code3"),
            mal3SystCode3 = extractor.optionalString("mal3_syst_code3"),
            mal1SystCode4 = extractor.optionalString("mal1_syst_code4"),
            mal2SystCode4 = extractor.optionalString("mal2_syst_code4"),
            mal3SystCode4 = extractor.optionalString("mal3_syst_code4"),
            mal1SystCode5 = extractor.optionalString("mal1_syst_code5"),
            mal2SystCode5 = extractor.optionalString("mal2_syst_code5"),
            mal1SystCode6 = extractor.optionalString("mal1_syst_code6"),
            mal1SystCode7 = extractor.optionalString("mal1_syst_code7"),
            mal1SystCode8 = extractor.optionalString("mal1_syst_code8"),
            mal1SystCode9 = extractor.optionalString("mal1_syst_code9"),
        )
    }

    private fun readPrimaryDiagnosis(extractor: NcrFieldExtractor): NcrPrimaryDiagnosis {
        return NcrPrimaryDiagnosis(
            incjr = extractor.mandatoryInt("incjr"),
            topoSublok = extractor.mandatoryString("topo_sublok"),
            morfCat = extractor.optionalInt("morf_cat"),
            diagBasis = extractor.mandatoryInt("diag_basis"),
            diffgrad = extractor.mandatoryString("diffgrad"),
            ct = extractor.mandatoryString("ct"),
            cn = extractor.mandatoryString("cn"),
            cm = extractor.mandatoryString("cm"),
            pt = extractor.optionalString("pt"),
            pn = extractor.optionalString("pn"),
            pm = extractor.optionalString("pm"),
            cstadium = extractor.optionalString("cstadium"),
            pstadium = extractor.optionalString("pstadium"),
            stadium = extractor.optionalString("stadium"),
            ondLymf = extractor.optionalInt("ond_lymf"),
            posLymf = extractor.optionalInt("pos_lymf")
        )
    }

    private fun readMetastaticDiagnosis(extractor: NcrFieldExtractor): NcrMetastaticDiagnosis {
        return NcrMetastaticDiagnosis(
            metaTopoSublok1 = extractor.optionalString("meta_topo_sublok1"),
            metaTopoSublok2 = extractor.optionalString("meta_topo_sublok2"),
            metaTopoSublok3 = extractor.optionalString("meta_topo_sublok3"),
            metaTopoSublok4 = extractor.optionalString("meta_topo_sublok4"),
            metaTopoSublok5 = extractor.optionalString("meta_topo_sublok5"),
            metaTopoSublok6 = extractor.optionalString("meta_topo_sublok6"),
            metaTopoSublok7 = extractor.optionalString("meta_topo_sublok7"),
            metaTopoSublok8 = extractor.optionalString("meta_topo_sublok8"),
            metaTopoSublok9 = extractor.optionalString("meta_topo_sublok9"),
            metaTopoSublok10 = extractor.optionalString("meta_topo_sublok10"),
            metaInt1 = extractor.optionalInt("meta_int1"),
            metaInt2 = extractor.optionalInt("meta_int2"),
            metaInt3 = extractor.optionalInt("meta_int3"),
            metaInt4 = extractor.optionalInt("meta_int4"),
            metaInt5 = extractor.optionalInt("meta_int5"),
            metaInt6 = extractor.optionalInt("meta_int6"),
            metaInt7 = extractor.optionalInt("meta_int7"),
            metaInt8 = extractor.optionalInt("meta_int8"),
            metaInt9 = extractor.optionalInt("meta_int9"),
            metaInt10 = extractor.optionalInt("meta_int10"),
            metaProg1 = extractor.optionalInt("meta_prog1"),
            metaProg2 = extractor.optionalInt("meta_prog2"),
            metaProg3 = extractor.optionalInt("meta_prog3"),
            metaProg4 = extractor.optionalInt("meta_prog4"),
            metaProg5 = extractor.optionalInt("meta_prog5"),
            metaProg6 = extractor.optionalInt("meta_prog6"),
            metaProg7 = extractor.optionalInt("meta_prog7"),
            metaProg8 = extractor.optionalInt("meta_prog8"),
            metaProg9 = extractor.optionalInt("meta_prog9"),
            metaProg10 = extractor.optionalInt("meta_prog10"),
            metaLeverAantal = extractor.optionalInt("meta_lever_aantal"),
            metaLeverAfm = extractor.optionalInt("meta_lever_afm")
        )
    }

    private fun readCharlsonComorbidities(extractor: NcrFieldExtractor): NcrCharlsonComorbidities {
        return NcrCharlsonComorbidities(
            cci = extractor.optionalInt("cci"),
            cciAids = extractor.optionalInt("cci_aids"),
            cciCat = extractor.optionalInt("cci_cat"),
            cciChf = extractor.optionalInt("cci_chf"),
            cciCollagenosis = extractor.optionalInt("cci_collagenosis"),
            cciCopd = extractor.optionalInt("cci_copd"),
            cciCvd = extractor.optionalInt("cci_cvd"),
            cciDementia = extractor.optionalInt("cci_dementia"),
            cciDm = extractor.optionalInt("cci_dm"),
            cciEodDm = extractor.optionalInt("cci_eod_dm"),
            cciMalignancy = extractor.optionalInt("cci_malignancy"),
            cciMetastatic = extractor.optionalInt("cci_metastatic"),
            cciMi = extractor.optionalInt("cci_mi"),
            cciMildLiver = extractor.optionalInt("cci_mild_liver"),
            cciPlegia = extractor.optionalInt("cci_plegia"),
            cciPvd = extractor.optionalInt("cci_pvd"),
            cciRenal = extractor.optionalInt("cci_renal"),
            cciSevereLiver = extractor.optionalInt("cci_severe_liver"),
            cciUlcer = extractor.optionalInt("cci_ulcer")
        )
    }

    private fun readLabValues(extractor: NcrFieldExtractor): NcrLabValues {
        return NcrLabValues(
            prechirCea = extractor.optionalDouble("prechir_cea"),
            postchirCea = extractor.optionalDouble("postchir_cea"),
            ldh1 = extractor.optionalInt("ldh1"),
            ldh2 = extractor.optionalInt("ldh2"),
            ldh3 = extractor.optionalInt("ldh3"),
            ldh4 = extractor.optionalInt("ldh4"),
            ldhInt1 = extractor.optionalInt("ldh_int1"),
            ldhInt2 = extractor.optionalInt("ldh_int2"),
            ldhInt3 = extractor.optionalInt("ldh_int3"),
            ldhInt4 = extractor.optionalInt("ldh_int4"),
            af1 = extractor.optionalInt("af1"),
            af2 = extractor.optionalInt("af2"),
            af3 = extractor.optionalInt("af3"),
            af4 = extractor.optionalInt("af4"),
            afInt1 = extractor.optionalInt("af_int1"),
            afInt2 = extractor.optionalInt("af_int2"),
            afInt3 = extractor.optionalInt("af_int3"),
            afInt4 = extractor.optionalInt("af_int4"),
            neutro1 = extractor.optionalDouble("neutro1"),
            neutro2 = extractor.optionalDouble("neutro2"),
            neutro3 = extractor.optionalDouble("neutro3"),
            neutro4 = extractor.optionalDouble("neutro4"),
            neutroInt1 = extractor.optionalInt("neutro_int1"),
            neutroInt2 = extractor.optionalInt("neutro_int2"),
            neutroInt3 = extractor.optionalInt("neutro_int3"),
            neutroInt4 = extractor.optionalInt("neutro_int4"),
            albumine1 = extractor.optionalDouble("albumine1"),
            albumine2 = extractor.optionalDouble("albumine2"),
            albumine3 = extractor.optionalDouble("albumine3"),
            albumine4 = extractor.optionalInt("albumine4"),
            albumineInt1 = extractor.optionalInt("albumine_int1"),
            albumineInt2 = extractor.optionalInt("albumine_int2"),
            albumineInt3 = extractor.optionalInt("albumine_int3"),
            albumineInt4 = extractor.optionalInt("albumine_int4"),
            leuko1 = extractor.optionalDouble("leuko1"),
            leuko2 = extractor.optionalDouble("leuko2"),
            leuko3 = extractor.optionalDouble("leuko3"),
            leuko4 = extractor.optionalDouble("leuko4"),
            leukoInt1 = extractor.optionalInt("leuko_int1"),
            leukoInt2 = extractor.optionalInt("leuko_int2"),
            leukoInt3 = extractor.optionalInt("leuko_int3"),
            leukoInt4 = extractor.optionalInt("leuko_int4")
        )
    }

    private fun readTreatment(extractor: NcrFieldExtractor): NcrTreatment {
        return NcrTreatment(
            deelnameStudie = extractor.optionalInt("deelname_studie"),
            tumgerichtTher = extractor.optionalInt("tumgericht_ther"),
            geenTherReden = extractor.optionalInt("geen_ther_reden"),
            gastroenterologyResection = readGastroenterologyResection(extractor),
            primarySurgery = readPrimarySurgery(extractor),
            metastaticSurgery = readMetastaticSurgery(extractor),
            primaryRadiotherapy = readPrimaryRadiotherapy(extractor),
            metastaticRadiotherapy = readMetastaticRadiotherapy(extractor),
            systemicTreatment = readSystemicTreatment(extractor),
            hipec = readHIPEC(extractor)
        )
    }

    private fun readGastroenterologyResection(extractor: NcrFieldExtractor): NcrGastroenterologyResection {
        return NcrGastroenterologyResection(
            mdlRes = extractor.optionalInt("mdl_res"),
            mdlResType1 = extractor.optionalInt("mdl_res_type1"),
            mdlResType2 = extractor.optionalInt("mdl_res_type2"),
            mdlResInt1 = extractor.optionalInt("mdl_res_int1"),
            mdlResInt2 = extractor.optionalInt("mdl_res_int2")
        )
    }

    private fun readPrimarySurgery(extractor: NcrFieldExtractor): NcrPrimarySurgery {
        return NcrPrimarySurgery(
            chir = extractor.optionalInt("chir"),
            chirInt1 = extractor.optionalInt("chir_int1"),
            chirInt2 = extractor.optionalInt("chir_int2"),
            chirOpnameduur1 = extractor.optionalInt("chir_opnameduur1"),
            chirOpnameduur2 = extractor.optionalInt("chir_opnameduur2"),
            chirType1 = extractor.optionalInt("chir_type1"),
            chirType2 = extractor.optionalInt("chir_type2"),
            chirTech1 = extractor.optionalInt("chir_tech1"),
            chirTech2 = extractor.optionalInt("chir_tech2"),
            chirUrg1 = extractor.optionalInt("chir_urg1"),
            chirUrg2 = extractor.optionalInt("chir_urg2"),
            chirRad1 = extractor.optionalInt("chir_rad1"),
            chirRad2 = extractor.optionalInt("chir_rad2"),
            chirCrm1 = extractor.optionalInt("chir_crm1"),
            chirCrm2 = extractor.optionalInt("chir_crm2"),
            chirNaadlek1 = extractor.optionalInt("chir_naadlek1"),
            chirNaadlek2 = extractor.optionalInt("chir_naadlek2")
        )
    }

    private fun readMetastaticSurgery(extractor: NcrFieldExtractor): NcrMetastaticSurgery {
        return NcrMetastaticSurgery(
            metaChirCode1 = extractor.optionalString("meta_chir_code1"),
            metaChirCode2 = extractor.optionalString("meta_chir_code2"),
            metaChirCode3 = extractor.optionalString("meta_chir_code3"),
            metaChirInt1 = extractor.optionalInt("meta_chir_int1"),
            metaChirInt2 = extractor.optionalInt("meta_chir_int2"),
            metaChirInt3 = extractor.optionalInt("meta_chir_int3"),
            metaChirRad1 = extractor.optionalInt("meta_chir_rad1"),
            metaChirRad2 = extractor.optionalInt("meta_chir_rad2"),
            metaChirRad3 = extractor.optionalInt("meta_chir_rad3")
        )
    }

    private fun readPrimaryRadiotherapy(extractor: NcrFieldExtractor): NcrPrimaryRadiotherapy {
        return NcrPrimaryRadiotherapy(
            rt = extractor.mandatoryInt("rt"),
            chemort = extractor.optionalInt("chemort"),
            rtType1 = extractor.optionalInt("rt_type1"),
            rtType2 = extractor.optionalInt("rt_type2"),
            rtStartInt1 = extractor.optionalInt("rt_start_int1"),
            rtStartInt2 = extractor.optionalInt("rt_start_int2"),
            rtStopInt1 = extractor.optionalInt("rt_stop_int1"),
            rtStopInt2 = extractor.optionalInt("rt_stop_int2"),
            rtDosis1 = extractor.optionalDouble("rt_dosis1"),
            rtDosis2 = extractor.optionalDouble("rt_dosis2"),
        )
    }

    private fun readMetastaticRadiotherapy(extractor: NcrFieldExtractor): NcrMetastaticRadiotherapy {
        return NcrMetastaticRadiotherapy(
            metaRtCode1 = extractor.optionalString("meta_rt_code1"),
            metaRtCode2 = extractor.optionalString("meta_rt_code2"),
            metaRtCode3 = extractor.optionalString("meta_rt_code3"),
            metaRtCode4 = extractor.optionalString("meta_rt_code4"),
            metaRtStartInt1 = extractor.optionalString("meta_rt_start_int1"),
            metaRtStartInt2 = extractor.optionalString("meta_rt_start_int2"),
            metaRtStartInt3 = extractor.optionalString("meta_rt_start_int3"),
            metaRtStartInt4 = extractor.optionalString("meta_rt_start_int4"),
            metaRtStopInt1 = extractor.optionalString("meta_rt_stop_int1"),
            metaRtStopInt2 = extractor.optionalString("meta_rt_stop_int2"),
            metaRtStopInt3 = extractor.optionalString("meta_rt_stop_int3"),
            metaRtStopInt4 = extractor.optionalString("meta_rt_stop_int4")
        )
    }

    private fun readSystemicTreatment(extractor: NcrFieldExtractor): NcrSystemicTreatment {
        return NcrSystemicTreatment(
            chemo = extractor.mandatoryInt("chemo"),
            target = extractor.mandatoryInt("target"),
            systCode1 = extractor.optionalString("syst_code1"),
            systCode2 = extractor.optionalString("syst_code2"),
            systCode3 = extractor.optionalString("syst_code3"),
            systCode4 = extractor.optionalString("syst_code4"),
            systCode5 = extractor.optionalString("syst_code5"),
            systCode6 = extractor.optionalString("syst_code6"),
            systCode7 = extractor.optionalString("syst_code7"),
            systCode8 = extractor.optionalString("syst_code8"),
            systCode9 = extractor.optionalString("syst_code9"),
            systCode10 = extractor.optionalString("syst_code10"),
            systCode11 = extractor.optionalString("syst_code11"),
            systCode12 = extractor.optionalString("syst_code12"),
            systCode13 = extractor.optionalString("syst_code13"),
            systCode14 = extractor.optionalString("syst_code14"),
            systPrepost1 = extractor.optionalInt("syst_prepost1"),
            systPrepost2 = extractor.optionalInt("syst_prepost2"),
            systPrepost3 = extractor.optionalInt("syst_prepost3"),
            systPrepost4 = extractor.optionalInt("syst_prepost4"),
            systPrepost5 = extractor.optionalInt("syst_prepost5"),
            systPrepost6 = extractor.optionalInt("syst_prepost6"),
            systPrepost7 = extractor.optionalInt("syst_prepost7"),
            systPrepost8 = extractor.optionalInt("syst_prepost8"),
            systPrepost9 = extractor.optionalInt("syst_prepost9"),
            systPrepost10 = extractor.optionalInt("syst_prepost10"),
            systPrepost11 = extractor.optionalInt("syst_prepost11"),
            systPrepost12 = extractor.optionalInt("syst_prepost12"),
            systPrepost13 = extractor.optionalInt("syst_prepost13"),
            systPrepost14 = extractor.optionalInt("syst_prepost14"),
            systSchemanum1 = extractor.optionalInt("syst_schemanum1"),
            systSchemanum2 = extractor.optionalInt("syst_schemanum2"),
            systSchemanum3 = extractor.optionalInt("syst_schemanum3"),
            systSchemanum4 = extractor.optionalInt("syst_schemanum4"),
            systSchemanum5 = extractor.optionalInt("syst_schemanum5"),
            systSchemanum6 = extractor.optionalInt("syst_schemanum6"),
            systSchemanum7 = extractor.optionalInt("syst_schemanum7"),
            systSchemanum8 = extractor.optionalInt("syst_schemanum8"),
            systSchemanum9 = extractor.optionalInt("syst_schemanum9"),
            systSchemanum10 = extractor.optionalInt("syst_schemanum10"),
            systSchemanum11 = extractor.optionalInt("syst_schemanum11"),
            systSchemanum12 = extractor.optionalInt("syst_schemanum12"),
            systSchemanum13 = extractor.optionalInt("syst_schemanum13"),
            systSchemanum14 = extractor.optionalInt("syst_schemanum14"),
            systKuren1 = extractor.optionalInt("syst_kuren1"),
            systKuren2 = extractor.optionalInt("syst_kuren2"),
            systKuren3 = extractor.optionalInt("syst_kuren3"),
            systKuren4 = extractor.optionalInt("syst_kuren4"),
            systKuren5 = extractor.optionalInt("syst_kuren5"),
            systKuren6 = extractor.optionalInt("syst_kuren6"),
            systKuren7 = extractor.optionalInt("syst_kuren7"),
            systKuren8 = extractor.optionalInt("syst_kuren8"),
            systKuren9 = extractor.optionalInt("syst_kuren9"),
            systKuren10 = extractor.optionalInt("syst_kuren10"),
            systKuren11 = extractor.optionalInt("syst_kuren11"),
            systKuren12 = extractor.optionalInt("syst_kuren12"),
            systKuren13 = extractor.optionalInt("syst_kuren13"),
            systKuren14 = extractor.optionalInt("syst_kuren14"),
            systStartInt1 = extractor.optionalInt("syst_start_int1"),
            systStartInt2 = extractor.optionalInt("syst_start_int2"),
            systStartInt3 = extractor.optionalInt("syst_start_int3"),
            systStartInt4 = extractor.optionalInt("syst_start_int4"),
            systStartInt5 = extractor.optionalInt("syst_start_int5"),
            systStartInt6 = extractor.optionalInt("syst_start_int6"),
            systStartInt7 = extractor.optionalInt("syst_start_int7"),
            systStartInt8 = extractor.optionalInt("syst_start_int8"),
            systStartInt9 = extractor.optionalInt("syst_start_int9"),
            systStartInt10 = extractor.optionalInt("syst_start_int10"),
            systStartInt11 = extractor.optionalInt("syst_start_int11"),
            systStartInt12 = extractor.optionalInt("syst_start_int12"),
            systStartInt13 = extractor.optionalInt("syst_start_int13"),
            systStartInt14 = extractor.optionalInt("syst_start_int14"),
            systStopInt1 = extractor.optionalInt("syst_stop_int1"),
            systStopInt2 = extractor.optionalInt("syst_stop_int2"),
            systStopInt3 = extractor.optionalInt("syst_stop_int3"),
            systStopInt4 = extractor.optionalInt("syst_stop_int4"),
            systStopInt5 = extractor.optionalInt("syst_stop_int5"),
            systStopInt6 = extractor.optionalInt("syst_stop_int6"),
            systStopInt7 = extractor.optionalInt("syst_stop_int7"),
            systStopInt8 = extractor.optionalInt("syst_stop_int8"),
            systStopInt9 = extractor.optionalInt("syst_stop_int9"),
            systStopInt10 = extractor.optionalInt("syst_stop_int10"),
            systStopInt11 = extractor.optionalInt("syst_stop_int11"),
            systStopInt12 = extractor.optionalInt("syst_stop_int12"),
            systStopInt13 = extractor.optionalInt("syst_stop_int13"),
            systStopInt14 = extractor.optionalInt("syst_stop_int14")
        )
    }

    private fun readHIPEC(extractor: NcrFieldExtractor): NcrHipec {
        return NcrHipec(
            hipec = extractor.optionalInt("hipec"),
            hipecInt1 = extractor.optionalInt("hipec_int1")
        )
    }

    private fun readTreatmentResponse(extractor: NcrFieldExtractor): NcrTreatmentResponse {
        return NcrTreatmentResponse(
            responsUitslag = extractor.optionalString("respons_uitslag"),
            responsInt = extractor.optionalInt("respons_int"),
            pfsEvent1 = extractor.optionalInt("pfs_event1"),
            pfsEvent2 = extractor.optionalInt("pfs_event2"),
            pfsEvent3 = extractor.optionalInt("pfs_event3"),
            pfsEvent4 = extractor.optionalInt("pfs_event4"),
            fupEventType1 = extractor.optionalInt("fup_event_type1"),
            fupEventType2 = extractor.optionalInt("fup_event_type2"),
            fupEventType3 = extractor.optionalInt("fup_event_type3"),
            fupEventType4 = extractor.optionalInt("fup_event_type4"),
            pfsInt1 = extractor.optionalInt("pfs_int1"),
            pfsInt2 = extractor.optionalInt("pfs_int2"),
            pfsInt3 = extractor.optionalInt("pfs_int3"),
            pfsInt4 = extractor.optionalInt("pfs_int4")
        )
    }

    private fun createFields(header: Array<String>): Map<String, Int> {
        return header.withIndex().associate { (i, field) -> field to i }
    }
}