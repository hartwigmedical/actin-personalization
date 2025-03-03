SELECT
    patient.id as patientId,
    patient.sex,
    survivalMeasure.isAlive,
    -- diagnosis.consolidatedTumorType,
    -- diagnosis.tumorLocations,
	primaryDiagnosis.sidedness,
    tumor.ageAtDiagnosis,
    survivalMeasure.daysSinceDiagnosis as observedOsFromTumorIncidenceDays,
    survivalMeasure.isAlive as hadSurvivalEvent,
    priorTumorOverview.priorTumorCount > 0 as hasHadPriorTumor,
    -- diagnosis.isMetachronous, -- based on episode.order that does not exist in the new datamodel
    comorbidityAssessment.charlsonComorbidityIndex as cci,
    -- diagnosis.cciNumberOfCategories,
    comorbidityAssessment.hasAids,
	comorbidityAssessment.hasCongestiveHeartFailure,
    comorbidityAssessment.hasCollagenosis,
    comorbidityAssessment.hasCopd,
    comorbidityAssessment.hasCerebrovascularDisease,
    comorbidityAssessment.hasDementia,
    comorbidityAssessment.hasDiabetesMellitus,
    comorbidityAssessment.hasDiabetesMellitusWithEndOrganDamage,
    comorbidityAssessment.hasOtherMalignancy,
    comorbidityAssessment.hasOtherMetastaticSolidTumor,
    comorbidityAssessment.hasMyocardialInfarct,
    comorbidityAssessment.hasMildLiverDisease,
    comorbidityAssessment.hasHemiplegiaOrParaplegia,
    comorbidityAssessment.hasPeripheralVascularDisease,
    comorbidityAssessment.hasRenalDisease,
    comorbidityAssessment.hasLiverDisease,
    comorbidityAssessment.hasUlcerDisease,
    
	primaryDiagnosis.presentedWithIleus,
    primaryDiagnosis.presentedWithPerforation,
    primaryDiagnosis.anorectalVergeDistanceCategory,
    
    molecularResult.hasMsi,
    molecularResult.hasBrafMutation,
    molecularResult.hasBrafV600EMutation,
    molecularResult.hasRasMutation,
    molecularResult.hasKrasG12CMutation,

	tumor.id as tumorId,
    tumor.id as episodeId,
    -- episode.*, episodes is replaced with tumor, survivalMeasure, primaryDiagnosis, metastaticDiagnosis --- still need to match with original names
    tumor.*, -- see what else is need from episodes 
    survivalMeasure.*,
    primaryDiagnosis.*,
    metastaticDiagnosis.*,
    surgeryOverview.surgeries
    -- metastasisOverview.metastasisLocationGroupsPriorToSystemicTreatment,
    -- episode.intervalTumorIncidenceTreatmentPlanStopDays - episode.intervalTumorIncidenceTreatmentPlanStartDays AS systemicTreatmentPlanDuration
FROM patient
    INNER JOIN tumor on patient.id = tumor.patientId
    INNER JOIN survivalMeasure on tumor.id = survivalMeasure.tumorId
	INNER JOIN primaryDiagnosis on tumor.id = primaryDiagnosis.tumorId
    INNER JOIN metastaticDiagnosis on tumor.id = metastaticDiagnosis.tumorId
	INNER JOIN comorbidityAssessment on tumor.id = comorbidityAssessment.tumorId
    INNER JOIN molecularResult on tumor.id = molecularResult.tumorId
    LEFT JOIN (select tumorId, count(*) as priorTumorCount FROM priorTumor GROUP BY tumorId) as priorTumorOverview on tumor.id = priorTumorOverview.tumorId
    LEFT JOIN (SELECT tumorId, GROUP_CONCAT(type) AS surgeries FROM primarySurgery GROUP BY tumorId) surgeryOverview on  tumor.id = surgeryOverview.tumorId