package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.datamodel.DiagnosisEpisode
import com.hartwig.actin.personalization.datamodel.LabMeasure
import com.hartwig.actin.personalization.datamodel.LabMeasurement
import com.hartwig.actin.personalization.datamodel.Location
import com.hartwig.actin.personalization.datamodel.Metastasis
import com.hartwig.actin.personalization.datamodel.PatientRecord
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.TumorEpisodes
import com.hartwig.actin.personalization.datamodel.TumorOfInterest
import com.hartwig.actin.personalization.datamodel.TumorType
import com.hartwig.actin.personalization.ncr.datamodel.NcrLabValues
import com.hartwig.actin.personalization.ncr.datamodel.NcrMetastaticDiagnosis
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.mapper.NcrLocationMapper.resolveLocation
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
        val (ncrDiagnosisEpisodes, ncrTreatmentEpisodes) = ncrRecords.partition { it.identification.epis == DIAGNOSIS_EPISODE }
        val diagnosisEpisode = ncrDiagnosisEpisodes.minBy { it.identification.keyEid }.let {
            DiagnosisEpisode(
                id = it.identification.keyEid,
                order = it.identification.teller,
                whoStatusPreTreatmentStart = it.patientCharacteristics.perfStat,
                asaClassificationPreSurgeryOrEndoscopy = resolve(it.patientCharacteristics.asa),
                tumorIncidenceYear = it.primaryDiagnosis.incjr,
                tumorBasisOfDiagnosis = resolve(it.primaryDiagnosis.diagBasis),
                tumorLocation = resolveLocation(it.primaryDiagnosis.topoSublok),
                tumorDifferentiationGrade = resolve(it.primaryDiagnosis.diffgrad.toInt()),
                tnmCT = enumValueOfNullable(it.primaryDiagnosis.ct),
                tnmCN = enumValueOfNullable(it.primaryDiagnosis.cn),
                tnmCM = enumValueOfNullable(it.primaryDiagnosis.cm),
                tnmPT = enumValueOfNullable(it.primaryDiagnosis.pt),
                tnmPN = enumValueOfNullable(it.primaryDiagnosis.pn),
                tnmPM = enumValueOfNullable(it.primaryDiagnosis.pm),
                stageCTNM = enumValueOfNullable(it.primaryDiagnosis.cstadium),
                stagePTNM = enumValueOfNullable(it.primaryDiagnosis.pstadium),
                stageTNM = enumValueOfNullable(it.primaryDiagnosis.stadium),
                numberOfInvestigatedLymphNodes = it.primaryDiagnosis.ondLymf,
                numberOfPositiveLymphNodes = it.primaryDiagnosis.posLymf,
                distantMetastasesStatus = resolve(it.identification.metaEpis),
                metastases = extractMetastases(it.metastaticDiagnosis),
                numberOfLiverMetastases = resolve(it.metastaticDiagnosis.metaLeverAantal),
                maximumSizeOfLiverMetastasisInMm = it.metastaticDiagnosis.metaLeverAfm,
                hasDoublePrimaryTumor = resolve(it.clinicalCharacteristics.dubbeltum),
                mesorectalFasciaIsClear = null,
                distanceToMesorectalFascia = extractDistanceToMesorectalFascia(it.clinicalCharacteristics.mrfAfst),
                venousInvasionCategory = resolve(it.clinicalCharacteristics.veneusInvas),
                lymphaticInvasionCategory = resolve(it.clinicalCharacteristics.lymfInvas),
                extraMuralInvasionCategory = resolve(it.clinicalCharacteristics.emi),
                tumorRegression = resolve(it.clinicalCharacteristics.tumregres),
                labMeasurements = extractLabMeasurements(it.labValues),
                hasReceivedTumorDirectedTreatment = false,
                reasonRefrainmentFromTumorDirectedTreatment = null,
                hasParticipatedInTrial = null,
                gastroenterologyResections = listOf(),
                surgeries = listOf(),
                surgeriesMetastases = listOf(),
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
                pfsMeasures = listOf(),
                cci = resolve(it.comorbidities.cci),
                cciNumberOfCategories = resolve(it.comorbidities.cciCat),
                cciHasAids = resolve(it.comorbidities.cciAids),
                cciHasCongestiveHeartFailure = resolve(it.comorbidities.cciChf),
                cciHasCollagenosis = resolve(it.comorbidities.cciCollagenosis),
                cciHasCopd = resolve(it.comorbidities.cciCopd),
                cciHasCerebrovascularDisease = resolve(it.comorbidities.cciCvd),
                cciHasDementia = resolve(it.comorbidities.cciDementia),
                cciHasDiabetesMellitus = resolve(it.comorbidities.cciDm),
                cciHasDiabetesMellitusWithEndOrganDamage = resolve(it.comorbidities.cciEodDm),
                cciHasOtherMalignancy = resolve(it.comorbidities.cciMalignancy),
                cciHasOtherMetastaticSolidTumor = resolve(it.comorbidities.cciMetastatic),
                cciHasMyocardialInfarct = resolve(it.comorbidities.cciMi),
                cciHasMildLiverDisease = resolve(it.comorbidities.cciMildLiver),
                cciHasHemiplegiaOrParaplegia = resolve(it.comorbidities.cciPlegia),
                cciHasPeripheralVascularDisease = resolve(it.comorbidities.cciPvd),
                cciHasRenalDisease = resolve(it.comorbidities.cciRenal),
                cciHasLiverDisease = resolve(it.comorbidities.cciSevereLiver),
                cciHasUlcerDisease = resolve(it.comorbidities.cciUlcer),
                presentedWithIleus = resolve(it.clinicalCharacteristics.ileus),
                presentedWithPerforation = resolve(it.clinicalCharacteristics.perforatie),
                anorectalVergeDistanceCategory = resolve(it.clinicalCharacteristics.anusAfst),
                hasMsi = null,
                hasBrafMutation = null,
                hasBrafV600EMutation = null,
                hasRasMutation = null,
                hasKrasG12CMutation = null
            )
        }

        return TumorEpisodes(diagnosisEpisode, listOf())
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