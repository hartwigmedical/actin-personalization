package com.hartwig.actin.analysis.recruitment.algo

import com.hartwig.actin.analysis.recruitment.datamodel.PatientRecord
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import weka.classifiers.lazy.IBk
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances

object NearestNeighborModel {

    private val LOGGER: Logger = LogManager.getLogger(NearestNeighborModel::class)

    fun run(patients: List<PatientRecord>) {
        val ageAttr = Attribute("age")
        val ecogAttr = Attribute("ecog")
        val pfsAttr = Attribute("pfs")

        val attributes: ArrayList<Attribute> = ArrayList(listOf(ageAttr, ecogAttr, pfsAttr))

        val patientDb = Instances("patients", attributes, patients.size)
        patientDb.setClassIndex(pfsAttr.index())

        LOGGER.info(" Creating WEKA instances object for ${patients.size} patients")
        for (patient in patients) {
            val patientInstance = DenseInstance(attributes.size)
            patientInstance.setDataset(patientDb)
            patientInstance.setValue(ageAttr, patient.age.toDouble())
            patientInstance.setValue(ecogAttr, patient.ecog.toDouble())
            patientInstance.setClassValue(patient.pfs.toDouble())

            patientDb.add(patientInstance)
        }

        val k = 1
        LOGGER.info(" Building K-nearest neighbour model with K=$k")
        val classifier = IBk(k)
        classifier.buildClassifier(patientDb)

        val newPatient = DenseInstance(attributes.size)
        newPatient.setDataset(patientDb)
        newPatient.setValue(ageAttr, 50.0)
        newPatient.setValue(ecogAttr, 1.0)

        LOGGER.info(" Classifying new patient with age 50 and ECOG 1")
        val expectedPfs = classifier.classifyInstance(newPatient)
        LOGGER.info(" PFS estimated to be $expectedPfs")
    }
}