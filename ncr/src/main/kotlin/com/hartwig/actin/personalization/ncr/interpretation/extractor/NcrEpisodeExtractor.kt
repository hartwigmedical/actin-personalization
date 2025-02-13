package com.hartwig.actin.personalization.ncr.interpretation.extractor

import com.hartwig.actin.personalization.datamodel.assessment.LabMeasure
import com.hartwig.actin.personalization.datamodel.old.Episode
import com.hartwig.actin.personalization.datamodel.old.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.old.LabMeasurement
import com.hartwig.actin.personalization.datamodel.old.MetastasesRadiotherapy
import com.hartwig.actin.personalization.datamodel.old.MetastasesSurgery
import com.hartwig.actin.personalization.datamodel.old.Metastasis
import com.hartwig.actin.personalization.datamodel.old.PfsMeasure
import com.hartwig.actin.personalization.datamodel.old.Radiotherapy
import com.hartwig.actin.personalization.datamodel.old.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.old.Surgery
import com.hartwig.actin.personalization.datamodel.outcome.ResponseType
import com.hartwig.actin.personalization.ncr.datamodel.NcrGastroenterologyResection
import com.hartwig.actin.personalization.ncr.datamodel.NcrLabValues
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticDiagnosis
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticRadiotherapy
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticSurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimaryRadiotherapy
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimarySurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.datamodel.NcrTreatmentResponse
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAnastomoticLeakageAfterSurgeryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrAsaClassificationPreSurgeryOrEndoscopyMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrDistantMetastasesStatusMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrExtraMuralInvasionCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrGastroenterologyResectionTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorLocationMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrLymphaticInvasionCategoryMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrMetastasesRadiotherapyTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrMetastasesSurgeryTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrNumberOfLiverMetastasesMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrPfsMeasureFollowUpEventMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrPfsMeasureTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrRadiotherapyTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrReasonRefrainmentFromTumorDirectedTherapyMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryCircumferentialResectionMarginMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryRadicalityMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryTechniqueMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryTypeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrSurgeryUrgencyMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTnmMMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTnmNMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTnmTMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorBasisOfDiagnosisMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorDifferentiationGradeMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrTumorRegressionMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrVenousInvasionDescriptionMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrWhoStatusMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolvePreAndPostSurgery
import kotlin.math.roundToInt

class NcrEpisodeExtractor(private val systemicTreatmentPlanExtractor: NcrSystemicTreatmentPlanExtractor) {

    fun extractEpisode(record: NcrRecord, intervalTumorIncidenceLatestAliveStatus: Int): Episode {
        return with(record) {
            val responseMeasure = treatmentResponse.responsUitslag?.let {
                if (it == "99" || it == "0") null else enumValueOf<ResponseType>(it)
            }
                ?.let { ResponseMeasure(it, treatmentResponse.responsInt ?: 0) }

            val pfsMeasures = extractPfsMeasures(treatmentResponse)
            val systemicTreatmentPlan = systemicTreatmentPlanExtractor.extractSystemicTreatmentPlan(
                treatment.systemicTreatment, pfsMeasures, responseMeasure, intervalTumorIncidenceLatestAliveStatus
            )
            val ageAtTreatmentPlanStart = patientCharacteristics.leeft.let { age ->
                systemicTreatmentPlan?.intervalTumorIncidenceTreatmentPlanStartDays?.let { interval ->
                    (age + 0.5 + (interval / 365.0)).roundToInt()
                }
            }

            val (distanceToMesorectalFascia, mesorectalFasciaIsClear) = extractDistanceToMesorectalFascia(clinicalCharacteristics.mrfAfst)
            val (hasHadPreSurgeryRadiotherapy, hasHadPostSurgeryRadiotherapy) = resolvePreAndPostSurgery(treatment.primaryRadiotherapy.rt)
            val (hasHadPreSurgeryChemoRadiotherapy, hasHadPostSurgeryChemoRadiotherapy) =
                resolvePreAndPostSurgery(treatment.primaryRadiotherapy.chemort)
            val (hasHadPreSurgerySystemicChemotherapy, hasHadPostSurgerySystemicChemotherapy) =
                resolvePreAndPostSurgery(treatment.systemicTreatment.chemo)
            val (hasHadPreSurgerySystemicTargetedTherapy, hasHadPostSurgerySystemicTargetedTherapy) =
                resolvePreAndPostSurgery(treatment.systemicTreatment.target)

            Episode(
                id = identification.keyEid,
                order = identification.teller,
                whoStatusPreTreatmentStart = NcrWhoStatusMapper.resolve(patientCharacteristics.perfStat),
                asaClassificationPreSurgeryOrEndoscopy =
                NcrAsaClassificationPreSurgeryOrEndoscopyMapper.resolve(patientCharacteristics.asa),
                tumorIncidenceYear = primaryDiagnosis.incjr,
                tumorBasisOfDiagnosis = NcrTumorBasisOfDiagnosisMapper.resolve(primaryDiagnosis.diagBasis),
                tumorLocation = NcrTumorLocationMapper.resolveTumorLocation(primaryDiagnosis.topoSublok),
                tumorDifferentiationGrade = NcrTumorDifferentiationGradeMapper.resolve(primaryDiagnosis.diffgrad.toInt()),
                tnmCT = NcrTnmTMapper.resolveNullable(primaryDiagnosis.ct),
                tnmCN = NcrTnmNMapper.resolveNullable(primaryDiagnosis.cn),
                tnmCM = NcrTnmMMapper.resolveNullable(primaryDiagnosis.cm),
                tnmPT = NcrTnmTMapper.resolveNullable(primaryDiagnosis.pt),
                tnmPN = NcrTnmNMapper.resolveNullable(primaryDiagnosis.pn),
                tnmPM = NcrTnmMMapper.resolveNullable(primaryDiagnosis.pm),
//                stageCTNM = NcrTumorStageMapper.resolveNullable(primaryDiagnosis.cstadium),
//                stagePTNM = NcrTumorStageMapper.resolveNullable(primaryDiagnosis.pstadium),
//                stageTNM = NcrTumorStageMapper.resolveNullable(primaryDiagnosis.stadium),
                investigatedLymphNodesNumber = primaryDiagnosis.ondLymf,
                positiveLymphNodesNumber = primaryDiagnosis.posLymf,
                distantMetastasesDetectionStatus = NcrDistantMetastasesStatusMapper.resolve(identification.metaEpis),
                metastases = extractMetastases(metastaticDiagnosis),
                numberOfLiverMetastases = NcrNumberOfLiverMetastasesMapper.resolve(metastaticDiagnosis.metaLeverAantal),
                maximumSizeOfLiverMetastasisMm = metastaticDiagnosis.metaLeverAfm.takeIf { it != 999 },
                hasDoublePrimaryTumor = NcrBooleanMapper.resolve(clinicalCharacteristics.dubbeltum),
                mesorectalFasciaIsClear = mesorectalFasciaIsClear,
                distanceToMesorectalFasciaMm = distanceToMesorectalFascia,
                venousInvasionDescription =  NcrVenousInvasionDescriptionMapper.resolve(clinicalCharacteristics.veneusInvas),
                lymphaticInvasionCategory = NcrLymphaticInvasionCategoryMapper.resolve(clinicalCharacteristics.lymfInvas),
                extraMuralInvasionCategory = NcrExtraMuralInvasionCategoryMapper.resolve(clinicalCharacteristics.emi),
                tumorRegression = NcrTumorRegressionMapper.resolve(clinicalCharacteristics.tumregres),
                labMeasurements = extractLabMeasurements(labValues),
                hasReceivedTumorDirectedTreatment = NcrBooleanMapper.resolve(treatment.tumgerichtTher) == true,
                reasonRefrainmentFromTumorDirectedTreatment =
                NcrReasonRefrainmentFromTumorDirectedTherapyMapper.resolve(treatment.geenTherReden),
                hasParticipatedInTrial = NcrBooleanMapper.resolve(treatment.deelnameStudie),
                gastroenterologyResections = extractGastroenterologyResections(treatment.gastroenterologyResection),
                surgeries = extractSurgeries(treatment.primarySurgery),
                metastasesSurgeries = extractMetastasesSurgeries(treatment.metastaticSurgery),
                radiotherapies = extractRadiotherapies(treatment.primaryRadiotherapy),
                metastasesRadiotherapies = extractMetastasesRadiotherapies(treatment.metastaticRadiotherapy),
                hasHadHipecTreatment = NcrBooleanMapper.resolve(treatment.hipec.hipec) == true,
                intervalTumorIncidenceHipecTreatmentDays = treatment.hipec.hipecInt1,
                hasHadPreSurgeryRadiotherapy = hasHadPreSurgeryRadiotherapy,
                hasHadPostSurgeryRadiotherapy = hasHadPostSurgeryRadiotherapy,
                hasHadPreSurgeryChemoRadiotherapy = hasHadPreSurgeryChemoRadiotherapy,
                hasHadPostSurgeryChemoRadiotherapy = hasHadPostSurgeryChemoRadiotherapy,
                hasHadPreSurgerySystemicChemotherapy = hasHadPreSurgerySystemicChemotherapy,
                hasHadPostSurgerySystemicChemotherapy = hasHadPostSurgerySystemicChemotherapy,
                hasHadPreSurgerySystemicTargetedTherapy = hasHadPreSurgerySystemicTargetedTherapy,
                hasHadPostSurgerySystemicTargetedTherapy = hasHadPostSurgerySystemicTargetedTherapy,
                responseMeasure = responseMeasure,
                systemicTreatmentPlan = systemicTreatmentPlan,
                pfsMeasures = pfsMeasures,
                ageAtTreatmentPlanStart = ageAtTreatmentPlanStart
            )
        }
    }

    private fun extractPfsMeasures(response: NcrTreatmentResponse): List<PfsMeasure> {
        return with(response) {
            listOf(
                Triple(pfsEvent1, fupEventType1, pfsInt1),
                Triple(pfsEvent2, fupEventType2, pfsInt2),
                Triple(pfsEvent3, fupEventType3, pfsInt3),
                Triple(pfsEvent4, fupEventType4, pfsInt4)
            )
                .mapNotNull { (event, type, interval) ->
                    event?.let { PfsMeasure(NcrPfsMeasureTypeMapper.resolve(it), NcrPfsMeasureFollowUpEventMapper.resolve(type), interval) }
                }
        }
    }

    private fun extractMetastasesRadiotherapies(ncrMetastaticRadiotherapy: NcrMetastaticRadiotherapy): List<MetastasesRadiotherapy> {
        return with(ncrMetastaticRadiotherapy) {
            listOf(
                Triple(metaRtCode1, metaRtStartInt1, metaRtStopInt1),
                Triple(metaRtCode2, metaRtStartInt2, metaRtStopInt2),
                Triple(metaRtCode3, metaRtStartInt3, metaRtStopInt3),
                Triple(metaRtCode4, metaRtStartInt4, metaRtStopInt4)
            )
                .mapNotNull { (mrtType, startInterval, stopInterval) ->
                    mrtType?.let { typeCode ->
                        MetastasesRadiotherapy(
                            NcrMetastasesRadiotherapyTypeMapper.resolve(typeCode),
                            startInterval?.takeIf { it != "." }?.toInt(),
                            stopInterval?.takeIf { it != "." }?.toInt()
                        )
                    }
                }
        }
    }

    private fun extractRadiotherapies(ncrPrimaryRadiotherapy: NcrPrimaryRadiotherapy): List<Radiotherapy> {
        return with(ncrPrimaryRadiotherapy) {
            listOfNotNull(
                extractRadiotherapy(rtType1, rtDosis1, rtStartInt1, rtStopInt1),
                extractRadiotherapy(rtType2, rtDosis2, rtStartInt2, rtStopInt2)
            )
        }
    }

    private fun extractRadiotherapy(rtType: Int?, dosage: Double?, startInt: Int?, stopInt: Int?) =
        rtType?.let(NcrRadiotherapyTypeMapper::resolve)?.let { type -> Radiotherapy(type, dosage, startInt, stopInt) }

    private fun extractMetastasesSurgeries(metastaticSurgery: NcrMetastaticSurgery): List<MetastasesSurgery> {
        return with(metastaticSurgery) {
            listOf(
                Triple(metaChirCode1, metaChirRad1, metaChirInt1),
                Triple(metaChirCode2, metaChirRad2, metaChirInt2),
                Triple(metaChirCode3, metaChirRad3, metaChirInt3)
            )
                .mapNotNull { (type, radicality, interval) ->
                    type?.let {
                        MetastasesSurgery(
                            NcrMetastasesSurgeryTypeMapper.resolve(it), NcrSurgeryRadicalityMapper.resolve(radicality), interval
                        )
                    }
                }
        }
    }

    private fun extractSurgeries(primarySurgery: NcrPrimarySurgery): List<Surgery> {
        return with(primarySurgery) {
            listOfNotNull(
                chirType1?.let { extractSurgery(it, chirTech1, chirUrg1, chirRad1, chirCrm1, chirNaadlek1, chirInt1, chirOpnameduur1) },
                chirType2?.let { extractSurgery(it, chirTech2, chirUrg2, chirRad2, chirCrm2, chirNaadlek2, chirInt2, chirOpnameduur2) }
            )
        }
    }

    private fun extractSurgery(
        surgeryType: Int, technique: Int?, urgency: Int?, radicality: Int?, margins: Int?, leakage: Int?, interval: Int?, duration: Int?
    ): Surgery {
        return Surgery(
            NcrSurgeryTypeMapper.resolve(surgeryType),
            NcrSurgeryTechniqueMapper.resolve(technique),
            NcrSurgeryUrgencyMapper.resolve(urgency),
            NcrSurgeryRadicalityMapper.resolve(radicality),
            NcrSurgeryCircumferentialResectionMarginMapper.resolve(margins),
            NcrAnastomoticLeakageAfterSurgeryMapper.resolve(leakage),
            interval,
            duration
        )
    }

    private fun extractGastroenterologyResections(resection: NcrGastroenterologyResection): List<GastroenterologyResection> {
        return with(resection) {
            listOf(
                mdlResType1 to mdlResInt1,
                mdlResType2 to mdlResInt2
            )
                .mapNotNull { (type, interval) ->
                    type?.let { GastroenterologyResection(NcrGastroenterologyResectionTypeMapper.resolve(it), interval) }
                }
        }
    }

    private fun extractLabMeasurements(labValues: NcrLabValues): List<LabMeasurement> {
        with(labValues) {
            val measurements = listOf(
                LabMeasure.LACTATE_DEHYDROGENASE to listOf(
                    ldh1 to ldhInt1,
                    ldh2 to ldhInt2,
                    ldh3 to ldhInt3,
                    ldh4 to ldhInt4
                ),
                LabMeasure.ALKALINE_PHOSPHATASE to listOf(
                    af1 to afInt1,
                    af2 to afInt2,
                    af3 to afInt3,
                    af4 to afInt4
                ),
                LabMeasure.NEUTROPHILS_ABSOLUTE to listOf(
                    neutro1 to neutroInt1,
                    neutro2 to neutroInt2,
                    neutro3 to neutroInt3,
                    neutro4 to neutroInt4
                ),
                LabMeasure.ALBUMINE to listOf(
                    albumine1 to albumineInt1,
                    albumine2 to albumineInt2,
                    albumine3 to albumineInt3,
                    albumine4 to albumineInt4
                ),
                LabMeasure.LEUKOCYTES_ABSOLUTE to listOf(
                    leuko1 to leukoInt1,
                    leuko2 to leukoInt2,
                    leuko3 to leukoInt3,
                    leuko4 to leukoInt4
                )
            ).flatMap { (measure, values) ->
                values.mapNotNull { (value, interval) ->
                    value?.toDouble()?.takeIf { it != 9999.0 && it <= measure.upperBound }?.let {
                        LabMeasurement(measure, it, measure.unit, interval, null, null)
                    }
                }
            }
            return measurements + listOfNotNull(
                periSurgicalCeaMeasurement(prechirCea, true),
                periSurgicalCeaMeasurement(postchirCea, false)
            )
        }
    }
    private fun periSurgicalCeaMeasurement(measurement: Double?, isPreSurgical: Boolean) = measurement?.takeIf { it != 9999.0 && it <= LabMeasure.CARCINOEMBRYONIC_ANTIGEN.upperBound }?.let {
        LabMeasurement(
            LabMeasure.CARCINOEMBRYONIC_ANTIGEN, it, LabMeasure.CARCINOEMBRYONIC_ANTIGEN.unit, null, isPreSurgical, !isPreSurgical
        )
    }

    private fun extractDistanceToMesorectalFascia(mrfAfst: Int?): Pair<Int?, Boolean?> {
        return when (mrfAfst) {
            null, 888, 999 -> null to null
            111 -> null to true
            222 -> null to false
            in 0..20 -> mrfAfst to null
            else -> throw IllegalStateException("Unexpected value for distance to mesorectal fascia: $mrfAfst")
        }
    }

    private fun extractMetastases(metastaticDiagnosis: NcrMetastaticDiagnosis): List<Metastasis> {
        return with(metastaticDiagnosis) {
            val locations = listOfNotNull(
                metaTopoSublok1,
                metaTopoSublok2,
                metaTopoSublok3,
                metaTopoSublok4,
                metaTopoSublok5,
                metaTopoSublok6,
                metaTopoSublok7,
                metaTopoSublok8,
                metaTopoSublok9,
                metaTopoSublok10
            )
            val intervalDays = listOf(
                metaInt1,
                metaInt2,
                metaInt3,
                metaInt4,
                metaInt5,
                metaInt6,
                metaInt7,
                metaInt8,
                metaInt9,
                metaInt10
            )
            val progression = listOf(
                metaProg1,
                metaProg2,
                metaProg3,
                metaProg4,
                metaProg5,
                metaProg6,
                metaProg7,
                metaProg8,
                metaProg9,
                metaProg10
            )
            locations.mapIndexed { i, location ->
                Metastasis(
                    NcrTumorLocationMapper.resolveTumorLocation(location),
                    intervalDays[i],
                    NcrBooleanMapper.resolve(progression[i])
                )
            }
        }
    }
}