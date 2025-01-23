package com.hartwig.actin.personalization.datamodel.v2

import com.hartwig.actin.personalization.datamodel.Sex
import com.hartwig.actin.personalization.datamodel.TumorEntry
import kotlinx.serialization.Serializable
import java.time.LocalDate

data class ReferencePatient(
    val sex: Sex,

    val observedSurvivalFromTumorIncidenceDays: Int,
    val hadSurvivalEvent: Boolean,

    val diagnosis : Diagnosis
)
