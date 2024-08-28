SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient` (
    `id` int NOT NULL,
    `ncrId` int NOT NULL,
    `sex` varchar(50) NOT NULL,
    `isAlive` bool NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `diagnosis`;
CREATE TABLE `diagnosis` (
    `id` int NOT NULL,
    `patientId` int NOT NULL,
    `consolidatedTumorType` varchar(255) NOT NULL,
    `tumorLocations` json NOT NULL,
    `ageAtDiagnosis` int NOT NULL,
    `intervalTumorIncidenceLatestAliveStatus` int NOT NULL,
    `hasHadPriorTumor` bool NOT NULL,
    `cci` int,
    `cciNumberOfCategories` varchar(50),
    `cciHasAids` bool,
    `cciHasCongestiveHeartFailure` bool,
    `cciHasCollagenosis` bool,
    `cciHasCopd` bool,
    `cciHasCerebrovascularDisease` bool,
    `cciHasDementia` bool,
    `cciHasDiabetesMellitus` bool,
    `cciHasDiabetesMellitusWithEndOrganDamage` bool,
    `cciHasOtherMalignancy` bool,
    `cciHasOtherMetastaticSolidTumor` bool,
    `cciHasMyocardialInfarct` bool,
    `cciHasMildLiverDisease` bool,
    `cciHasHemiplegiaOrParaplegia` bool,
    `cciHasPeripheralVascularDisease` bool,
    `cciHasRenalDisease` bool,
    `cciHasLiverDisease` bool,
    `cciHasUlcerDisease` bool,

    `presentedWithIleus` bool,
    `presentedWithPerforation` bool,
    `anorectalVergeDistanceCategory` varchar(50),

    `hasMsi` bool,
    `hasBrafMutation` bool,
    `hasBrafV600EMutation` bool,
    `hasRasMutation` bool,
    `hasKrasG12CMutation` bool,
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

    `hasDoublePrimaryTumor` bool,
    `mesorectalFasciaIsClear` bool,
    `distanceToMesorectalFascia` int,
    `venousInvasionCategory` varchar(50),
    `lymphaticInvasionCategory` varchar(50),
    `extraMuralInvasionCategory` varchar(50),
    `tumorRegression` varchar(50),

    `hasReceivedTumorDirectedTreatment` bool NOT NULL,
    `reasonRefrainmentFromTumorDirectedTreatment` varchar(255),
    `hasParticipatedInTrial` bool,

    `gastroenterologyResections` json NOT NULL,
    `metastasesSurgeries` json NOT NULL,
    `radiotherapies` json NOT NULL,
    `metastasesRadiotherapies` json NOT NULL,
    `hasHadHipecTreatment` bool NOT NULL,
    `intervalTumorIncidenceHipecTreatment` int,
    `hasHadPreSurgeryRadiotherapy` bool NOT NULL,
    `hasHadPostSurgeryRadiotherapy` bool NOT NULL,
    `hasHadPreSurgeryChemoRadiotherapy` bool NOT NULL,
    `hasHadPostSurgeryChemoRadiotherapy` bool NOT NULL,
    `hasHadPreSurgerySystemicChemotherapy` bool NOT NULL,
    `hasHadPostSurgerySystemicChemotherapy` bool NOT NULL,
    `hasHadPreSurgerySystemicTargetedTherapy` bool NOT NULL,
    `hasHadPostSurgerySystemicTargetedTherapy` bool NOT NULL,

    `response` varchar(50),
    `intervalTumorIncidenceResponseDate` int,
    `systemicTreatmentPlan` varchar(50),
    `intervalTumorIncidenceTreatmentPlanStart` int,
    `intervalTumorIncidenceTreatmentPlanStop` int,
    `intervalTreatmentPlanStartLatestAliveStatus` int,
    `pfs` int,
    `intervalTreatmentPlanStartResponseDate` int,
    `observedPfsDays` int,
    `hadEvent` bool,
    FOREIGN KEY (`diagnosisId`) REFERENCES `diagnosis`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `priorTumor`;
CREATE TABLE `priorTumor` (
    `id` int NOT NULL AUTO_INCREMENT,
    `diagnosisId` int NOT NULL,
    `consolidatedTumorType` varchar(255) NOT NULL,
    `tumorLocations` json NOT NULL,
    `hasHadTumorDirectedSystemicTherapy` bool NOT NULL,
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
    `isPartOfProgression` bool,
    FOREIGN KEY (`episodeId`) REFERENCES `episode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `labMeasurement`;
CREATE TABLE `labMeasurement` (
    `id` int NOT NULL AUTO_INCREMENT,
    `episodeId` int NOT NULL,
    `name` varchar(50) NOT NULL,
    `value` double NOT NULL,
    `unit` varchar(50) NOT NULL,
    `intervalTumorIncidenceLabMeasure` int,
    `isPreSurgical` bool,
    `isPostSurgical` bool,
    FOREIGN KEY (`episodeId`) REFERENCES `episode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `surgery`;
CREATE TABLE `surgery` (
    `id` int NOT NULL AUTO_INCREMENT,
    `episodeId` int NOT NULL,
    `type` varchar(50) NOT NULL,
    `technique` varchar(50),
    `urgency` varchar(255),
    `radicality` varchar(50),
    `circumferentialResectionMargin` varchar(50),
    `anastomoticLeakageAfterSurgery` varchar(50),
    `intervalTumorIncidenceSurgery` int,
    `hospitalizationDuration` int,
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
    FOREIGN KEY (`episodeId`) REFERENCES `episode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `systemicTreatmentSchemeDrug`;
CREATE TABLE `systemicTreatmentSchemeDrug` (
    `id` int NOT NULL AUTO_INCREMENT,
    `systemicTreatmentSchemeId` int NOT NULL,
    `drug` varchar(50) NOT NULL,
    `treatmentSchemeNumber` int,
    `treatmentNumberOfCycles` int,
    `treatmentCyclesDetails` varchar(50),
    `intervalTumorIncidenceTreatmentStart` int,
    `intervalTumorIncidenceTreatmentStop` int,
    `preSurgery` bool,
    `postSurgery` bool,
    FOREIGN KEY (`systemicTreatmentSchemeId`) REFERENCES `systemicTreatmentScheme`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `pfsMeasure`;
CREATE TABLE `pfsMeasure` (
    `id` int NOT NULL AUTO_INCREMENT,
    `episodeId` int NOT NULL,
    `type` varchar(50) NOT NULL,
    `followUpEvent` varchar(50),
    `intervalTumorIncidencePfsMeasure` int,
    FOREIGN KEY (`episodeId`) REFERENCES `episode`(`id`),
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
