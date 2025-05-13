package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.ComorbidityAssessment
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasurement
import com.hartwig.actin.personalization.datamodel.assessment.MolecularResult
import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasurement
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import kotlinx.serialization.Serializable

@Serializable
data class ReferenceEntry(
    val source: ReferenceSource,
    val sourceId: Int,
    val diagnosisYear: Int,
    val ageAtDiagnosis: Int,
    val sex: Sex,
    val latestSurvivalMeasurement: SurvivalMeasurement,
    val priorTumors: List<PriorTumor>,
    val primaryDiagnosis: PrimaryDiagnosis,
    val metastaticDiagnosis: MetastaticDiagnosis,
    val whoAssessments: List<WhoAssessment>,
    val asaAssessments: List<AsaAssessment>,
    val comorbidityAssessments: List<ComorbidityAssessment>,
    val molecularResults: List<MolecularResult>,
    val labMeasurements: List<LabMeasurement>,
    val treatmentEpisodes: List<TreatmentEpisode>
)
