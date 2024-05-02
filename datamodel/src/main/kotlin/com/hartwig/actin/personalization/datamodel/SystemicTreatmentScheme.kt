package com.hartwig.actin.personalization.datamodel

data class SystemicTreatmentScheme( //Should combine treatments of same scheme number
    val treatmentComponents: List<SystemicTreatmentComponent>,
    val intervalTumorIncidenceTreatmentLineStartMin: Int?,
    val intervalTumorIncidenceTreatmentLineStartMax: Int?,
    val intervalTumorIncidenceTreatmentLineStopMin: Int?,
    val intervalTumorIncidenceTreatmentLineStopMax: Int?,

    //Should be added when episode response measure belongs to this treatment (i.e. should be prior to min start of following systemic treatment line)
    val treatmentResponse: ResponseMeasure?,

    val treatmentPfs: PfsMeasureType?, //Max pfs measure of type PROGRESSION/DEATH belongs to this treatment (i.e. should be measured prior to min start of following systemic treatment line)
    val treatmentRawPfs: List<PfsMeasure> //All pfs measure entries belonging to this treatment (i.e. should be measured prior to min start of following systemic treatment line)
) {

    fun intervalTreatmentStartMinResponseDate(): Int? = intervalTumorIncidenceTreatmentLineStartMin?.let(::responseInterval)

    fun intervalTreatmentStartMaxResponseDate(): Int? = intervalTumorIncidenceTreatmentLineStartMax?.let(::responseInterval)

    private fun responseInterval(referenceInterval: Int): Int? =
        treatmentResponse?.intervalTumorIncidenceResponseDate?.minus(referenceInterval)
}
