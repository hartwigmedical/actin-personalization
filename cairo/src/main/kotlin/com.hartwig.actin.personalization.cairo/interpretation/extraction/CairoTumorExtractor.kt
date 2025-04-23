package com.hartwig.actin.personalization.cairo.interpretation.extraction

import com.hartwig.actin.personalization.cairo.datamodel.CairoRecord
import com.hartwig.actin.personalization.cairo.interpretation.mapping.CairoVitalStatusMapper
import com.hartwig.actin.personalization.datamodel.Tumor
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasure
import kotlin.math.roundToInt

object CairoTumorExtractor {
    fun extractTumor(records: List<CairoRecord>): Tumor {
        val diagnosis = records.first()
        return Tumor(
            diagnosisYear = extractDiagnosisYear(records),
            ageAtDiagnosis = diagnosis.patientCharacteristics.age.roundToInt(),
            latestSurvivalStatus = extractLatestSurvivalMeasure(diagnosis),
            priorTumors = null, //TODO: in malignancy column sometimes tumor mentions, but is free text
            primaryDiagnosis = CairoPrimaryDiagnosisExtractor.extract(records),
            metastaticDiagnosis = CairoMetastaticDiagnosisExtractor.extract(records),
            whoAssessments =,
            asaAssessments =,
            comorbidityAssessments =,
            molecularResults =,
            labMeasurements =,
            treatmentEpisodes =
        )
    }
    private fun extractDiagnosisYear(records: List<CairoRecord>): Int {
        return records.first().primaryDiagnosis.dateDiagnosis //TODO still need to get the year out of the complete date
    }

    private fun extractLatestSurvivalMeasure(diagnosisRecord: CairoRecord): SurvivalMeasure {
        return SurvivalMeasure(
            daysSinceDiagnosis = (diagnosisRecord.treatmentResponse.osDate - diagnosisRecord.primaryDiagnosis.dateDiagnosis), //TODO still need to figure out
            isAlive = CairoVitalStatusMapper.resolve(diagnosisRecord.treatmentResponse.osEvent)
        )
    }
}