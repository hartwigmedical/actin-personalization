package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.treatment.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.treatment.HipecTreatment
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticSurgery
import com.hartwig.actin.personalization.datamodel.treatment.PrimaryRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.PrimarySurgery
import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTreatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.ncr.datamodel.NcrGastroenterologyResection
import com.hartwig.actin.personalization.ncr.datamodel.NcrHipec
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticRadiotherapy
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticSurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimaryRadiotherapy
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimarySurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.conversion.MetastaticRtIntervalConversion
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrAnastomoticLeakageAfterSurgeryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrCircumferentialResectionMarginMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrGastroenterologyResectionTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrMetastasesSurgeryTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrMetastaticPresenceMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrMetastaticRadiotherapyTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrRadiotherapyTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrReasonRefrainmentFromTreatmentMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrSurgeryRadicalityMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrSurgeryTechniqueMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrSurgeryTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrSurgeryUrgencyMapper

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
                systemicTreatments = emptyList(),
                responseMeasures = emptyList(),
                progressionMeasures = emptyList()
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
                            type = NcrMetastasesSurgeryTypeMapper.resolve(it),
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
}