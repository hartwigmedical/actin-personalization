package com.hartwig.actin.personalization.ncr.interpretation

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.interpretation.conversion.MetastaticRtIntervalConversion
import kotlin.math.max

const val DIAGNOSIS_EPISODE = "DIA"

object NcrFunctions {

    fun diagnosisRecord(records: List<NcrRecord>): NcrRecord {
        return records.single { it.identification.epis == DIAGNOSIS_EPISODE }
    }

    fun metastaticRecord(records: List<NcrRecord>): NcrRecord {
        return records.single { it.identification.metaEpis == 1 || it.identification.metaEpis == 2 }
    }

    fun daysBetweenRecords(records: List<NcrRecord>): Map<NcrRecord, Int> {
        val diagnosis = diagnosisRecord(records)
        return records.associateWith { calculateDaysBetween(diagnosis, it) }
    }

    private fun calculateDaysBetween(diagnosis: NcrRecord, record: NcrRecord): Int {
        if (diagnosis == record) {
            return 0
        }

        val intervals = listOfNotNull(
            record.treatment.gastroenterologyResection.mdlResInt1,
            record.treatment.primarySurgery.chirInt1,
            record.treatment.metastaticSurgery.metaChirInt1,
            record.treatment.primaryRadiotherapy.rtStartInt1,
            MetastaticRtIntervalConversion.convert(record.treatment.metastaticRadiotherapy.metaRtStartInt1),
            record.treatment.systemicTreatment.systStartInt1
        )

        return if (intervals.isNotEmpty()) {
            intervals.min()
        } else {
            max(0, record.patientCharacteristics.leeft - diagnosis.patientCharacteristics.leeft - 365)
        }
    }
}