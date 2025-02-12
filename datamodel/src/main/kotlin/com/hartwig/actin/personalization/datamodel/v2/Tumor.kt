package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.v2.assessment.AsaAssessment
import com.hartwig.actin.personalization.datamodel.v2.assessment.ComorbidityAssessment
import com.hartwig.actin.personalization.datamodel.v2.assessment.LabMeasurement
import com.hartwig.actin.personalization.datamodel.v2.assessment.MolecularResult
import com.hartwig.actin.personalization.datamodel.v2.assessment.WhoAssessment
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.MetastaticDiagnosis
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.PrimaryDiagnosis
import com.hartwig.actin.personalization.datamodel.v2.diagnosis.PriorTumor
import com.hartwig.actin.personalization.datamodel.v2.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.v2.outcome.ResponseMeasure
import com.hartwig.actin.personalization.datamodel.v2.outcome.SurvivalMeasure
import com.hartwig.actin.personalization.datamodel.v2.treatment.GastroenterologyResection
import com.hartwig.actin.personalization.datamodel.v2.treatment.HipecTreatment
import com.hartwig.actin.personalization.datamodel.v2.treatment.MetastaticRadiotherapy
import com.hartwig.actin.personalization.datamodel.v2.treatment.MetastaticSurgery
import com.hartwig.actin.personalization.datamodel.v2.treatment.PrimaryRadiotherapy
import com.hartwig.actin.personalization.datamodel.v2.treatment.PrimarySurgery
import com.hartwig.actin.personalization.datamodel.v2.treatment.ReasonRefrainmentFromTumorDirectedTreatment
import com.hartwig.actin.personalization.datamodel.v2.treatment.SystemicTreatment
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
    val comorbidityAssessments : List<ComorbidityAssessment>,
    val molecularResults : List<MolecularResult>,
    val labMeasurements: List<LabMeasurement>,

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

    val responseMeasures: List<ResponseMeasure>,
    val progressionMeasures: List<ProgressionMeasure>
)