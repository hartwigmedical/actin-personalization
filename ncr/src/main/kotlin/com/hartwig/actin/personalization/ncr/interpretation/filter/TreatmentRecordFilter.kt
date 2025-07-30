package com.hartwig.actin.personalization.ncr.interpretation.filter

import com.hartwig.actin.personalization.ncr.datamodel.NcrRecord
import com.hartwig.actin.personalization.ncr.datamodel.NcrTreatment

class TreatmentRecordFilter(override val logFilteredRecords: Boolean) : RecordFilter {

    internal fun hasAtLeastOneTreatment(treatment: NcrTreatment): Boolean {
        return listOf(
            treatment.primarySurgery.chir,
            treatment.primaryRadiotherapy.rt,
            treatment.systemicTreatment.chemo,
            treatment.hipec.hipec
        ).any { it.isNotZeroOrNull() } ||
                listOf(
                    treatment.metastaticSurgery.metaChirCode1,
                    treatment.metastaticSurgery.metaChirCode2,
                    treatment.metastaticSurgery.metaChirCode3,
                    treatment.metastaticRadiotherapy.metaRtCode1,
                    treatment.metastaticRadiotherapy.metaRtCode2,
                    treatment.metastaticRadiotherapy.metaRtCode3,
                    treatment.metastaticRadiotherapy.metaRtCode4,
                ).any { it != null }
    }

    internal fun hasConsistentReasonRefrainingTreatment(tumorRecords: List<NcrRecord>): Boolean {
        val hasConsistentReasonRefrainingTreatment = tumorRecords.map { it.treatment }.all { treatment ->
            when (hasAtLeastOneTreatment(treatment)) {
                true -> treatment.geenTherReden == null
                else -> true
            }
        }
        if (!hasConsistentReasonRefrainingTreatment) log("Inconsistent reason for refraining treatment for tumor ${tumorRecords.tumorId()}")
        return hasConsistentReasonRefrainingTreatment
    }

    internal fun hasValidPrimarySurgeryInt(
        surgeryChir: Int?,
        surgeryStartInt: Int?,
        otherTreatmentCode: Int?,
        otherTreatmentStartInt: Int?
    ): Boolean {
        return when (otherTreatmentCode) {
            1 -> surgeryChir == null || (surgeryStartInt != null && otherTreatmentStartInt != null && otherTreatmentStartInt <= surgeryStartInt)
            2 -> surgeryChir != null && (surgeryStartInt != null && otherTreatmentStartInt != null && otherTreatmentStartInt >= surgeryStartInt)
            else -> true
        }
    }

    internal fun hasValidTherprepostCode(tumorRecords: List<NcrRecord>): Boolean {
        val hasValidTherprepostCode = tumorRecords.map { it.treatment }.all { it ->
            hasValidPrimarySurgeryInt(
                it.primarySurgery.chir,
                it.primarySurgery.chirInt1,
                it.primaryRadiotherapy.rt,
                it.primaryRadiotherapy.rtStartInt1
            ) &&
                    hasValidPrimarySurgeryInt(
                        it.primarySurgery.chir,
                        it.primarySurgery.chirInt1,
                        it.systemicTreatment.chemo,
                        it.systemicTreatment.systStartInt1
                    )
        }
        if (!hasValidTherprepostCode) log("Invalid therapy pre/post codes for tumor ${tumorRecords.tumorId()}")
        return hasValidTherprepostCode
    }

    internal fun isPrimarySurgeryValid(tumorRecords: List<NcrRecord>): Boolean {
        val isPrimarySurgeryValid = tumorRecords.map { it.treatment }.all { treatment ->
            val hasPrimarySurgeryChir = treatment.primarySurgery.chir != null
            val hasPrimarySurgeryType1 = treatment.primarySurgery.chirType1.isNotZeroOrNull()
            val hasPrimarySurgeryType2 = treatment.primarySurgery.chirType2.isNotZeroOrNull()
            hasPrimarySurgeryChir == (hasPrimarySurgeryType1 || hasPrimarySurgeryType2)
        }
        if (!isPrimarySurgeryValid) log("Primary surgery validity check failed for tumor ${tumorRecords.tumorId()}")
        return isPrimarySurgeryValid
    }

    internal fun isPrimaryRadiotherapyValid(tumorRecords: List<NcrRecord>): Boolean {
        val isPrimaryRadiotherapyValid = tumorRecords.map { it.treatment }.all { treatment ->
            val hasPrimaryRadiotherapyRt = treatment.primaryRadiotherapy.rt != 0
            val hasPrimaryRadiotherapyChemort = treatment.primaryRadiotherapy.chemort.isNotZeroOrNull()
            val hasPrimaryRadiotherapyType1 = treatment.primaryRadiotherapy.rtType1.isNotZeroOrNull()
            val hasPrimaryRadiotherapyType2 = treatment.primaryRadiotherapy.rtType2.isNotZeroOrNull()
            (hasPrimaryRadiotherapyRt || hasPrimaryRadiotherapyChemort) == (hasPrimaryRadiotherapyType1 || hasPrimaryRadiotherapyType2)
        }
        if (!isPrimaryRadiotherapyValid) log("Primary radiotherapy validity check failed for tumor ${tumorRecords.tumorId()}")
        return isPrimaryRadiotherapyValid
    }

    internal fun isGastroResectionValid(tumorRecords: List<NcrRecord>): Boolean {
        val isGastroResectionValid = tumorRecords.map { it.treatment }.all { treatment ->
            val hasGastroResection = treatment.gastroenterologyResection.mdlRes.isNotZeroOrNull()
            val hasGastroResectionType1 = treatment.gastroenterologyResection.mdlResType1.isNotZeroOrNull()
            val hasGastroResectionType2 = treatment.gastroenterologyResection.mdlResType2.isNotZeroOrNull()
            hasGastroResection == (hasGastroResectionType1 || hasGastroResectionType2)
        }
        if (!isGastroResectionValid) log("Gastrointestinal resection validity check failed for tumor ${tumorRecords.tumorId()}")
        return isGastroResectionValid
    }

    internal fun isSystemicTreatmentValid(tumorRecords: List<NcrRecord>): Boolean {
        val isSystemicTreatmentValid = tumorRecords.map { it.treatment }.all { treatment ->
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
            hasSystemicChemo == hasSystemicTarget && hasSystemicTarget == hasSystemicCode
        }
        if (!isSystemicTreatmentValid) log("Systemic treatment validation failed: chemo, target, or codes are inconsistent for tumor ${tumorRecords.tumorId()}")
        return isSystemicTreatmentValid
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

    override fun tumorRecords(record: List<NcrRecord>): Boolean {
        return listOf(
            ::hasConsistentReasonRefrainingTreatment,
            ::hasValidTherprepostCode,
            ::isPrimarySurgeryValid,
            ::isPrimaryRadiotherapyValid,
            ::isGastroResectionValid,
            ::isSystemicTreatmentValid,
            ::hasValidSystemCodes
        ).all { it(record) }
    }
}
