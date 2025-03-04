package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.treatment.MetastaticPresence
import com.hartwig.actin.personalization.datamodel.treatment.ReasonRefrainmentFromTreatment
import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

object NcrTreatmentEpisodeExtractor {

    fun extract(records: List<NcrRecord>) : List<TreatmentEpisode> {
        return records.map { extractTreatmentEpisode(it) }
    }

    private fun extractTreatmentEpisode(record: NcrRecord) : TreatmentEpisode {
        return TreatmentEpisode(
            metastaticPresence = MetastaticPresence.AT_START,
            reasonRefrainmentFromTreatment = ReasonRefrainmentFromTreatment.NOT_APPLICABLE,
            gastroenterologyResections = emptyList(),
            primarySurgeries = emptyList(),
            metastaticSurgeries = emptyList(),
            hipecTreatments = emptyList(),
            primaryRadiotherapies = emptyList(),
            metastaticRadiotherapies = emptyList(),
            systemicTreatments = emptyList(),
            responseMeasures = emptyList(),
            progressionMeasures = emptyList()
        )
        /*
                            hasReceivedTumorDirectedTreatment = NcrBooleanMapper.resolve(treatment.tumgerichtTher) == true,
                reasonRefrainmentFromTumorDirectedTreatment =
                NcrReasonRefrainmentFromTumorDirectedTherapyMapper.resolve(treatment.geenTherReden),
                hasParticipatedInTrial = NcrBooleanMapper.resolve(treatment.deelnameStudie),

             */
    }
}