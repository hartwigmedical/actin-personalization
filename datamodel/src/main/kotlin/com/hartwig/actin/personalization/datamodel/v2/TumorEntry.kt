package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.ReasonRefrainmentFromTumorDirectedTreatment
import kotlinx.serialization.Serializable

@Serializable
data class TumorEntry(
    val diagnosisYear: Int,
    val ageAtDiagnosis: Int,
    val latestSurvivalStatus: LatestSurvivalStatus,

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

    val responseMeasure: ResponseMeasure? = null,
    val pfsMeasures: List<PfsMeasure>
)