package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord

private const val PRE_SURGERY_CODE = 1
private const val POST_SURGERY_CODE = 2

class TreatmentRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    override fun apply(tumorRecords: List<NcrRecord>): Boolean {
        return listOf(
            ::hasConsistentPreAndPostSurgicalIntervalsForChemoAndRadiotherapy,
            ::hasValidPrimarySurgery,
            ::hasValidPrimaryRadiotherapy,
            ::hasValidGastroResection,
            ::hasValidSystemicTreatment,
            ::hasValidSystemCodes
        ).all { it(tumorRecords) }
    }

    private fun hasConsistentPreAndPostSurgicalIntervals(
        surgeryChir: Int?,
        surgeryStartInt: Int?,
        otherTreatmentCode: Int?,
        otherTreatmentStartInt: Int?
    ): Boolean {
        return when (otherTreatmentCode) {
            PRE_SURGERY_CODE -> surgeryChir == null ||
                    (surgeryStartInt != null && otherTreatmentStartInt != null && otherTreatmentStartInt <= surgeryStartInt)

            POST_SURGERY_CODE -> surgeryChir != null &&
                    (surgeryStartInt != null && otherTreatmentStartInt != null && otherTreatmentStartInt >= surgeryStartInt)

            else -> true
        }
    }

    internal fun hasConsistentPreAndPostSurgicalIntervalsForChemoAndRadiotherapy(tumorRecords: List<NcrRecord>): Boolean {
        val hasConsistentPreAndPostSurgicalIntervalsForChemoAndRadiotherapy =
            tumorRecords.map { it.treatment }.all { it ->
                hasConsistentPreAndPostSurgicalIntervals(
                    it.primarySurgery.chir,
                    it.primarySurgery.chirInt1,
                    it.primaryRadiotherapy.rt,
                    it.primaryRadiotherapy.rtStartInt1
                ) &&
                        hasConsistentPreAndPostSurgicalIntervals(
                            it.primarySurgery.chir,
                            it.primarySurgery.chirInt1,
                            it.primaryRadiotherapy.chemort,
                            it.primaryRadiotherapy.rtStartInt1
                        )
            }

        if (!hasConsistentPreAndPostSurgicalIntervalsForChemoAndRadiotherapy) {
            log("Invalid therapy pre/post codes for tumor ${tumorRecords.tumorId()}")
        }

        return hasConsistentPreAndPostSurgicalIntervalsForChemoAndRadiotherapy
    }

    internal fun hasValidPrimarySurgery(tumorRecords: List<NcrRecord>): Boolean {
        val hasValidPrimarySurgery = tumorRecords.map { it.treatment }.all { treatment ->
            val hasPrimarySurgeryChir = treatment.primarySurgery.chir == 1
            val hasPrimarySurgeryType1 = treatment.primarySurgery.chirType1.notZeroNorNull()
            val hasPrimarySurgeryType2 = treatment.primarySurgery.chirType2.notZeroNorNull()
            hasPrimarySurgeryChir == (hasPrimarySurgeryType1 || hasPrimarySurgeryType2)
        }

        if (!hasValidPrimarySurgery) log("Primary surgery validity check failed for tumor ${tumorRecords.tumorId()}")

        return hasValidPrimarySurgery
    }

    internal fun hasValidPrimaryRadiotherapy(tumorRecords: List<NcrRecord>): Boolean {
        val hasValidPrimaryRadiotherapy = tumorRecords.map { it.treatment }.all { treatment ->
            val hasPrimaryRadiotherapyRt = treatment.primaryRadiotherapy.rt != 0
            val hasPrimaryRadiotherapyChemort = treatment.primaryRadiotherapy.chemort.notZeroNorNull()
            val hasPrimaryRadiotherapyType1 = treatment.primaryRadiotherapy.rtType1.notZeroNorNull()
            val hasPrimaryRadiotherapyType2 = treatment.primaryRadiotherapy.rtType2.notZeroNorNull()
            (hasPrimaryRadiotherapyRt || hasPrimaryRadiotherapyChemort) == (hasPrimaryRadiotherapyType1 || hasPrimaryRadiotherapyType2)
        }

        if (!hasValidPrimaryRadiotherapy) log("Primary radiotherapy validity check failed for tumor ${tumorRecords.tumorId()}")

        return hasValidPrimaryRadiotherapy
    }

    internal fun hasValidGastroResection(tumorRecords: List<NcrRecord>): Boolean {
        val hasValidGastroResection = tumorRecords.map { it.treatment }.all { treatment ->
            val hasGastroResection = treatment.gastroenterologyResection.mdlRes.notZeroNorNull()
            val hasGastroResectionType1 = treatment.gastroenterologyResection.mdlResType1.notZeroNorNull()
            val hasGastroResectionType2 = treatment.gastroenterologyResection.mdlResType2.notZeroNorNull()
            hasGastroResection == (hasGastroResectionType1 || hasGastroResectionType2)
        }

        if (!hasValidGastroResection) log("Gastrointestinal resection validity check failed for tumor ${tumorRecords.tumorId()}")

        return hasValidGastroResection
    }

    internal fun hasValidSystemicTreatment(tumorRecords: List<NcrRecord>): Boolean {
        val hasValidSystemicTreatment = tumorRecords.map { it.treatment }.all { treatment ->
            val hasSystemicChemo = treatment.systemicTreatment.chemo != 0
            val hasSystemicTarget = treatment.systemicTreatment.target != 0
            val hasSystemicCode = listOf(
                treatment.systemicTreatment.systCode1,
                treatment.systemicTreatment.systCode2,
                treatment.systemicTreatment.systCode3,
                treatment.systemicTreatment.systCode4,
                treatment.systemicTreatment.systCode5,
                treatment.systemicTreatment.systCode6,
                treatment.systemicTreatment.systCode7,
                treatment.systemicTreatment.systCode8,
                treatment.systemicTreatment.systCode9,
                treatment.systemicTreatment.systCode10,
                treatment.systemicTreatment.systCode11,
                treatment.systemicTreatment.systCode12,
                treatment.systemicTreatment.systCode13,
                treatment.systemicTreatment.systCode14
            ).any { code -> code != null }
            hasSystemicCode == (hasSystemicChemo || hasSystemicTarget)
        }

        if (!hasValidSystemicTreatment) {
            log("Systemic treatment validation failed: chemo, target, or codes are inconsistent for tumor ${tumorRecords.tumorId()}")
        }

        return hasValidSystemicTreatment
    }

    internal fun hasValidSystemCodes(tumorRecords: List<NcrRecord>): Boolean {
        val treatmentRecords = tumorRecords.map { it.treatment }
        val hasValidSystemCodes = treatmentRecords.all {
            listOf(
                it.systemicTreatment.systCode1 to it.systemicTreatment.systSchemanum1,
                it.systemicTreatment.systCode2 to it.systemicTreatment.systSchemanum2,
                it.systemicTreatment.systCode3 to it.systemicTreatment.systSchemanum3,
                it.systemicTreatment.systCode4 to it.systemicTreatment.systSchemanum4,
                it.systemicTreatment.systCode5 to it.systemicTreatment.systSchemanum5,
                it.systemicTreatment.systCode6 to it.systemicTreatment.systSchemanum6,
                it.systemicTreatment.systCode7 to it.systemicTreatment.systSchemanum7,
                it.systemicTreatment.systCode8 to it.systemicTreatment.systSchemanum8,
                it.systemicTreatment.systCode9 to it.systemicTreatment.systSchemanum9,
                it.systemicTreatment.systCode10 to it.systemicTreatment.systSchemanum10,
                it.systemicTreatment.systCode11 to it.systemicTreatment.systSchemanum11,
                it.systemicTreatment.systCode12 to it.systemicTreatment.systSchemanum12,
                it.systemicTreatment.systCode13 to it.systemicTreatment.systSchemanum13,
                it.systemicTreatment.systCode14 to it.systemicTreatment.systSchemanum14,
            ).all { (code, schemaNum) ->
                code != null || schemaNum == null
            }
        }

        if (!hasValidSystemCodes) log("Systemic codes validation failed: found schemaNum without code for tumor ${tumorRecords.tumorId()}")

        return hasValidSystemCodes
    }
}
