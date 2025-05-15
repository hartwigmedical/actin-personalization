package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.ReferenceEntry
import com.hartwig.actin.personalization.datamodel.ReferenceSource
import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.ComorbidityAssessment
import com.hartwig.actin.personalization.datamodel.assessment.MolecularResult
import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasurement
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.NcrFunctions
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrAsaClassificationMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrBooleanMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrSexMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrVitalStatusMapper
import com.hartwig.actin.personalization.ncr.interpretation.mapping.NcrWhoStatusMapper

object NcrReferenceEntryExtractor {

    fun extract(records: List<NcrRecord>): ReferenceEntry {
        val diagnosis = NcrFunctions.diagnosisRecord(records)

        return ReferenceEntry(
            source = ReferenceSource.NCR,
            sourceId = extractSourceId(records),
            diagnosisYear = diagnosis.primaryDiagnosis.incjr,
            ageAtDiagnosis = diagnosis.patientCharacteristics.leeft,
            sex = extractSex(records),
            latestSurvivalMeasurement = extractLatestSurvivalMeasure(diagnosis),
            priorTumors = NcrPriorTumorExtractor.extract(records),
            primaryDiagnosis = NcrPrimaryDiagnosisExtractor.extract(records),
            metastaticDiagnosis = NcrMetastaticDiagnosisExtractor.extract(records),
            whoAssessments = extractWhoAssessments(records),
            asaAssessments = extractAsaAssessments(records),
            comorbidityAssessments = extractComorbidityAssessments(diagnosis),
            molecularResults = extractMolecularResults(diagnosis),
            labMeasurements = NcrLabMeasurementExtractor.extract(records),
            treatmentEpisodes = NcrTreatmentEpisodeExtractor.extract(records)
        )
    }

    private fun extractSourceId(records: List<NcrRecord>): Int {
        return records.map { it.identification.keyZid }.distinct().single()
    }

    private fun extractSex(records: List<NcrRecord>): Sex {
        return NcrSexMapper.resolve(records.map { it.patientCharacteristics.gesl }.distinct().single())
    }

    private fun extractLatestSurvivalMeasure(diagnosisRecord: NcrRecord): SurvivalMeasurement {
        return SurvivalMeasurement(
            daysSinceDiagnosis = diagnosisRecord.patientCharacteristics.vitStatInt!!,
            isAlive = NcrVitalStatusMapper.resolve(diagnosisRecord.patientCharacteristics.vitStat!!)
        )
    }

    private fun extractWhoAssessments(records: List<NcrRecord>): List<WhoAssessment> {
        return NcrFunctions.recordsWithMinDaysSinceDiagnosis(records).mapNotNull {
            NcrWhoStatusMapper.resolve(it.key.patientCharacteristics.perfStat)?.let { whoStatus ->
                WhoAssessment(daysSinceDiagnosis = it.value, whoStatus = whoStatus)
            }
        }
    }

    private fun extractAsaAssessments(records: List<NcrRecord>): List<AsaAssessment> {
        return NcrFunctions.recordsWithMinDaysSinceDiagnosis(records).mapNotNull {
            NcrAsaClassificationMapper.resolve(it.key.patientCharacteristics.asa)?.let { classification ->
                AsaAssessment(daysSinceDiagnosis = it.value, classification = classification)
            }
        }
    }

    private fun extractComorbidityAssessments(diagnosis: NcrRecord): List<ComorbidityAssessment> {
        if (diagnosis.comorbidities.cci == null) {
            return emptyList()
        }

        return listOf(
            ComorbidityAssessment(
                daysSinceDiagnosis = 0,
                charlsonComorbidityIndex = diagnosis.comorbidities.cci,
                hasAids = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciAids)!!,
                hasCongestiveHeartFailure = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciChf)!!,
                hasCollagenosis = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciCollagenosis)!!,
                hasCopd = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciCopd)!!,
                hasCerebrovascularDisease = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciCvd)!!,
                hasDementia = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciDementia)!!,
                hasDiabetesMellitus = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciDm)!!,
                hasDiabetesMellitusWithEndOrganDamage = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciEodDm)!!,
                hasOtherMalignancy = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciMalignancy)!!,
                hasOtherMetastaticSolidTumor = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciMetastatic)!!,
                hasMyocardialInfarct = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciMi)!!,
                hasMildLiverDisease = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciMildLiver)!!,
                hasHemiplegiaOrParaplegia = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciPlegia)!!,
                hasPeripheralVascularDisease = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciPvd)!!,
                hasRenalDisease = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciRenal)!!,
                hasLiverDisease = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciSevereLiver)!!,
                hasUlcerDisease = NcrBooleanMapper.resolve(diagnosis.comorbidities.cciUlcer)!!
            )
        )
    }

    private fun extractMolecularResults(diagnosis: NcrRecord): List<MolecularResult> {
        val (hasBrafMutation, hasBrafV600EMutation) = when (diagnosis.molecularCharacteristics.brafMut) {
            0 -> Pair(false, false)
            1 -> Pair(true, null)
            2 -> Pair(true, true)
            3 -> Pair(true, false)
            9, null -> Pair(null, null)
            else -> throw IllegalStateException("Unexpected value for BRAF mutation: ${diagnosis.molecularCharacteristics.brafMut}")
        }

        val (hasRasMutation, hasKrasG12CMutation) = when (diagnosis.molecularCharacteristics.rasMut) {
            0 -> Pair(false, false)
            1 -> Pair(true, null)
            2 -> Pair(true, false)
            3 -> Pair(true, true)
            9, null -> Pair(null, null)
            else -> throw IllegalStateException("Unexpected value for RAS mutation: ${diagnosis.molecularCharacteristics.rasMut}")
        }
        val hasMsi = NcrBooleanMapper.resolve(diagnosis.molecularCharacteristics.msiStat)

        if (listOfNotNull(hasBrafMutation, hasBrafV600EMutation, hasRasMutation, hasKrasG12CMutation, hasMsi).isEmpty()) {
            return emptyList()
        }

        return listOf(
            MolecularResult(
                daysSinceDiagnosis = 0,
                hasMsi = hasMsi,
                hasBrafMutation = hasBrafMutation,
                hasBrafV600EMutation = hasBrafV600EMutation,
                hasRasMutation = hasRasMutation,
                hasKrasG12CMutation = hasKrasG12CMutation
            )
        )
    }
}
