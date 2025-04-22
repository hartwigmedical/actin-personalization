package com.hartwig.actin.personalization.cairo.serialization

import com.hartwig.actin.personalization.cairo.datamodel.*
import java.io.File
import java.nio.file.Files

object CairoDataReader {

    fun read(folderPath: String): List<CairoRecord> {
        val folder = File(folderPath)
        if (!folder.exists() || !folder.isDirectory) {
            throw IllegalArgumentException("Provided path is not a valid directory: $folderPath")
        }

        val csvFiles = folder.listFiles { _, name -> name.endsWith(".csv") } ?: arrayOf()
        val records = mutableListOf<CairoRecord>()

        val c2Files = csvFiles.filter{ it.name.startsWith("C2") }
        val c3Files = csvFiles.filter{ it.name.startsWith("C3") }

        val aggregatedC2Data = aggregateCsvFiles(c2Files)
        val aggregatedC3Data = aggregateCsvFiles(c3Files)

        aggregatedC2Data.values.forEach { aggregatedRow ->
            val extractor = CairoFieldExtractor(aggregatedRow)
            records.add(createRecordFromExtractor(extractor, 2))
        }

        aggregatedC3Data.values.forEach { aggregatedRow ->
            val extractor = CairoFieldExtractor(aggregatedRow)
            records.add(createRecordFromExtractor(extractor, 3))
        }

        return records
    }


    fun aggregateCsvFiles(csvFiles: List<File>, fieldDelimiter: String = ";"): Map<String, MutableMap<String, String>> {
        val aggregatedData = mutableMapOf<String, MutableMap<String, String>>()

        csvFiles.forEach { file ->
            val lines = Files.readAllLines(file.toPath())
            if (lines.isEmpty()) return@forEach

            val header = lines.first().split(fieldDelimiter).map { it.trim() }

            lines.drop(1).forEach { line ->
                val parts = line.split(fieldDelimiter)
                val rowMap = header.zip(parts).toMap()
                val patnr = rowMap["patnr"] ?: throw IllegalArgumentException("Missing 'patnr' in file: ${file.name}")

                val aggregatedRow = aggregatedData.getOrPut(patnr) { mutableMapOf() }
                aggregatedRow.putAll(rowMap)
            }
        }
        return aggregatedData
    }

    private fun createRecordFromExtractor(extractor: CairoFieldExtractor, cairoStudy: Int): CairoRecord {
        return CairoRecord(
            identification = readIdentification(extractor, cairoStudy),
            patientCharacteristics = readPatientCharacteristics(extractor, cairoStudy),
            molecularCharacteristics = readMolecularCharacteristics(extractor, cairoStudy),
            primaryDiagnosis = readPrimaryDiagnosis(extractor, cairoStudy),
            metastaticDiagnosis = readMetastaticDiagnosis(extractor, cairoStudy),
            labValues = readLabValues(extractor, cairoStudy),
            treatment = readTreatment(extractor, cairoStudy),
            treatmentResponse = readTreatmentResponse(extractor, cairoStudy),
       )
    }

    private fun readIdentification(extractor: CairoFieldExtractor, cairoStudy:Int): CairoIdentification {
        return CairoIdentification(
            cairoStudy = cairoStudy,
            patnr = extractor.mandatoryString("patnr"),
        )
    }

    private fun readPatientCharacteristics(extractor: CairoFieldExtractor, cairoStudy: Int): CairoPatientCharacteristics {
        if (cairoStudy == 2) {
            return CairoPatientCharacteristics(
                sex = extractor.mandatoryInt("Gender"),
                age = extractor.optionalDouble("age"),
                whoStat = extractor.optionalInt("who"),
                weight = extractor.optionalInt("Weight"),
                height = extractor.optionalInt("Height"),
                birthdate = null,
                smoking = null,
                comorbidities = extractor.optionalInt("concomittantDisease"),
                comorbiditiesSpecification = extractor.optionalInt("concomittantDiseaseComments")
            )
        }
        if (cairoStudy == 3){
            return CairoPatientCharacteristics(
                sex = extractor.mandatoryInt("sex"),
                age = 50, //TODO: Should be extracted from birthdate
                whoStat = extractor.optionalInt("who"),
                weight = extractor.optionalInt("weight"),
                height = extractor.optionalInt("height"),
                birthdate = extractor.optionalString("birthdate"),
                smoking = extractor.optionalInt("smoking"),
                comorbidities = extractor.optionalInt("comorbidities"),
                comorbiditiesSpecification = extractor.optionalString("comorbiditiesSpecification")
            )
        }
        else {
            throw IllegalArgumentException("Unknown Cairo study: $cairoStudy")
        }
    }
    private fun readMolecularCharacteristics(extractor: CairoFieldExtractor, cairoStudy:Int): CairoMolecularCharacteristics {
        if (cairoStudy == 2) {
            return CairoMolecularCharacteristics(
                BRAF = extractor.optionalInt("BRAF"),
                BRAF_mutation = extractor.optionalString("BRAF_mut"),
                KRAS = extractor.optionalInt("KRAS"),
                KRAS_mutation = extractor.optionalString("KRAS_mut"),
                NRAS = extractor.optionalInt("NRAS"),
                NRAS_mutation = extractor.optionalString("NRAS_mut"),
            )
        }
        if (cairoStudy == 3){
            return CairoMolecularCharacteristics(
                BRAF = extractor.optionalInt("BRAFV600E.combined"),
                BRAF_mutation = null,
                KRAS = extractor.optionalInt("KRAS.combined"),
                KRAS_mutation = null,
                NRAS = null,
                NRAS_mutation = null,
            )
        }
        else {
            throw IllegalArgumentException("Unknown Cairo study: $cairoStudy")
        }
    }

    private fun readPrimaryDiagnosis(extractor: CairoFieldExtractor, cairoStudy: Int): CairoPrimaryDiagnosis {
        if (cairoStudy == 2){
            return CairoPrimaryDiagnosis(
                dateDiagnosis = extractor.optionalString("dateOfHistologicalDiagnosis"),
                sitePrimaryTumor = extractor.optionalString("primaryTumorSite"),
                sitePrimaryTumorComments = null,
                sitePrimaryTumorInvolved = null,
                sitePrimaryTumorMeasurableLesion = null,
                sitePrimaryTumorMethod = null,
                tnmClassification = null
            )
        }
        if (cairoStudy == 3){
            return CairoPrimaryDiagnosis(
                dateDiagnosis = extractor.optionalString("dateDiagnosis"),
                sitePrimaryTumor = extractor.optionalString("sitePrimaryTumor"),
                sitePrimaryTumorComments = extractor.optionalString("primaryTumorSiteComments"),
                sitePrimaryTumorInvolved = extractor.optionalString("primaryTumorSiteInvolved"),
                sitePrimaryTumorMeasurableLesion = extractor.optionalString("primaryTumorSiteLesionMeasurable"),
                sitePrimaryTumorMethod = extractor.optionalString("primaryTumorSiteMethod"),
                tnmClassification = extractor.optionalString("tnmClassification")
            )
        }
        else {
            throw IllegalArgumentException("Unknown Cairo study: $cairoStudy")
        }
    }

    private fun readMetastaticDiagnosis(extractor: CairoFieldExtractor, cairoStudy: Int): CairoMetastaticDiagnosis {
        if (cairoStudy == 2) {
            return CairoMetastaticDiagnosis(
                dateFirstMetastasis = extractor.optionalString("dateOfDistantMetastasis"),
                baselineSumLongestDiameters = null,
                lymphNodeMetastases = null,
                lungMetastases = null,
                liverMetastases = null,
                skinMetastases = null,
                softTissueMetastases = null,
                boneMetastases = null,
                ascitisMetastases = null,
                pleuralEffusionMetastases = null,
                otherSiteMetastases = null
            )
        }
        if (cairoStudy == 3) {
            return CairoMetastaticDiagnosis(
                dateFirstMetastasis = extractor.optionalString("TMDTCINMEASDT"),
                baselineSumLongestDiameters = extractor.optionalInt("baselineSumLongestDiameters"),
                lymphNodeMetastases = CairoMetastasesLymphNode(
                    lymphNodeMethod = extractor.optionalInt("lymphNodeMethod"),
                    lymphNodeInvolved = extractor.optionalInt("lymphNodeInvolved"),
                    lymphNodeLesionMeasurable = extractor.optionalInt("lymphNodeLesionMeasurable"),
                    lymphNodeComments = extractor.optionalString("lymphNodeComments")
                ),
                lungMetastases = CairoMetastasesLung(
                    lungMethod = extractor.optionalInt("lungMethod"),
                    lungInvolved = extractor.optionalInt("lungInvolved"),
                    lungLesionMeasurable = extractor.optionalInt("lungLesionMeasurable"),
                    lungLesionComments = extractor.optionalString("lungLesionComments")
                ),
                liverMetastases = CairoMetastasesLiver(
                    liverMethod = extractor.optionalInt("liverMethod"),
                    liverInvolved = extractor.optionalInt("liverInvolved"),
                    liverLesionMeasurable = extractor.optionalInt("liverLesionMeasurable"),
                    liverComments = extractor.optionalString("liverComments")
                ),
                skinMetastases = CairoMetastasesSkin(
                    skinMethod = extractor.optionalInt("skinMethod"),
                    skinInvolved = extractor.optionalInt("skinInvolved"),
                    skinLesionMeasurable = extractor.optionalInt("skinLesionMeasurable"),
                    skinComments = extractor.optionalString("skinLesionComments")
                ),
                softTissueMetastases = CairoMetastasesSoftTissue(
                    softTissueMethod = extractor.optionalInt("softTissueMethod"),
                    softTissueInvolved = extractor.optionalInt("softTissueInvolved"),
                    softTissueLesionMeasurable = extractor.optionalInt("softTissueLesionMeasurable"),
                    softTissueComments = extractor.optionalString("softTissueComments")
                ),
                boneMetastases = CairoMetastasesBone(
                    boneMethod = extractor.optionalInt("boneMethod"),
                    boneInvolved = extractor.optionalInt("boneInvolved"),
                    boneComments = extractor.optionalString("boneComments")
                ),
                ascitisMetastases = CairoMetastasesAscitis(
                    ascitisMethod = extractor.optionalInt("ascitisMethod"),
                    ascitisInvolved = extractor.optionalInt("ascitisInvolved"),
                    ascitisMalignancyRelated = extractor.optionalInt("ascitisMalignancyRelated")
                ),
                pleuralEffusionMetastases = CairoMetastasesPleuralEffusion(
                    pleuralEffusionMethod = extractor.optionalInt("pleuralEffusionMethod"),
                    pleuralEffusionInvolved = extractor.optionalInt("pleuralEffusionInvolved"),
                    pleuralEffusionMalignancyRelated = extractor.optionalInt("pleuralEffusionMalignancyRelated"),
                ),
                otherSiteMetastases = CairoMetastasesOther(
                    otherMethod = extractor.optionalInt("otherMethod"),
                    otherInvolved = extractor.optionalInt("otherInvolved"),
                    otherSpecification = extractor.optionalString("otherSpecification"),
                )
            )
        } else {
            throw IllegalArgumentException("Unknown Cairo study: $cairoStudy")
        }
    }

    private fun readLabValues(extractor: CairoFieldExtractor, cairoStudy: Int): CairoLabValues {
        if(cairoStudy == 2) {
            return CairoLabValues(
                HaematologyValues = CairoHaematologyValues(
                    labHaematologySampleDate = extractor.optionalString("Date of sample hematology"),
                    WBC = extractor.optionalDouble("WBC, value"),
                    neutro = extractor.optionalDouble("Neutrophils, value"),
                    platelets = extractor.optionalDouble("Platelets, value"),
                    hemoglobin = null,
                ),
                BiochemistryValues = CairoBiochemistryValues(
                    labBiochemistrySampleDate = null,
                    CRP = null,
                    alkalinePhosphatase = extractor.optionalDouble("Alkaline Phosphatase, value"),
                    bilirubin = extractor.optionalDouble("Bilirubin, value"),
                    ASAT = extractor.optionalDouble("ASAT, value"),
                    ALAT = extractor.optionalDouble("ALAT, value"),
                    creatinin = extractor.optionalDouble("Serum creatinine, value"),
                    creatininClearance = null,
                    natrium = extractor.optionalDouble("Na+, value"),
                    kalium = extractor.optionalDouble("K+, value"),
                    calcium = extractor.optionalDouble("Calcium, value"),
                    phosphatase = extractor.optionalDouble("Phosphate, value"),
                    albumin = extractor.optionalDouble("Albumin, value"),
                    ldh = extractor.optionalDouble("LDH, value"),
                    magnesium = extractor.optionalDouble("Magnesium, value"),
                    CEA = extractor.optionalDouble("CEA, value"),
                ),
                UrinalysisValues = CairoUrinalysisValues(
                    labUrinalysisSampleDate = extractor.optionalString("Date of sample urinalysis"),
                    proteinDipstick = extractor.optionalInt("Dipstick for protein"),
                    proteinG24H = extractor.optionalDouble("Protein, value"),
                    proteinGL = null
                ),
                labComments = null
            )
        }
        if(cairoStudy == 3) {
            return CairoLabValues(
                HaematologyValues = CairoHaematologyValues(
                    labHaematologySampleDate = extractor.optionalString("labHaematologySampleDate"),
                    WBC = extractor.optionalDouble("WBC"),
                    neutro = extractor.optionalDouble("neutrophils"),
                    platelets = extractor.optionalDouble("platelets"),
                    hemoglobin = extractor.optionalDouble("hemoglobin"),
                ),
                BiochemistryValues = CairoBiochemistryValues(
                    labBiochemistrySampleDate = extractor.optionalString("labBiochemistrySampleDate"),
                    CRP = extractor.optionalDouble("CRP"),
                    alkalinePhosphatase = extractor.optionalDouble("alkalinePhosphatase"),
                    bilirubin = extractor.optionalDouble("totalBilirubin"),
                    ASAT = extractor.optionalDouble("ASAT"),
                    ALAT = extractor.optionalDouble("ALAT"),
                    creatinin = extractor.optionalDouble("creatinin"),
                    creatininClearance = extractor.optionalDouble("creatininClearance"),
                    natrium = extractor.optionalDouble("natrium"),
                    kalium = extractor.optionalDouble("kalium"),
                    calcium = extractor.optionalDouble("calcium"),
                    phosphatase = extractor.optionalDouble("phosphatase"),
                    albumin = extractor.optionalDouble("albumin"),
                    ldh = extractor.optionalDouble("ldh"),
                    magnesium = null,
                    CEA = null,
                ),
                UrinalysisValues = CairoUrinalysisValues(
                    labUrinalysisSampleDate = extractor.optionalString("labUrinalysisSampleDate"),
                    proteinDipstick = extractor.optionalInt("proteinDipstick"),
                    proteinG24H = extractor.optionalDouble("proteinG24H"),
                    proteinGL = extractor.optionalDouble("proteinGL")
                ),
                labComments = extractor.optionalString("labComments")
            )
        }
        else {
            throw IllegalArgumentException("Unknown Cairo study: $cairoStudy")
        }
    }

    private fun readTreatment(extractor: CairoFieldExtractor, cairoStudy: Int): CairoTreatment {
        if (cairoStudy == 2) {
            return CairoTreatment(
                treatmentArm = extractor.mandatoryString("C2_ARM"),
                dateStartTreatment = extractor.mandatoryString("First day of this cycle"),
                dateStartTreatmentCycleSix = null,
                dateEndTreatment = extractor.mandatoryString("Date of last drug administration"),
                dateEndTreatmentOxaliplatin = null,
                dateEndTreatmentBevacizumab = null,
                dateEndTreatmentCapecitabine = null,
                treatmentCyclesTotal = extractor.optionalInt("Total number of cycles given"),
                treatmentCyclesBevacizumab = null,
                treatmentCyclesMaintenance = null,
                treatmentChanges = null,
                commentsTreatmentReintroduction = null,
                metastaticSurgery = CairoMetastaticSurgery( // TODO: take from PSd/PS1s
                    metastatesBiopsy = null,
                    metastasesBiopsyDate = null,
                    metastatesBiopsyPathology = null,
                    metastasesBiopsyHospital = null,
                    priorResectionMetastases = null,
                    priorResectionMetastasesDate = null,
                    priorResectionMetastasesSite = null,
                    priorResectionMetastasesPathology = null,
                    priorResectionMetastasesHospital = null
                ),
                primarySurgery = CairoPrimarySurgery(// TODO: take from PSd/PS1s
                    resectionPrimaryTumor = null,
                    resectionPrimaryTumorDate = null,
                    resectionPrimaryTumorPathology = null,
                    resectionPrimaryTumorHospital = null
                ),
                adjuvantChemo = CairoAdjuvantChemo(
                    adjuvantChemo = extractor.optionalInt("priorAdjuvantChemotherapy"),
                    adjuvantChemoDrugs = extractor.optionalString("priorAdjuvantChemotherapySpecification"),
                    adjuvantChemoDateStart = extractor.optionalString("priorAdjuvantChemotherapyStartDate"),
                    adjuvantChemoDateEnd = extractor.optionalString("priorAdjuvantChemotherapyEndDate"),
                ),
                adjuvantRadio = CairoRadiotherapy(
                    radiotherapy =  extractor.optionalInt("priorRadiotherapy"),
                    radiotherapySite = extractor.optionalString("priorRadiotherapySpecification"),
                    radiotherapyDateStart = extractor.optionalString("priorRadiotherapyDateStart"),
                    radiotherapyDateEnd = extractor.optionalString("priorRadiotherapyDateEnd"),
                    radiotherapyDose = extractor.optionalDouble("priorRadiotherapyDose")
                )
            )
        }
        if (cairoStudy == 3) {
            return CairoTreatment(
                treatmentArm = extractor.mandatoryString("arm"),
                dateStartTreatment = extractor.mandatoryString("dateStartTreatment"),
                dateStartTreatmentCycleSix = extractor.optionalString("dateStartTreatmentCycle6"),
                dateEndTreatment = null, //TODO: extract from dateEndTreatmentOxaliplatin, dateEndTreatmentBevacizumab, dateEndTreatmentCapecitabine
                dateEndTreatmentOxaliplatin = extractor.optionalString("treatmentOxaDateLast"),
                dateEndTreatmentBevacizumab = extractor.optionalString("treatmentBevaDateLast"),
                dateEndTreatmentCapecitabine = extractor.optionalString("treatmentCapDateLast"),
                treatmentCyclesTotal = extractor.optionalInt("totalCyclesTreatment"),
                treatmentCyclesBevacizumab = extractor.optionalInt("bevacizumabCycles"),
                treatmentCyclesMaintenance = extractor.optionalInt("totalCyclesMaintenanceTherapy"),
                treatmentChanges = CairoTreatmentChanges(
                    doseReductionCapecitabine = extractor.optionalInt("doseReductionTreatmentCap"),
                    doseReductionCapecitabinePercentage = extractor.optionalDouble("doseReductionTreatmentCapPercentage"),
                    doseReductionCapecitabineDateLast = extractor.optionalString("doseReductionTreatmentCapDateLast"),
                    doseReductionOxaliplatin = extractor.optionalInt("doseReductionOxa"),
                    doseReductionOxaliplatinPercentage = extractor.optionalDouble("doseReductionTreatmentOxaPercentage"),
                    doseReductionOxaliplatinDateLast = extractor.optionalString("doseReductionTreatmentOxaDateLast"),
                    treatmentDelay = extractor.optionalInt("treatmentDelay"),
                    treatmentDelayWeeks = extractor.optionalInt("treatmentDelayWeeks")
                ),
                commentsTreatmentReintroduction = extractor.optionalString("commentsChemotherapyReintroduction"),
                metastaticSurgery = CairoMetastaticSurgery(
                    metastatesBiopsy = extractor.optionalInt("metastaticBiopsy"),
                    metastasesBiopsyDate = extractor.optionalString("metastaticBiopsyDate"),
                    metastatesBiopsyPathology = extractor.optionalString("metastaticBiopsyPath"),
                    metastasesBiopsyHospital = extractor.optionalString("metastaticBiopsyHospital"),
                    priorResectionMetastases = extractor.optionalInt("priorResectionMetastases"),
                    priorResectionMetastasesDate = extractor.optionalString("priorResectionMetastasesDate"),
                    priorResectionMetastasesSite = extractor.optionalString("priorResectionMetastasesSite"),
                    priorResectionMetastasesPathology = extractor.optionalString("priorResectionMetastasesPath"),
                    priorResectionMetastasesHospital = extractor.optionalString("priorResectionMetastasesHospital")
                ),
                primarySurgery = CairoPrimarySurgery(
                    resectionPrimaryTumor = extractor.optionalInt("resectionPrimaryTumor"),
                    resectionPrimaryTumorDate = extractor.optionalString("resectionPrimaryTumorDate"),
                    resectionPrimaryTumorPathology = extractor.optionalString("resectionPrimaryTumorPath"),
                    resectionPrimaryTumorHospital = extractor.optionalString("resectionPrimaryTumorHospital")
                ),
                adjuvantChemo = CairoAdjuvantChemo(
                    adjuvantChemo = extractor.optionalInt("adjuvantChemo"),
                    adjuvantChemoDrugs = extractor.optionalString("adjuvantChemoDrugs"),
                    adjuvantChemoDateStart = extractor.optionalString("adjuvantChemoDateStart"),
                    adjuvantChemoDateEnd = extractor.optionalString("adjuvantChemoEndDate"),
                ),
                adjuvantRadio = CairoRadiotherapy(
                    radiotherapy = extractor.optionalInt("radiotherapy"),
                    radiotherapySite = extractor.optionalString("radiotherapySite"),
                    radiotherapyDateStart = extractor.optionalString("radiotherapyDateStart"),
                    radiotherapyDateEnd = extractor.optionalString("radiotherapyDateLast"),
                    radiotherapyDose = extractor.optionalDouble("radiotherapyDose")
                )
            )
        }
        else {
            throw IllegalArgumentException("Unknown Cairo study: $cairoStudy")
        }
    }

    private fun readTreatmentResponse(extractor: CairoFieldExtractor, cairoStudy: Int): CairoTreatmentResponse {
        if(cairoStudy == 2) {
            return CairoTreatmentResponse(
                pfsDate = extractor.optionalString("upd_pfs_date"),
                pfsMonths = extractor.optionalDouble("upd_pfs_mo"),
                pfsEvent = extractor.optionalInt("upd_pfs_event"),
                progressionMethod = null,
                progressionCtDate = null,
                osDate = extractor.optionalString("OS_date"),
                osMonths = extractor.optionalDouble("OS_timemonths"),
                osEvent = extractor.optionalInt("OS_event"),
                deathCause = null,
                deathCauseSpecification = null,
                nextTreatment = null,
                nextTreatmentSpecification = null,
                bestOverallResponse = extractor.optionalInt("Best overall response"),
                bestOverallResponseSpecification = extractor.optionalString("Best overall response, specification"),
                responseAssessment = null
            )
        }
        if(cairoStudy == 3) {
            return CairoTreatmentResponse(
                pfsDate = extractor.optionalString("firstProgressionDate"),
                pfsMonths = null,
                pfsEvent = null,
                progressionMethod = extractor.optionalInt("firstProgressionMethod"),
                progressionCtDate = extractor.optionalString("firstProgressionCtDate"),
                osDate = extractor.optionalString("dateLastAliveStatus"),
                osMonths = null,
                osEvent = extractor.optionalInt("survivalStatus"),
                deathCause = extractor.optionalInt("deathCause"),
                deathCauseSpecification = extractor.optionalString("deathCauseSpecification"),
                nextTreatment = extractor.optionalInt("nextTreatment"),
                nextTreatmentSpecification = extractor.optionalString("nextTreatmentSpecification"),
                bestOverallResponse = extractor.optionalInt("bestOverallResponseAfterReintroductionTreatment"),
                bestOverallResponseSpecification = extractor.optionalString("bestOverallResponseAfterReintroductionTreatmentSpecification"),
                responseAssessment = CairoResponseAssessment(
                    protocolDiscontinuationReason = extractor.optionalInt("protocolDiscontinuationReason"),
                    protocolDiscontinuationReasonSpecification = extractor.optionalString("protocolDiscontinuationReasonSpecification"),
                    reasonStopObservationTreatment = extractor.optionalInt("reasonStopObservationTreatment"),
                    reasonStopObservationTreatmentSpecification = extractor.optionalString("reasonStopObservationTreatmentSpecification"),
                    responseAssessmentDateBeforeTreatment = extractor.optionalString("responseAssessmentDateBeforeTreatment"),
                    responseAssessmentDateAfterThreeCycles = extractor.optionalString("responseAssessmentDateAfterThreeCycles"),
                    responseAssessmentSiteAfterThreeCycles = extractor.optionalInt("responseAssessmentSiteAfterThreeCycles"),
                    responseAssessmentDateAfterSixCycles = extractor.optionalString("responseAssessmentDateAfterSixCycles"),
                    responseAssessmentSiteAfterSixCycles = extractor.optionalInt("responseAssessmentSiteAfterSixCycles"),
                    crDateFirstDocumented = extractor.optionalString("crDateFirstDocumented"),
                    crDateConfirmed = extractor.optionalString("crDateConfirmed"),
                    prDateFirstDocumented = extractor.optionalString("prDateFirstDocumented"),
                    prDateConfirmed = extractor.optionalString("prDateConfirmed"),
                    sdDateFirstDocumented = extractor.optionalString("sdDateFirstDocumented"),
                    progressionDateFirstDocumented = extractor.optionalString("progressionDateFirstDocumented")
                )
            )
        }
        else {
            throw IllegalArgumentException("Unknown Cairo study: $cairoStudy")
        }
    }
}
