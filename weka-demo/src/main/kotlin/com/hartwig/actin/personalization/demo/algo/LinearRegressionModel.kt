package com.hartwig.actin.personalization.demo.algo

import com.hartwig.actin.personalization.demo.datamodel.ReferencePatient
import io.github.oshai.kotlinlogging.KotlinLogging
import weka.classifiers.functions.LinearRegression
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances

object LinearRegressionModel {

    private val LOGGER = KotlinLogging.logger {}

    fun run(patients: List<ReferencePatient>) {
        val ageAttr = Attribute("age")
        val psaAttr = Attribute("psa")
        val ecogAttr = Attribute("ecog")
        val pfsAttr = Attribute("pfs")

        val attributes: ArrayList<Attribute> = ArrayList(listOf(ageAttr, psaAttr, ecogAttr, pfsAttr))

        val patientDb = Instances("patients", attributes, patients.size)
        patientDb.setClassIndex(pfsAttr.index())

        LOGGER.info { " Creating WEKA instances object for ${patients.size} patients" }
        for (patient in patients) {
            val patientInstance = DenseInstance(attributes.size)
            patientInstance.setDataset(patientDb)
            patientInstance.setValue(ageAttr, patient.age.toDouble())
            patientInstance.setValue(psaAttr, patient.psa.toDouble())
            patientInstance.setValue(ecogAttr, patient.ecog.toDouble())
            patientInstance.setClassValue(patient.pfs.toDouble())

            patientDb.add(patientInstance)
        }

        LOGGER.info { " Building linear regression model using to predict ${patientDb.classAttribute().name()}" }
        val linearRegressionModel = LinearRegression()
        linearRegressionModel.buildClassifier(patientDb)

        LOGGER.info { " Classifier output is $linearRegressionModel" }
    }
}