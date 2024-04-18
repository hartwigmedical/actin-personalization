package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.Diagnosis
import com.hartwig.actin.personalization.datamodel.Episode
import com.hartwig.actin.personalization.datamodel.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.LabMeasure
import com.hartwig.actin.personalization.datamodel.LabMeasurement
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.MetastasesSurgery
import com.hartwig.actin.personalization.datamodel.Metastasis
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.Surgery
import com.hartwig.actin.personalization.datamodel.TumorEpisodes
import com.hartwig.actin.personalization.datamodel.TumorOfInterest
import com.hartwig.actin.personalization.datamodel.TumorType
import com.hartwig.actin.personalization.ncr.datamodel.NcrGastroenterologyResection
import com.hartwig.actin.personalization.ncr.datamodel.NcrLabValues
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticDiagnosis
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticSurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrPrimarySurgery
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrLocationMapper.resolveLocation
import com.hartwig.actin.personalization.ncr.interpretation.mapper.resolveMetastasesSurgeryType
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
            episodesPerTumorOfInterest = determineEpisodesPerTumorOfInterest(ncrRecords),
            priorTumors = listOf()
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
        return Pair(createTumorOfInterest(ncrRecords), createTumorEpisodes(ncrRecords))
    }

    private fun createTumorOfInterest(ncrRecords: List<NcrRecord>): TumorOfInterest {
        return TumorOfInterest(
            consolidatedTumorType = TumorType.ADENOCARCINOMA_DIFFUSE_TYPE,
            consolidatedTumorLocation = Location.UNKNOWN_PRIMARY_TUMOR,
            hasHadTumorDirectedSystemicTherapy = false,
            hasHadPriorTumor = false,
            intervalsTumorIncidenceDiagnosisTumorPrior = listOf()
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
                mesorectalFasciaIsClear = null,  // TODO: do we need this?
                distanceToMesorectalFascia = extractDistanceToMesorectalFascia(clinicalCharacteristics.mrfAfst),
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
                radiotherapies = listOf(),
                radiotherapiesMetastases = listOf(),
                hasHadHipecTreatment = false,
                intervalTumorIncidenceHipecTreatment = null,
                systemicTreatments = listOf(),
                systemicTreatmentSchemes = listOf(),
                hasHadPreSurgeryRadiotherapy = false,
                hasHadPostSurgeryRadiotherapy = false,
                hasHadPreSurgeryChemoRadiotherapy = false,
                hasHadPostSurgeryChemoRadiotherapy = true,
                hasHadPreSurgerySystemicChemotherapy = false,
                hasHadPostSurgerySystemicChemotherapy = false,
                hasHadPreSurgerySystemicTargetedTherapy = false,
                hasHadPostSurgerySystemicTargetedTherapy = false,
                responseMeasure = null,
                pfsMeasures = listOf()
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

    private fun extractDistanceToMesorectalFascia(mrfAfst: Int?): Int? {
        return when (mrfAfst) {
            null, 222, 888, 999 -> null
            111 -> 0
            in 0..20 -> mrfAfst
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