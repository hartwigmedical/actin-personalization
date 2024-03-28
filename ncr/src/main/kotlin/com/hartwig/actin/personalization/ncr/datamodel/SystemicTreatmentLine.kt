package com.hartwig.actin.personalization.ncr.datamodel

data class SystemicTreatmentLine(
    val treatmentLineName: TreatmentLineName,
    val drugs: List<Drug>,
    val intervalTumorIncidenceTreatmentLineStartMin: Int,
    val intervalTumorIncidenceTreatmentLineStartMax: Int,
    val intervalTumorIncidenceTreatmentLineStopMin: Int,
    val intervalTumorIncidenceTreatmentLineStopMax: Int,

    val treatmentResponse: ResponseMeasure?, //Should be added when episode response measure belongs to this treatment (i.e. should be prior to min start of following systemic treatment line)
    val intervalTreatmentStartMinResponseDate: Int?,
    val intervalTreatmentStartMaxResponseDate: Int?,

    val treatmentPfs: Int?, //Max pfs measure of type PROGRESSION/DEATH belongs to this treatment (i.e. should be measured prior to min start of following systemic treatment line)
    val treatmentRawPfs: List<PFSMeasure> //All pfs measure entries belonging to this treatment (i.e. should be measured prior to min start of following systemic treatment line)
)
