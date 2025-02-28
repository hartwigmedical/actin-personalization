package com.hartwig.actin.personalization.datamodel

import com.hartwig.actin.personalization.datamodel.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.assessment.ComorbidityAssessment
import com.hartwig.actin.personalization.datamodel.assessment.LabMeasurement
import com.hartwig.actin.personalization.datamodel.assessment.MolecularResult
import com.hartwig.actin.personalization.datamodel.assessment.WhoAssessment
import com.hartwig.actin.personalization.datamodel.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.outcome.SurvivalMeasure
import com.hartwig.actin.personalization.datamodel.treatment.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.treatment.HipecTreatment
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.MetastaticSurgery
import com.hartwig.actin.personalization.datamodel.treatment.PrimaryRadiotherapy
import com.hartwig.actin.personalization.datamodel.treatment.PrimarySurgery
import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTumorDirectedTreatment
import com.hartwig.actin.personalization.datamodel.treatment.SystemicTreatment
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
    val molecularResults: List<MolecularResult> = emptyList(),
    val labMeasurements: List<LabMeasurement> = emptyList(),

    val hasReceivedTumorDirectedTreatment: Boolean,
    val reasonRefrainmentFromTumorDirectedTreatment: ReasonRefrainmentFromTumorDirectedTreatment? = null,
    val hasParticipatedInTrial: Boolean? = null,

    val gastroenterologyResections: List<GastroenterologyResection> = emptyList(),
    val primarySurgeries: List<PrimarySurgery> = emptyList(),
    val metastaticSurgeries: List<MetastaticSurgery> = emptyList(),
    val hipecTreatment: HipecTreatment,
    val primaryRadiotherapies: List<PrimaryRadiotherapy> = emptyList(),
    val metastaticRadiotherapies: List<MetastaticRadiotherapy> = emptyList(),
    val systemicTreatments: List<SystemicTreatment> = emptyList(),

    val responseMeasures: List<ResponseMeasure> = emptyList(),
    val progressionMeasures: List<ProgressionMeasure> = emptyList()
)