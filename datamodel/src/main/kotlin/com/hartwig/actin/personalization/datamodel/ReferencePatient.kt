package com.hartwig.actin.personalization.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class ReferencePatient(
    val source: ReferenceSource,
    val sourceId: Int,
    val sex: Sex,
    val tumors: List<Tumor>
)

/* TODO (KD) Consider using below ReferenceRecord as the key datamodel (flatten tumors per patient in the datamodel already).  
    
data class ReferenceRecord(
    val source: ReferenceSource,
    val sourcePatientId: Int,
    val sex: Sex,
    val diagnosisYear: Int,
    val ageAtDiagnosis: Int,
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

 */