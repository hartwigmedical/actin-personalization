SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient` (
    `id` INT NOT NULL,
    `sex` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `tumor`;
CREATE TABLE `tumor` (
    `id` INT NOT NULL,
    `patientId` INT NOT NULL,
    `diagnosisYear` INT NOT NULL,
    `ageAtDiagnosis` INT NOT NULL,
    FOREIGN KEY (`patientId`) REFERENCES `patient`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `survivalMeasure`;
CREATE TABLE `survivalMeasure` (
    `tumorId` INT NOT NULL,
    `daysSinceDiagnosis` INT NOT NULL,
    `isAlive` BOOL NOT NULL,
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`tumorId`)
);

DROP TABLE IF EXISTS `priorTumor`;
CREATE TABLE `priorTumor` (
    `id` INT NOT NULL,
    `tumorId` INT NOT NULL,
    `daysBeforeDiagnosis` INT NOT NULL,
    `primaryTumorType` VARCHAR(255) NOT NULL,
    `primaryTumorLocation` VARCHAR(255) NOT NULL,
    `primaryTumorLocationCategory` VARCHAR(50) NOT NULL,
    `primaryTumorStage` VARCHAR(10),
    `systemicDrugsReceived` JSON NOT NULL,
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`, `tumorId`)
);

DROP TABLE IF EXISTS `primaryDiagnosis`;
CREATE TABLE `primaryDiagnosis` (
    `id` INT NOT NULL,
    `tumorId` INT NOT NULL,
    `basisOfDiagnosis` VARCHAR(255) NOT NULL,
    `hasDoublePrimaryTumor` BOOL NOT NULL,
    `primaryTumorType` VARCHAR(255) NOT NULL,
    `primaryTumorLocation` VARCHAR(255) NOT NULL,
    `sidedness` VARCHAR(50),
    `anorectalVergeDistanceCategory` VARCHAR(50),
    `mesorectalFasciaIsClear` BOOL,
    `distanceToMesorectalFasciaMm` INT,
    `differentiationGrade` VARCHAR(255),
    `clinicalTnmClassification` JSON NOT NULL,
    `pathologicalTnmClassification` JSON NOT NULL,
    `clinicalTumorStage` VARCHAR(50) NOT NULL,
    `pathologicalTumorStage` VARCHAR(50) NOT NULL,
    `investigatedLymphNodesCount` INT,
    `positiveLymphNodesCount` INT,
    `presentedWithIleus` BOOL,
    `presentedWithPerforation` BOOL,
    `venousInvasionDescription` VARCHAR(50),
    `lymphaticInvasionCategory` VARCHAR(50),
    `extraMuralInvasionCategory` VARCHAR(50),
    `tumorRegression` VARCHAR(50),
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`, `tumorId`)
);

DROP TABLE IF EXISTS `metastaticDiagnosis`;
CREATE TABLE `metastaticDiagnosis` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `tumorId` INT NOT NULL,
    `distantMetastasesDetectionStatus` VARCHAR(50) NOT NULL,
    `numberOfLiverMetastases` VARCHAR(50),
    `maximumSizeOfLiverMetastasisMm` INT,
    `investigatedLymphNodesCount` INT,
    `positiveLymphNodesCount` INT,
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `metastasis`;
CREATE TABLE `metastasis` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `metastaticDiagnosisId` INT NOT NULL,
    `daysSinceDiagnosis` INT,
    `location` VARCHAR(255) NOT NULL,
    `isLinkedToProgression` BOOL,
    FOREIGN KEY (`metastaticDiagnosisId`) REFERENCES `metastaticDiagnosis`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `whoAssessment`;
CREATE TABLE `whoAssessment` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `tumorId` INT NOT NULL,
    `daysSinceDiagnosis` INT NOT NULL,
    `whoStatus` INT NOT NULL,
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `asaAssessment`;
CREATE TABLE `asaAssessment` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `tumorId` INT NOT NULL,
    `daysSinceDiagnosis` INT NOT NULL,
    `asaClassification` VARCHAR(10) NOT NULL,
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `comorbidityAssessment`;
CREATE TABLE `comorbidityAssessment` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `tumorId` INT NOT NULL,
    `charlsonComorbidityIndex` INT NOT NULL,
    `daysSinceDiagnosis` INT NOT NULL,
    `hasAids` BOOL NOT NULL,
    `hasCongestiveHeartFailure` BOOL NOT NULL,
    `hasCollagenosis` BOOL NOT NULL,
    `hasCopd` BOOL NOT NULL,
    `hasCerebrovascularDisease` BOOL NOT NULL,
    `hasDementia` BOOL NOT NULL,
    `hasDiabetesMellitus` BOOL NOT NULL,
    `hasDiabetesMellitusWithEndOrganDamage` BOOL NOT NULL,
    `hasOtherMalignancy` BOOL NOT NULL,
    `hasOtherMetastaticSolidTumor` BOOL NOT NULL,
    `hasMyocardialInfarct` BOOL NOT NULL,
    `hasMildLiverDisease` BOOL NOT NULL,
    `hasHemiplegiaOrParaplegia` BOOL NOT NULL,
    `hasPeripheralVascularDisease` BOOL NOT NULL,
    `hasRenalDisease` BOOL NOT NULL,
    `hasLiverDisease` BOOL NOT NULL,
    `hasUlcerDisease` BOOL NOT NULL,
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `molecularResult`;
CREATE TABLE `molecularResult` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `tumorId` INT NOT NULL,
    `daysSinceDiagnosis` INT NOT NULL,
    `hasMsi` BOOL,
    `hasBrafMutation` BOOL,
    `hasBrafV600EMutation` BOOL,
    `hasRasMutation` BOOL,
    `hasKrasG12CMutation` BOOL,
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `labMeasurement`;
CREATE TABLE `labMeasurement` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `tumorId` INT NOT NULL,
    `daysSinceDiagnosis` INT,
    `name` VARCHAR(50) NOT NULL,
    `value` DOUBLE NOT NULL,
    `unit` VARCHAR(50) NOT NULL,
    `isPreSurgical` BOOL,
    `isPostSurgical` BOOL,
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `treatmentEpisode`;
CREATE TABLE `treatmentEpisode` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `tumorId` INT NOT NULL,
    `metastaticPresence` VARCHAR(50) NOT NULL,
    `reasonRefrainmentFromTreatment` VARCHAR(255) NOT NULL,
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `gastroenterologyResection`;
CREATE TABLE `gastroenterologyResection` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `treatmentEpisodeId` INT NOT NULL,
    `daysSinceDiagnosis` INT,
    `resectionType` VARCHAR(50) NOT NULL,
    FOREIGN KEY (`treatmentEpisodeId`) REFERENCES `treatmentEpisode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `primarySurgery`;
CREATE TABLE `primarySurgery` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `treatmentEpisodeId` INT NOT NULL,
    `daysSinceDiagnosis` INT,
    `type` varchar(50) NOT NULL,
    `technique` varchar(50),
    `urgency` varchar(255),
    `radicality` varchar(50),
    `circumferentialResectionMargin` varchar(50),
    `anastomoticLeakageAfterSurgery` varchar(50),
    `hospitalizationDurationDays` int,
    FOREIGN KEY (`treatmentEpisodeId`) REFERENCES `treatmentEpisode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `metastaticSurgery`;
CREATE TABLE `metastaticSurgery` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `treatmentEpisodeId` INT NOT NULL,
    `daysSinceDiagnosis` INT,
    `type` varchar(100) NOT NULL,
    `radicality` varchar(50),
    FOREIGN KEY (`treatmentEpisodeId`) REFERENCES `treatmentEpisode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `hipecTreatment`;
CREATE TABLE `hipecTreatment` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `treatmentEpisodeId` INT NOT NULL,
    `daysSinceDiagnosis` INT NOT NULL,
    FOREIGN KEY (`treatmentEpisodeId`) REFERENCES `treatmentEpisode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `primaryRadiotherapy`;
CREATE TABLE `primaryRadiotherapy` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `treatmentEpisodeId` INT NOT NULL,
    `daysBetweenDiagnosisAndStart` INT,
    `daysBetweenDiagnosisAndStop` INT,
    `type` VARCHAR(50) NOT NULL,
    `totalDosage` DOUBLE,
    FOREIGN KEY (`treatmentEpisodeId`) REFERENCES `treatmentEpisode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `metastaticRadiotherapy`;
CREATE TABLE `metastaticRadiotherapy` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `treatmentEpisodeId` INT NOT NULL,
    `daysBetweenDiagnosisAndStart` INT,
    `daysBetweenDiagnosisAndStop` INT,
    `type` VARCHAR(100) NOT NULL,
    FOREIGN KEY (`treatmentEpisodeId`) REFERENCES `treatmentEpisode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `systemicTreatment`;
CREATE TABLE `systemicTreatment` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `treatmentEpisodeId` INT NOT NULL,
    `daysBetweenDiagnosisAndStart` INT,
    `daysBetweenDiagnosisAndStop` INT,
    `treatment` VARCHAR(50) NOT NULL,
    FOREIGN KEY (`treatmentEpisodeId`) REFERENCES `treatmentEpisode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `systemicTreatmentScheme`;
CREATE TABLE `systemicTreatmentScheme` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `systemicTreatmentId` INT NOT NULL,
    `minDaysBetweenDiagnosisAndStart` INT,
    `maxDaysBetweenDiagnosisAndStart` INT,
    `minDaysBetweenDiagnosisAndStop` INT,
    `maxDaysBetweenDiagnosisAndStop` INT,
    FOREIGN KEY (`systemicTreatmentId`) REFERENCES `systemicTreatment`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `systemicTreatmentDrug`;
CREATE TABLE `systemicTreatmentDrug` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `systemicTreatmentSchemeId` INT NOT NULL,
    `daysBetweenDiagnosisAndStart` INT,
    `daysBetweenDiagnosisAndStop` INT,
    `drug` VARCHAR(50) NOT NULL,
    `numberOfCycles` INT,
    `intent` VARCHAR(50),
    `drugTreatmentIsOngoing` BOOL,
    `isAdministeredPreSurgery` BOOL,
    `isAdministeredPostSurgery` BOOL,
    FOREIGN KEY (`systemicTreatmentSchemeId`) REFERENCES `systemicTreatmentScheme`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `responseMeasure`;
CREATE TABLE `responseMeasure` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `treatmentEpisodeId` INT NOT NULL,
    `daysSinceDiagnosis` INT,
    `response` VARCHAR(10) NOT NULL,
    FOREIGN KEY (`treatmentEpisodeId`) REFERENCES `treatmentEpisode`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `progressionMeasure`;
CREATE TABLE `progressionMeasure` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `treatmentEpisodeId` INT NOT NULL,
    `daysSinceDiagnosis` INT,
    `type` VARCHAR(50) NOT NULL,
    `followUpEvent` VARCHAR(50),
    FOREIGN KEY (`treatmentEpisodeId`) REFERENCES `treatmentEpisode`(`id`),
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
