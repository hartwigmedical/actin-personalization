package com.hartwig.actin.personalization.similarity

import com.hartwig.actin.personalization.datamodel.TumorEntry
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.Utils

private val missingValue = Utils.missingValue()

fun createPatientDb(patients: List<TumorEntry>, fields: List<Field>): Instances {
    val attributes = ArrayList(fields.map(Field::toAttribute))
    val patientDb = Instances("patients", attributes, patients.count())
    patientDb.setClassIndex(attributes.last().index())

    patients.forEach { patient ->
        val values = fields.map { it.getFor(patient) ?: missingValue }
        val patientInstance = DenseInstance(1.0, values.toDoubleArray())
        patientInstance.setDataset(patientDb)
        patientDb.add(patientInstance)
    }
    return patientDb
}