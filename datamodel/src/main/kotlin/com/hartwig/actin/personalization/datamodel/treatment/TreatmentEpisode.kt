package com.hartwig.actin.personalization.datamodel.treatment

import com.hartwig.actin.personalization.datamodel.outcome.ProgressionMeasure
import com.hartwig.actin.personalization.datamodel.outcome.ResponseMeasure
import kotlinx.serialization.Serializable

@Serializable
data class TreatmentEpisode (
    val metastaticPresence: MetastaticPresence,
    val reasonRefrainmentFromTreatment: ReasonRefrainmentFromTreatment,
    val gastroenterologyResections: List<GastroenterologyResection>,
    val primarySurgeries: List<PrimarySurgery>,
    val metastaticSurgeries: List<MetastaticSurgery>,
    val hipecTreatments: List<HipecTreatment>,
    val primaryRadiotherapies: List<PrimaryRadiotherapy>,
    val metastaticRadiotherapies: List<MetastaticRadiotherapy>,
    val systemicTreatments: List<SystemicTreatment>,
    val responseMeasures: List<ResponseMeasure>,
    val progressionMeasures: List<ProgressionMeasure>
)