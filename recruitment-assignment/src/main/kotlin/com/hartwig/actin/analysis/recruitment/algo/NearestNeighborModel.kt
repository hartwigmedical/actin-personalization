package com.hartwig.actin.analysis.recruitment.algo

import com.hartwig.actin.analysis.recruitment.datamodel.PatientRecord
import weka.core.Attribute
import weka.core.Instances

object NearestNeighborModel {

    fun run(patients : List<PatientRecord>) {
        val attributes : ArrayList<Attribute> = ArrayList()

        attributes.add(Attribute("age"))
        attributes.add(Attribute("ecog"))
        attributes.add(Attribute("metastatic sites", listOf("x", "y")))

        val instances = Instances("patients", attributes, 10)
    }
}