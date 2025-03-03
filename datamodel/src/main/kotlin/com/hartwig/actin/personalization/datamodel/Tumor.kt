package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.ComorbidityAssessment
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasurement
import com.hartwig.actin.personalization.datamodel.assessment.MolecularResult
import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasure
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import kotlinx.serialization.Serializable

@Serializable
data class Tumor(
    val diagnosisYear: Int,
    val ageAtDiagnosis: Int,
    val latestSurvivalStatus: SurvivalMeasure,
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