package com.hartwig.actin.personalization.ncr.interpretation.extraction

import com.hartwig.actin.personalization.datamodel.treatment.TreatmentEpisode
import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

object NcrTreatmentEpisodeExtractor {

    fun extract(records: List<NcrRecord>) : List<TreatmentEpisode> {
        return emptyList()
        /*
                            hasReceivedTumorDirectedTreatment = NcrBooleanMapper.resolve(treatment.tumgerichtTher) == true,
                reasonRefrainmentFromTumorDirectedTreatment =
                NcrReasonRefrainmentFromTumorDirectedTherapyMapper.resolve(treatment.geenTherReden),
                hasParticipatedInTrial = NcrBooleanMapper.resolve(treatment.deelnameStudie),

             */

    }
}