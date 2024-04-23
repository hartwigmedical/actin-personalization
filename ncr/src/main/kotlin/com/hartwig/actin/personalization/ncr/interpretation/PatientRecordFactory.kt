package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.LabMeasure
import com.hartwig.actin.personalization.datamodel.LabMeasurement
import com.hartwig.actin.personalization.datamodel.MetastasesRadiotherapy
import com.hartwig.actin.personalization.datamodel.MetastasesSurgery
import com.hartwig.actin.personalization.datamodel.Metastasis
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.PfsMeasure
import com.hartwig.actin.personalization.datamodel.PriorTumor
import com.hartwig.actin.personalization.datamodel.Radiotherapy
import com.hartwig.actin.personalization.datamodel.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.ResponseMeasureType
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.StageTNM
import com.hartwig.actin.personalization.datamodel.Surgery
import com.hartwig.actin.personalization.datamodel.SystemicTreatmentScheme
import com.hartwig.actin.personalization.datamodel.TumorEpisodes
import com.hartwig.actin.personalization.datamodel.TumorOfInterest
import com.hartwig.actin.personalization.ncr.datamodel.NcrGastroenterologyResection
import com.hartwig.actin.personalization.ncr.datamodel.NcrLabValues
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticDiagnosis
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticRadiotherapy
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticSurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimaryRadiotherapy
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimarySurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.datamodel.NcrTreatmentResponse
import com.hartwig.actin.personalization.ncr.interpretation.extractor.extractSystemicTreatmentSchemes
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrLocationMapper.resolveLocation
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolveMetastasesRadiotherapyType
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolveMetastasesSurgeryType
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolvePreAndPostSurgery
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolveTreatmentName
import java.util.stream.Collectors

private const val DIAGNOSIS_EPISODE = "DIA"

object PatientRecordFactory {

    fun create(ncrRecords: List<NcrRecord>): List<PatientRecord> {
        val recordsPerPatient: Map<Int, List<NcrRecord>> = ncrRecords.groupBy { it.identification.keyNkr }

        return recordsPerPatient.entries.parallelStream()
            .map { createPatientRecord(it.value) }
            .collect(Collectors.toList())
    }

    private fun createPatientRecord(ncrRecords: List<NcrRecord>): PatientRecord {
        return PatientRecord(
            ncrId = extractNcrId(ncrRecords),
            sex = extractSex(ncrRecords),
            isAlive = determineIsAlive(ncrRecords),
            episodesPerTumorOfInterest = determineEpisodesPerTumorOfInterest(ncrRecords)
        )
    }

    private fun extractNcrId(ncrRecords: List<NcrRecord>): Int {
        val ncrIds: List<Int> = ncrRecords.map { it.identification.keyNkr }.distinct()
        if (ncrIds.count() != 1) {
            throw IllegalStateException("Non-unique or missing NCR ID when creating a single patient record: $ncrIds")
        }
        return ncrIds[0]
    }

    private fun extractSex(ncrRecords: List<NcrRecord>): Sex {
        val sexes: List<Int> = ncrRecords.map { it.patientCharacteristics.gesl }.distinct()
        if (sexes.count() != 1) {
            throw IllegalStateException("Multiple sexes found for patient with NCR ID '" + extractNcrId(ncrRecords) + "'")
        }

        return resolve(sexes.single())
    }

    private fun determineIsAlive(ncrRecords: List<NcrRecord>): Boolean? {
        // Vital status is only collect on diagnosis episodes.
        val vitalStatuses: List<Int?> = diagnosisEpisodes(ncrRecords).map { it.patientCharacteristics.vitStat }.distinct()
        if (vitalStatuses.count() != 1) {
            throw IllegalStateException("Non-unique or missing vital statuses when creating a single patient record: $vitalStatuses")
        }
        return when (val vitalStatus = vitalStatuses[0]) {
            null -> return null
            0 -> true
            1 -> false
            else -> throw IllegalStateException("Cannot convert vital status: $vitalStatus")
        }
    }

    private fun determineEpisodesPerTumorOfInterest(ncrRecords: List<NcrRecord>): Map<TumorOfInterest, TumorEpisodes> {
        val recordsPerTumor = ncrRecords.groupBy { it.identification.keyZid }
        return recordsPerTumor.values.associate(::createEpisodesForOneTumorOfInterest)
    }

    private fun createEpisodesForOneTumorOfInterest(ncrRecords: List<NcrRecord>): Pair<TumorOfInterest, TumorEpisodes> {
        val tumorEpisodes = createTumorEpisodes(ncrRecords)
        val episodes = tumorEpisodes.followupEpisodes + tumorEpisodes.diagnosisEpisode

        val diagnosisRecord = diagnosisEpisodes(ncrRecords).minBy { it.identification.keyEid }
        val priorTumors = extractPriorTumors(diagnosisRecord)
        val tumorOfInterest = TumorOfInterest(
            consolidatedTumorType = resolve(ncrRecords.mapNotNull { it.primaryDiagnosis.morfCat }.distinct().single()),
            consolidatedTumorLocation = episodes.map(Episode::tumorLocation).distinct().single(),
            hasHadTumorDirectedSystemicTherapy = episodes.any(Episode::hasReceivedTumorDirectedTreatment),
            hasHadPriorTumor = priorTumors.isNotEmpty(),
            priorTumors = priorTumors
        )
        return Pair(tumorOfInterest, tumorEpisodes)
    }

    private fun extractPriorTumors(record: NcrRecord): List<PriorTumor> {
        return with(record.priorMalignancies) {
            listOfNotNull(
                mal1Morf?.let {
                    extractPriorTumor(
                        it,
                        mal1TopoSublok,
                        mal1Syst,
                        mal1Int,
                        1,
                        mal1Tumsoort,
                        mal1Stadium,
                        listOfNotNull(
                            mal1SystCode1,
                            mal1SystCode2,
                            mal1SystCode3,
                            mal1SystCode4,
                            mal1SystCode5,
                            mal1SystCode6,
                            mal1SystCode7,
                            mal1SystCode8,
                            mal1SystCode9
                        )
                    )
                },
                mal2Morf?.let {
                    extractPriorTumor(
                        it,
                        mal2TopoSublok,
                        mal2Syst,
                        mal2Int,
                        2,
                        mal2Tumsoort,
                        mal2Stadium,
                        listOfNotNull(mal2SystCode1, mal2SystCode2, mal2SystCode3, mal2SystCode4, mal2SystCode5)
                    )
                },
                mal3Morf?.let {
                    extractPriorTumor(
                        it,
                        mal3TopoSublok,
                        mal3Syst,
                        mal3Int,
                        3,
                        mal3Tumsoort,
                        mal3Stadium,
                        listOfNotNull(mal3SystCode1, mal3SystCode2, mal3SystCode3, mal3SystCode4)
                    )
                },
                mal4Morf?.let {
                    extractPriorTumor(
                        it, mal4TopoSublok, mal4Syst, mal4Int, 4, mal4Tumsoort, mal4Stadium, emptyList()
                    )
                }
            )
        }
    }

    private fun extractPriorTumor(
        type: Int,
        location: String?,
        hadSystemic: Int?,
        interval: Int?,
        id: Int,
        category: Int?,
        stage: String?,
        treatments: List<String>
    ): PriorTumor {
        if (location == null) {
            throw IllegalStateException("Missing location for prior tumor with ID $id")
        }
        return PriorTumor(
            consolidatedTumorType = resolve(type),
            consolidatedTumorLocation = resolveLocation(location),
            hasHadTumorDirectedSystemicTherapy = resolve(hadSystemic),
            incidenceIntervalPrimaryTumor = interval,
            tumorPriorId = id,
            tumorLocationCategory = resolve(category),
            stageTNM = stage?.let { enumValueOf<StageTNM>(it) },
            systemicTreatments = treatments.map(::resolveTreatmentName)
        )
    }

    private inline fun <reified T : E?, reified E : Enum<E>> enumValueOfNullable(code: String?): T {
        return code?.let { enumValueOf<E>(it) } as T
    }

    private fun createTumorEpisodes(ncrRecords: List<NcrRecord>): TumorEpisodes {
        val (ncrDiagnosisRecords, ncrTreatmentRecords) = ncrRecords.partition { it.identification.epis == DIAGNOSIS_EPISODE }
        val diagnosisRecord = ncrDiagnosisRecords.minBy { it.identification.keyEid }
        val diagnosisEpisode = extractEpisode(diagnosisRecord)
        val diagnosis = with(diagnosisRecord) {
            val (hasBrafMutation, hasBrafV600EMutation) = when (molecularCharacteristics.brafMut) {
                0 -> Pair(false, false)
                1 -> Pair(true, null)
                2 -> Pair(true, true)
                3 -> Pair(true, false)
                9, null -> Pair(null, null)
                else -> throw IllegalStateException("Unexpected value for BRAF mutation: ${molecularCharacteristics.brafMut}")
            }
            val (hasRasMutation, hasKrasG12CMutation) = when (molecularCharacteristics.rasMut) {
                0 -> Pair(false, false)
                1 -> Pair(true, null)
                2 -> Pair(true, false)
                3 -> Pair(true, true)
                9, null -> Pair(null, null)
                else -> throw IllegalStateException("Unexpected value for RAS mutation: ${molecularCharacteristics.rasMut}")
            }

            Diagnosis(
                cci = resolve(comorbidities.cci),
                cciNumberOfCategories = resolve(comorbidities.cciCat),
                cciHasAids = resolve(comorbidities.cciAids),
                cciHasCongestiveHeartFailure = resolve(comorbidities.cciChf),
                cciHasCollagenosis = resolve(comorbidities.cciCollagenosis),
                cciHasCopd = resolve(comorbidities.cciCopd),
                cciHasCerebrovascularDisease = resolve(comorbidities.cciCvd),
                cciHasDementia = resolve(comorbidities.cciDementia),
                cciHasDiabetesMellitus = resolve(comorbidities.cciDm),
                cciHasDiabetesMellitusWithEndOrganDamage = resolve(comorbidities.cciEodDm),
                cciHasOtherMalignancy = resolve(comorbidities.cciMalignancy),
                cciHasOtherMetastaticSolidTumor = resolve(comorbidities.cciMetastatic),
                cciHasMyocardialInfarct = resolve(comorbidities.cciMi),
                cciHasMildLiverDisease = resolve(comorbidities.cciMildLiver),
                cciHasHemiplegiaOrParaplegia = resolve(comorbidities.cciPlegia),
                cciHasPeripheralVascularDisease = resolve(comorbidities.cciPvd),
                cciHasRenalDisease = resolve(comorbidities.cciRenal),
                cciHasLiverDisease = resolve(comorbidities.cciSevereLiver),
                cciHasUlcerDisease = resolve(comorbidities.cciUlcer),
                presentedWithIleus = resolve(clinicalCharacteristics.ileus),
                presentedWithPerforation = resolve(clinicalCharacteristics.perforatie),
                anorectalVergeDistanceCategory = resolve(clinicalCharacteristics.anusAfst),
                hasMsi = resolve(molecularCharacteristics.msiStat),
                hasBrafMutation = hasBrafMutation,
                hasBrafV600EMutation = hasBrafV600EMutation,
                hasRasMutation = hasRasMutation,
                hasKrasG12CMutation = hasKrasG12CMutation
            )
        }

        return TumorEpisodes(diagnosis, diagnosisEpisode, ncrTreatmentRecords.map(::extractEpisode))
    }

    private fun extractEpisode(record: NcrRecord): Episode {
        return with(record) {
            val responseMeasure = treatmentResponse.responsUitslag?.let {
                if (it == "99" || it == "0") null else enumValueOf<ResponseMeasureType>(it)
            }
                ?.let { ResponseMeasure(it, treatmentResponse.responsInt ?: 0) }

            val pfsMeasures = extractPfsMeasures(treatmentResponse)
            val systemicTreatmentSchemes = extractSystemicTreatmentSchemes(treatment.systemicTreatment, responseMeasure, pfsMeasures)
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
                whoStatusPreTreatmentStart = patientCharacteristics.perfStat,
                asaClassificationPreSurgeryOrEndoscopy = resolve(patientCharacteristics.asa),
                tumorIncidenceYear = primaryDiagnosis.incjr,
                tumorBasisOfDiagnosis = resolve(primaryDiagnosis.diagBasis),
                tumorLocation = resolveLocation(primaryDiagnosis.topoSublok),
                tumorDifferentiationGrade = resolve(primaryDiagnosis.diffgrad.toInt()),
                tnmCT = enumValueOfNullable(primaryDiagnosis.ct),
                tnmCN = enumValueOfNullable(primaryDiagnosis.cn),
                tnmCM = enumValueOfNullable(primaryDiagnosis.cm),
                tnmPT = enumValueOfNullable(primaryDiagnosis.pt),
                tnmPN = enumValueOfNullable(primaryDiagnosis.pn),
                tnmPM = enumValueOfNullable(primaryDiagnosis.pm),
                stageCTNM = enumValueOfNullable(primaryDiagnosis.cstadium),
                stagePTNM = enumValueOfNullable(primaryDiagnosis.pstadium),
                stageTNM = enumValueOfNullable(primaryDiagnosis.stadium),
                numberOfInvestigatedLymphNodes = primaryDiagnosis.ondLymf,
                numberOfPositiveLymphNodes = primaryDiagnosis.posLymf,
                distantMetastasesStatus = resolve(identification.metaEpis),
                metastases = extractMetastases(metastaticDiagnosis),
                numberOfLiverMetastases = resolve(metastaticDiagnosis.metaLeverAantal),
                maximumSizeOfLiverMetastasisInMm = metastaticDiagnosis.metaLeverAfm,
                hasDoublePrimaryTumor = resolve(clinicalCharacteristics.dubbeltum),
                mesorectalFasciaIsClear = mesorectalFasciaIsClear,
                distanceToMesorectalFascia = distanceToMesorectalFascia,
                venousInvasionCategory = resolve(clinicalCharacteristics.veneusInvas),
                lymphaticInvasionCategory = resolve(clinicalCharacteristics.lymfInvas),
                extraMuralInvasionCategory = resolve(clinicalCharacteristics.emi),
                tumorRegression = resolve(clinicalCharacteristics.tumregres),
                labMeasurements = extractLabMeasurements(labValues),
                hasReceivedTumorDirectedTreatment = resolve(treatment.tumgerichtTher),
                reasonRefrainmentFromTumorDirectedTreatment = resolve(treatment.geenTherReden),
                hasParticipatedInTrial = resolve(treatment.deelnameStudie),
                gastroenterologyResections = extractGastroenterologyResections(treatment.gastroenterologyResection),
                surgeries = extractSurgeries(treatment.primarySurgery),
                metastasesSurgeries = extractMetastasesSurgeries(treatment.metastaticSurgery),
                radiotherapies = extractRadiotherapies(treatment.primaryRadiotherapy),
                metastasesRadiotherapies = extractMetastasesRadiotherapies(treatment.metastaticRadiotherapy),
                hasHadHipecTreatment = resolve(treatment.hipec.hipec),
                intervalTumorIncidenceHipecTreatment = treatment.hipec.hipecInt1,
                systemicTreatments = systemicTreatmentSchemes.flatMap(SystemicTreatmentScheme::treatments),
                systemicTreatmentSchemes = systemicTreatmentSchemes,
                hasHadPreSurgeryRadiotherapy = hasHadPreSurgeryRadiotherapy,
                hasHadPostSurgeryRadiotherapy = hasHadPostSurgeryRadiotherapy,
                hasHadPreSurgeryChemoRadiotherapy = hasHadPreSurgeryChemoRadiotherapy,
                hasHadPostSurgeryChemoRadiotherapy = hasHadPostSurgeryChemoRadiotherapy,
                hasHadPreSurgerySystemicChemotherapy = hasHadPreSurgerySystemicChemotherapy,
                hasHadPostSurgerySystemicChemotherapy = hasHadPostSurgerySystemicChemotherapy,
                hasHadPreSurgerySystemicTargetedTherapy = hasHadPreSurgerySystemicTargetedTherapy,
                hasHadPostSurgerySystemicTargetedTherapy = hasHadPostSurgerySystemicTargetedTherapy,
                responseMeasure = responseMeasure,
                pfsMeasures = pfsMeasures
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
                .mapNotNull { (event, type, interval) -> event?.let { PfsMeasure(resolve(event), resolve(type), interval) } }
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
                .mapNotNull { (type, startInterval, stopInterval) ->
                    type?.let {
                        MetastasesRadiotherapy(resolveMetastasesRadiotherapyType(it), startInterval?.toInt(), stopInterval?.toInt())
                    }
                }
        }
    }

    private fun extractRadiotherapies(ncrPrimaryRadiotherapy: NcrPrimaryRadiotherapy): List<Radiotherapy> {
        return with(ncrPrimaryRadiotherapy) {
            listOfNotNull(
                rtType1?.let { Radiotherapy(resolve(it), rtDosis1, rtStartInt1, rtStopInt1) },
                rtType2?.let { Radiotherapy(resolve(it), rtDosis2, rtStartInt2, rtStopInt2) }
            )
        }
    }

    private fun extractMetastasesSurgeries(metastaticSurgery: NcrMetastaticSurgery): List<MetastasesSurgery> {
        return with(metastaticSurgery) {
            listOf(
                Triple(metaChirCode1, metaChirRad1, metaChirInt1),
                Triple(metaChirCode2, metaChirRad2, metaChirInt2),
                Triple(metaChirCode3, metaChirRad3, metaChirInt3)
            )
                .mapNotNull { (type, radicality, interval) ->
                    type?.let { MetastasesSurgery(resolveMetastasesSurgeryType(it), resolve(radicality), interval, null) }
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
            resolve(surgeryType),
            resolve(technique),
            resolve(urgency),
            resolve(radicality),
            resolve(margins),
            resolve(leakage),
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
        }
            .filter { it.first != null }
            .map { (type, interval) -> GastroenterologyResection(resolve(type), interval) }
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
            )
                .flatMap { (measure, values) ->
                    values.filterNot { (value, interval) -> value == null && interval == null }
                        .map { (value, interval) -> LabMeasurement(measure, value as Double, measure.unit, interval, null, null) }
                }

            return measurements + listOfNotNull(
                periSurgicalCeaMeasurement(prechirCea, true),
                periSurgicalCeaMeasurement(postchirCea, false)
            )
        }
    }

    private fun periSurgicalCeaMeasurement(measurement: Double?, isPreSurgical: Boolean) = measurement?.let {
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
                    resolveLocation(location),
                    intervalDays[i],
                    resolve(progression[i])
                )
            }
        }
    }

    private fun diagnosisEpisodes(ncrRecords: List<NcrRecord>): List<NcrRecord> {
        return ncrRecords.filter { it.identification.epis == DIAGNOSIS_EPISODE }
    }
}