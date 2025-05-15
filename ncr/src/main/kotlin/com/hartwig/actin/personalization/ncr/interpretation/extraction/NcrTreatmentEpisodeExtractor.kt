package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ResponseType
import com.hartwig.actin.personalization.datamodel.treatment.Drug
import com.hartwig.actin.personalization.datamodel.treatment.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.treatment.HipecTreatment
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticSurgery
import com.hartwig.actin.personalization.datamodel.treatment.PrimaryRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.PrimarySurgery
import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatmentDrug
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatmentScheme
import com.hartwig.actin.personalization.datamodel.treatment.Treatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.ncr.datamodel.NcrGastroenterologyResection
import com.hartwig.actin.personalization.ncr.datamodel.NcrHipec
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticRadiotherapy
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticSurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimaryRadiotherapy
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimarySurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.datamodel.NcrSystemicTreatment
import com.hartwig.actin.personalization.ncr.datamodel.NcrTreatmentResponse
import com.hartwig.actin.personalization.ncr.interpretation.conversion.MetastaticRtIntervalConversion
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrAnastomoticLeakageAfterSurgeryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrCircumferentialResectionMarginMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrDrugMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrGastroenterologyResectionTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrMetastaticPresenceMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrMetastaticRadiotherapyTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrMetastaticSurgeryTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrPfsMeasureFollowUpEventMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrPfsMeasureTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrRadiotherapyTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrReasonRefrainmentFromTreatmentMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrSurgeryRadicalityMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrSurgeryTechniqueMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrSurgeryTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrSurgeryUrgencyMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.resolveCyclesAndDetails
import com.hartwig.actin.personalization.ncr.interpretation.mapping.resolvePreAndPostSurgery
import kotlin.math.max
import kotlin.math.min

private val ALLOWED_SUBSTITUTIONS = setOf(Drug.FLUOROURACIL, Drug.CAPECITABINE, Drug.TEGAFUR, Drug.TEGAFUR_OR_GIMERACIL_OR_OTERACIL)

object NcrTreatmentEpisodeExtractor {

    fun extract(records: List<NcrRecord>): List<TreatmentEpisode> {
        return records.map { extractTreatmentEpisode(it) }
    }

    private fun extractTreatmentEpisode(record: NcrRecord): TreatmentEpisode {
        with(record) {
            return TreatmentEpisode(
                metastaticPresence = NcrMetastaticPresenceMapper.resolve(identification.metaEpis),
                reasonRefrainmentFromTreatment = NcrReasonRefrainmentFromTreatmentMapper.resolve(treatment.geenTherReden)
                    ?: ReasonRefrainmentFromTreatment.NOT_APPLICABLE,
                gastroenterologyResections = extractGastroenterologyResections(record.treatment.gastroenterologyResection),
                primarySurgeries = extractPrimarySurgeries(record.treatment.primarySurgery),
                metastaticSurgeries = extractMetastaticSurgeries(record.treatment.metastaticSurgery),
                hipecTreatments = extractHipecTreatments(record.treatment.hipec),
                primaryRadiotherapies = extractPrimaryRadiotherapies(record.treatment.primaryRadiotherapy),
                metastaticRadiotherapies = extractMetastasesRadiotherapies(record.treatment.metastaticRadiotherapy),
                systemicTreatments = extractSystemicTreatments(record.treatment.systemicTreatment),
                responseMeasures = extractResponseMeasures(record.treatmentResponse),
                progressionMeasures = extractProgressionMeasures(record.treatmentResponse)
            )
        }
    }

    private fun extractGastroenterologyResections(resection: NcrGastroenterologyResection): List<GastroenterologyResection> {
        return with(resection) {
            listOf(
                mdlResType1 to mdlResInt1,
                mdlResType2 to mdlResInt2
            )
                .mapNotNull { (resectionTypeCode, daysSinceDiagnosis) ->
                    resectionTypeCode?.let {
                        GastroenterologyResection(
                            daysSinceDiagnosis = daysSinceDiagnosis,
                            resectionType = NcrGastroenterologyResectionTypeMapper.resolve(it)
                        )
                    }
                }
        }
    }

    private fun extractPrimarySurgeries(primarySurgery: NcrPrimarySurgery): List<PrimarySurgery> {
        return with(primarySurgery) {
            listOfNotNull(
                chirType1?.let {
                    extractPrimarySurgery(
                        daysSinceDiagnosis = chirInt1,
                        surgeryTypeCode = it,
                        techniqueCode = chirTech1,
                        urgencyCode = chirUrg1,
                        radicalityCode = chirRad1,
                        circumferentialResectionMarginCode = chirCrm1,
                        leakageCode = chirNaadlek1,
                        hospitalizationDurationDays = chirOpnameduur1
                    )
                },
                chirType2?.let {
                    extractPrimarySurgery(
                        daysSinceDiagnosis = chirInt2,
                        surgeryTypeCode = it,
                        techniqueCode = chirTech2,
                        urgencyCode = chirUrg2,
                        radicalityCode = chirRad2,
                        circumferentialResectionMarginCode = chirCrm2,
                        leakageCode = chirNaadlek2,
                        hospitalizationDurationDays = chirOpnameduur2
                    )
                }
            )
        }
    }

    private fun extractPrimarySurgery(
        daysSinceDiagnosis: Int?,
        surgeryTypeCode: Int,
        techniqueCode: Int?,
        urgencyCode: Int?,
        radicalityCode: Int?,
        circumferentialResectionMarginCode: Int?,
        leakageCode: Int?,
        hospitalizationDurationDays: Int?
    ): PrimarySurgery {
        return PrimarySurgery(
            daysSinceDiagnosis = daysSinceDiagnosis,
            type = NcrSurgeryTypeMapper.resolve(surgeryTypeCode),
            technique = NcrSurgeryTechniqueMapper.resolve(techniqueCode),
            urgency = NcrSurgeryUrgencyMapper.resolve(urgencyCode),
            radicality = NcrSurgeryRadicalityMapper.resolve(radicalityCode),
            circumferentialResectionMargin = NcrCircumferentialResectionMarginMapper.resolve(circumferentialResectionMarginCode),
            anastomoticLeakageAfterSurgery = NcrAnastomoticLeakageAfterSurgeryMapper.resolve(leakageCode),
            hospitalizationDurationDays = hospitalizationDurationDays
        )
    }

    private fun extractMetastaticSurgeries(metastaticSurgery: NcrMetastaticSurgery): List<MetastaticSurgery> {
        return with(metastaticSurgery) {
            listOf(
                Triple(metaChirCode1, metaChirRad1, metaChirInt1),
                Triple(metaChirCode2, metaChirRad2, metaChirInt2),
                Triple(metaChirCode3, metaChirRad3, metaChirInt3)
            )
                .mapNotNull { (type, radicality, daysSinceDiagnosis) ->
                    type?.let {
                        MetastaticSurgery(
                            daysSinceDiagnosis = daysSinceDiagnosis,
                            type = NcrMetastaticSurgeryTypeMapper.resolve(it),
                            radicality = NcrSurgeryRadicalityMapper.resolve(radicality),
                        )
                    }
                }
        }
    }

    private fun extractHipecTreatments(hipec: NcrHipec): List<HipecTreatment> {
        val hasHadHipecTreatment = NcrBooleanMapper.resolve(hipec.hipec) == true
        if (!hasHadHipecTreatment) {
            return emptyList()
        }

        return listOfNotNull(hipec.hipecInt1).map { HipecTreatment(daysSinceDiagnosis = it) }
    }

    private fun extractPrimaryRadiotherapies(ncrPrimaryRadiotherapy: NcrPrimaryRadiotherapy): List<PrimaryRadiotherapy> {
        return with(ncrPrimaryRadiotherapy) {
            listOfNotNull(
                extractPrimaryRadiotherapy(rtStartInt1, rtStopInt1, rtType1, rtDosis1),
                extractPrimaryRadiotherapy(rtStartInt2, rtStopInt2, rtType2, rtDosis2)
            )
        }
    }

    private fun extractPrimaryRadiotherapy(startInt: Int?, stopInt: Int?, rtType: Int?, dosage: Double?): PrimaryRadiotherapy? {
        return rtType?.let(NcrRadiotherapyTypeMapper::resolve)?.let { type ->
            PrimaryRadiotherapy(
                daysBetweenDiagnosisAndStart = startInt,
                daysBetweenDiagnosisAndStop = stopInt,
                type = type,
                totalDosage = dosage
            )
        }
    }

    private fun extractMetastasesRadiotherapies(ncrMetastaticRadiotherapy: NcrMetastaticRadiotherapy): List<MetastaticRadiotherapy> {
        return with(ncrMetastaticRadiotherapy) {
            listOf(
                Triple(metaRtCode1, metaRtStartInt1, metaRtStopInt1),
                Triple(metaRtCode2, metaRtStartInt2, metaRtStopInt2),
                Triple(metaRtCode3, metaRtStartInt3, metaRtStopInt3),
                Triple(metaRtCode4, metaRtStartInt4, metaRtStopInt4)
            )
                .mapNotNull { (mrtType, startInterval, stopInterval) ->
                    mrtType?.let { typeCode ->
                        MetastaticRadiotherapy(
                            daysBetweenDiagnosisAndStart = MetastaticRtIntervalConversion.convert(startInterval),
                            daysBetweenDiagnosisAndStop = MetastaticRtIntervalConversion.convert(stopInterval),
                            type = NcrMetastaticRadiotherapyTypeMapper.resolve(typeCode)
                        )
                    }
                }
        }
    }

    private fun extractSystemicTreatments(systemicTreatment: NcrSystemicTreatment): List<SystemicTreatment> {
        val schemes = with(systemicTreatment) {
            listOfNotNull(
                extractDrugWithSchemeNr(systCode1, systSchemanum1, systStartInt1, systStopInt1, systKuren1, systPrepost1),
                extractDrugWithSchemeNr(systCode2, systSchemanum2, systStartInt2, systStopInt2, systKuren2, systPrepost2),
                extractDrugWithSchemeNr(systCode3, systSchemanum3, systStartInt3, systStopInt3, systKuren3, systPrepost3),
                extractDrugWithSchemeNr(systCode4, systSchemanum4, systStartInt4, systStopInt4, systKuren4, systPrepost4),
                extractDrugWithSchemeNr(systCode5, systSchemanum5, systStartInt5, systStopInt5, systKuren5, systPrepost5),
                extractDrugWithSchemeNr(systCode6, systSchemanum6, systStartInt6, systStopInt6, systKuren6, systPrepost6),
                extractDrugWithSchemeNr(systCode7, systSchemanum7, systStartInt7, systStopInt7, systKuren7, systPrepost7),
                extractDrugWithSchemeNr(systCode8, systSchemanum8, systStartInt8, systStopInt8, systKuren8, systPrepost8),
                extractDrugWithSchemeNr(systCode9, systSchemanum9, systStartInt9, systStopInt9, systKuren9, systPrepost9),
                extractDrugWithSchemeNr(systCode10, systSchemanum10, systStartInt10, systStopInt10, systKuren10, systPrepost10),
                extractDrugWithSchemeNr(systCode11, systSchemanum11, systStartInt11, systStopInt11, systKuren11, systPrepost11),
                extractDrugWithSchemeNr(systCode12, systSchemanum12, systStartInt12, systStopInt12, systKuren12, systPrepost12),
                extractDrugWithSchemeNr(systCode13, systSchemanum13, systStartInt13, systStopInt13, systKuren13, systPrepost13),
                extractDrugWithSchemeNr(systCode14, systSchemanum14, systStartInt14, systStopInt14, systKuren14, systPrepost14),
            ).groupBy({ it.second }, { it.first })
                .toSortedMap()
                .map { (_, components) ->
                    val (startMin, startMax, stopMin, stopMax) = components.map {
                        with(it) {
                            StartAndStopMinAndMax(
                                daysBetweenDiagnosisAndStart,
                                daysBetweenDiagnosisAndStart,
                                daysBetweenDiagnosisAndStop,
                                daysBetweenDiagnosisAndStop
                            )
                        }
                    }.reduce(StartAndStopMinAndMax::plus)

                    SystemicTreatmentScheme(
                        minDaysBetweenDiagnosisAndStart = startMin,
                        maxDaysBetweenDiagnosisAndStart = startMax,
                        minDaysBetweenDiagnosisAndStop = stopMin,
                        maxDaysBetweenDiagnosisAndStop = stopMax,
                        components = components
                    )
                }
        }

        if (schemes.isEmpty()) {
            return emptyList()
        }

        return listOf(
            SystemicTreatment(
                daysBetweenDiagnosisAndStart = schemes.first().minDaysBetweenDiagnosisAndStart,
                daysBetweenDiagnosisAndStop = schemes.last().maxDaysBetweenDiagnosisAndStop,
                treatment = determineTreatmentFromSchemes(schemes),
                schemes = schemes
            )
        )
    }

    private fun extractDrugWithSchemeNr(
        drugCode: String?,
        schemaNum: Int?,
        daysBetweenDiagnosisAndStart: Int?,
        daysBetweenDiagnosisAndStop: Int?,
        cycleCode: Int?,
        prePostCode: Int?
    ): Pair<SystemicTreatmentDrug, Int>? {
        if (drugCode == null || schemaNum == null) {
            return null
        }

        val (preSurgery, postSurgery) = resolvePreAndPostSurgery(prePostCode)
        val (cycles, intent, isOngoing) = resolveCyclesAndDetails(cycleCode)
        return Pair(
            first = SystemicTreatmentDrug(
                daysBetweenDiagnosisAndStart = daysBetweenDiagnosisAndStart,
                daysBetweenDiagnosisAndStop = daysBetweenDiagnosisAndStop,
                drug = NcrDrugMapper.resolve(drugCode),
                numberOfCycles = cycles,
                intent = intent,
                drugTreatmentIsOngoing = isOngoing,
                isAdministeredPreSurgery = preSurgery,
                isAdministeredPostSurgery = postSurgery
            ),
            second = schemaNum
        )
    }

    private data class StartAndStopMinAndMax(
        val startMin: Int?, val startMax: Int?, val stopMin: Int?, val stopMax: Int?
    ) {

        operator fun plus(other: StartAndStopMinAndMax): StartAndStopMinAndMax {
            return StartAndStopMinAndMax(
                startMin = minOfNullables(startMin, other.startMin),
                startMax = maxOfNullables(startMax, other.startMax),
                stopMin = minOfNullables(stopMin, other.stopMin),
                stopMax = maxOfNullables(stopMax, other.stopMax)
            )
        }

        private fun maxOfNullables(a: Int?, b: Int?): Int? {
            return when {
                a == null -> b
                b == null -> a
                else -> max(a, b)
            }
        }

        private fun minOfNullables(a: Int?, b: Int?): Int? {
            return when {
                a == null -> b
                b == null -> a
                else -> min(a, b)
            }
        }
    }

    private fun determineTreatmentFromSchemes(treatmentSchemes: Iterable<SystemicTreatmentScheme>): Treatment {
        val firstSchemeDrugs = drugsFromScheme(treatmentSchemes.first()).toSet()
        val followupDrugs = treatmentSchemes.drop(1).flatMap(::drugsFromScheme).toSet()
        val newDrugsToIgnore = if (firstSchemeDrugs.intersect(ALLOWED_SUBSTITUTIONS).isNotEmpty()) ALLOWED_SUBSTITUTIONS else emptySet()

        return if ((followupDrugs - firstSchemeDrugs - newDrugsToIgnore).isEmpty()) {
            Treatment.findForDrugs(firstSchemeDrugs)
        } else Treatment.OTHER
    }

    private fun drugsFromScheme(systemicTreatmentScheme: SystemicTreatmentScheme): List<Drug> {
        return systemicTreatmentScheme.components.map(SystemicTreatmentDrug::drug)
    }

    private fun extractResponseMeasures(treatmentResponse: NcrTreatmentResponse): List<ResponseMeasure> {
        val response = treatmentResponse.responsUitslag

        if (response == null || response == "99" || response == "0") {
            return emptyList()
        }

        return listOf(ResponseMeasure(treatmentResponse.responsInt, ResponseType.valueOf(response)))
    }

    private fun extractProgressionMeasures(treatmentResponse: NcrTreatmentResponse): List<ProgressionMeasure> {
        return with(treatmentResponse) {
            listOf(
                Triple(pfsEvent1, fupEventType1, pfsInt1),
                Triple(pfsEvent2, fupEventType2, pfsInt2),
                Triple(pfsEvent3, fupEventType3, pfsInt3),
                Triple(pfsEvent4, fupEventType4, pfsInt4)
            )
                .mapNotNull { (type, followupEvent, daysSinceDiagnosis) ->
                    type?.let {
                        ProgressionMeasure(
                            daysSinceDiagnosis = daysSinceDiagnosis,
                            type = NcrPfsMeasureTypeMapper.resolve(it),
                            followUpEvent = NcrPfsMeasureFollowUpEventMapper.resolve(followupEvent)
                        )
                    }
                }
        }
    }
}