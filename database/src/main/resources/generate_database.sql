SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient` (
    `id` int NOT NULL,
    `ncrId` int NOT NULL,
    `sex` varchar(50) NOT NULL,
    `isAlive` tinyint(1),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `diagnosis`;
CREATE TABLE `diagnosis` (
    `id` int NOT NULL,
    `patientId` int NOT NULL,
    `consolidatedTumorType` varchar(255) NOT NULL,
    `tumorLocations` json NOT NULL,
    `hasHadPriorTumor` tinyint(1) NOT NULL,
    `cci` int,
    `cciNumberOfCategories` varchar(50),
    `cciHasAids` tinyint(1),
    `cciHasCongestiveHeartFailure` tinyint(1),
    `cciHasCollagenosis` tinyint(1),
    `cciHasCopd` tinyint(1),
    `cciHasCerebrovascularDisease` tinyint(1),
    `cciHasDementia` tinyint(1),
    `cciHasDiabetesMellitus` tinyint(1),
    `cciHasDiabetesMellitusWithEndOrganDamage` tinyint(1),
    `cciHasOtherMalignancy` tinyint(1),
    `cciHasOtherMetastaticSolidTumor` tinyint(1),
    `cciHasMyocardialInfarct` tinyint(1),
    `cciHasMildLiverDisease` tinyint(1),
    `cciHasHemiplegiaOrParaplegia` tinyint(1),
    `cciHasPeripheralVascularDisease` tinyint(1),
    `cciHasRenalDisease` tinyint(1),
    `cciHasLiverDisease` tinyint(1),
    `cciHasUlcerDisease` tinyint(1),

    `presentedWithIleus` tinyint(1),
    `presentedWithPerforation` tinyint(1),
    `anorectalVergeDistanceCategory` varchar(50),

    `hasMsi` tinyint(1),
    `hasBrafMutation` tinyint(1),
    `hasBrafV600EMutation` tinyint(1),
    `hasRasMutation` tinyint(1),
    `hasKrasG12CMutation` tinyint(1),
    FOREIGN KEY (`patientId`) REFERENCES `patient`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `episode`;
CREATE TABLE `episode` (
    `id` int NOT NULL,
    `diagnosisId` int NOT NULL,
    `order` int NOT NULL,
    `whoStatusPreTreatmentStart` int,
    `asaClassificationPreSurgeryOrEndoscopy` varchar(50),

    `tumorIncidenceYear` int NOT NULL,
    `tumorBasisOfDiagnosis` varchar(255) NOT NULL,
    `tumorLocation` varchar(255) NOT NULL,
    `tumorDifferentiationGrade` varchar(255),
    `tnmCT` varchar(50),
    `tnmCN` varchar(50),
    `tnmCM` varchar(50),
    `tnmPT` varchar(50),
    `tnmPN` varchar(50),
    `tnmPM` varchar(50),
    `stageCTNM` varchar(50),
    `stagePTNM` varchar(50),
    `stageTNM` varchar(50),
    `numberOfInvestigatedLymphNodes` int,
    `numberOfPositiveLymphNodes` int,

    `distantMetastasesStatus` varchar(50) NOT NULL,
    `numberOfLiverMetastases` varchar(50),
    `maximumSizeOfLiverMetastasisInMm` int,

    `hasDoublePrimaryTumor` tinyint(1),
    `mesorectalFasciaIsClear` tinyint(1),
    `distanceToMesorectalFascia` int,
    `venousInvasionCategory` varchar(50),
    `lymphaticInvasionCategory` varchar(50),
    `extraMuralInvasionCategory` varchar(50),
    `tumorRegression` varchar(50),

    `hasReceivedTumorDirectedTreatment` tinyint(1) NOT NULL,
    `reasonRefrainmentFromTumorDirectedTreatment` varchar(255),
    `hasParticipatedInTrial` tinyint(1),

    `gastroenterologyResections` json NOT NULL,
    `metastasesSurgeries` json NOT NULL,
    `radiotherapies` json NOT NULL,
    `metastasesRadiotherapies` json NOT NULL,
    `hasHadHipecTreatment` tinyint(1) NOT NULL,
    `intervalTumorIncidenceHipecTreatment` int,
    `hasHadPreSurgeryRadiotherapy` tinyint(1) NOT NULL,
    `hasHadPostSurgeryRadiotherapy` tinyint(1) NOT NULL,
    `hasHadPreSurgeryChemoRadiotherapy` tinyint(1) NOT NULL,
    `hasHadPostSurgeryChemoRadiotherapy` tinyint(1) NOT NULL,
    `hasHadPreSurgerySystemicChemotherapy` tinyint(1) NOT NULL,
    `hasHadPostSurgerySystemicChemotherapy` tinyint(1) NOT NULL,
    `hasHadPreSurgerySystemicTargetedTherapy` tinyint(1) NOT NULL,
    `hasHadPostSurgerySystemicTargetedTherapy` tinyint(1) NOT NULL,

    `response` varchar(50),
    `intervalTumorIncidenceResponseDate` int,
    `systemicTreatmentPlan` varchar(50),
    `intervalTumorIncidenceTreatmentPlanStart` int,
    `intervalTumorIncidenceTreatmentPlanStop` int,
    `pfs` int,
    `intervalTreatmentPlanStartResponseDate` int,
    FOREIGN KEY (`diagnosisId`) REFERENCES `diagnosis`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `priorTumor`;
CREATE TABLE `priorTumor` (
    `id` int NOT NULL AUTO_INCREMENT,
    `diagnosisId` int NOT NULL,
    `consolidatedTumorType` varchar(255) NOT NULL,
    `tumorLocations` json NOT NULL,
    `hasHadTumorDirectedSystemicTherapy` tinyint(1) NOT NULL,
    `incidenceIntervalPrimaryTumor` int,
    `tumorPriorId` int NOT NULL,
    `tumorLocationCategory` varchar(50) NOT NULL,
    `stageTNM` varchar(50),
    `systemicTreatments` json NOT NULL,
    FOREIGN KEY (`diagnosisId`) REFERENCES `diagnosis`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `metastasis`;
CREATE TABLE `metastasis` (
    `id` int NOT NULL AUTO_INCREMENT,
    `episodeId` int NOT NULL,
    `location` varchar(255) NOT NULL,
    `locationGroup` varchar(50) NOT NULL,
    `intervalTumorIncidenceMetastasisDetection` int,
    `progression` tinyint(1),
    FOREIGN KEY (`episodeId`) REFERENCES `episode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `labMeasurement`;
CREATE TABLE `labMeasurement` (
    `id` int NOT NULL AUTO_INCREMENT,
    `episodeId` int NOT NULL,
    `labMeasure` varchar(50) NOT NULL,
    `labMeasureValue` double NOT NULL,
    `labMeasureUnit` varchar(50) NOT NULL,
    `intervalTumorIncidenceLabMeasureValue` int,
    `isPreSurgical` tinyint(1),
    `isPostSurgical` tinyint(1),
    FOREIGN KEY (`episodeId`) REFERENCES `episode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `surgery`;
CREATE TABLE `surgery` (
    `id` int NOT NULL AUTO_INCREMENT,
    `episodeId` int NOT NULL,
    `surgeryType` varchar(50) NOT NULL,
    `surgeryTechnique` varchar(50),
    `surgeryUrgency` varchar(255),
    `surgeryRadicality` varchar(50),
    `circumferentialResectionMargin` varchar(50),
    `anastomoticLeakageAfterSurgery` varchar(50),
    `intervalTumorIncidenceSurgery` int,
    `durationOfHospitalization` int,
    FOREIGN KEY (`episodeId`) REFERENCES `episode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `systemicTreatmentScheme`;
CREATE TABLE `systemicTreatmentScheme` (
    `id` int NOT NULL,
    `episodeId` int NOT NULL,
    `intervalTumorIncidenceTreatmentLineStartMin` int,
    `intervalTumorIncidenceTreatmentLineStartMax` int,
    `intervalTumorIncidenceTreatmentLineStopMin` int,
    `intervalTumorIncidenceTreatmentLineStopMax` int,

    `intervalTreatmentStartMinResponseDate` int,
    `intervalTreatmentStartMaxResponseDate` int,
    FOREIGN KEY (`episodeId`) REFERENCES `episode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `systemicTreatmentComponent`;
CREATE TABLE `systemicTreatmentComponent` (
    `id` int NOT NULL AUTO_INCREMENT,
    `systemicTreatmentSchemeId` int NOT NULL,
    `drug` varchar(50) NOT NULL,
    `treatmentSchemeNumber` int,
    `treatmentNumberOfCycles` int,
    `treatmentCyclesDetails` varchar(50),
    `intervalTumorIncidenceTreatmentStart` int,
    `intervalTumorIncidenceTreatmentStop` int,
    `preSurgery` tinyint(1),
    `postSurgery` tinyint(1),
    FOREIGN KEY (`systemicTreatmentSchemeId`) REFERENCES `systemicTreatmentScheme`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `pfsMeasure`;
CREATE TABLE `pfsMeasure` (
    `id` int NOT NULL AUTO_INCREMENT,
    `systemicTreatmentSchemeId` int NOT NULL,
    `pfsMeasureType` varchar(50) NOT NULL,
    `pfsMeasureFollowupEvent` varchar(50),
    `intervalTumorIncidencePfsMeasureDate` int,
    FOREIGN KEY (`systemicTreatmentSchemeId`) REFERENCES `systemicTreatmentScheme`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `drug`;
CREATE TABLE `drug` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `treatmentCategory` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `group` varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
);

SET FOREIGN_KEY_CHECKS = 1;
