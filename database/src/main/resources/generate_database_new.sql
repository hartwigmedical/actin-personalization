SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient` (
    `id` int NOT NULL,
    `sex` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `survivalMeasure`;
CREATE TABLE `survivalMeasure` (
    `id` int NOT NULL AUTO_INCREMENT,
    `daysSinceDiagnosis` int NOT NULL,
    `isAlive` bool NOT NULL
)

DROP TABLE IF EXISTS `tumor`;
CREATE TABLE `tumor` (
    `id` int NOT NULL,
    `patientId` int NOT NULL,
    `diagnosisYear` int NOT NULL,
    `ageAtDiagnosis` int NOT NULL,
    latestSurvivalStatus int NOT NULL

        -- [AE] TODO at primaryDiagnosis

    FOREIGN KEY (`patientId`) REFERENCES `patient`(`id`),
    CONSTRAINT fk_lss FOREIGN KEY (latestSurvivalStatus) REFERENCES survivalMeasure(id) ON DELETE RESTRICT,
    CONSTRAINT unique_lss UNIQUE (latestSurvivalStatus)
    PRIMARY KEY (`id`)
)

DROP TABLE IF EXISTS `priorTumor`;
CREATE TABLE `priorTumor` (
    `id` int NOT NULL AUTO_INCREMENT,
    `tumorId` int NOT NULL,
    `daysBeforeDiagnosis` int,
    `primaryTumorType` varchar(255) NOT NULL,
    `primaryTumorLocation` varchar(255) NOT NULL,
    `primaryTumorLocationCategory` varchar(50) NOT NULL,
    `primaryTumorStage` varchar(10) NOT NULL,
    `systemicDrugsReceived` json NOT NULL,
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `whoAssessment`;
CREATE TABLE `whoAssessment` (
    `id` int NOT NULL AUTO_INCREMENT,
    `tumorId` int NOT NULL,
    `daysSinceDiagnosis` int NOT NULL,
    `whoStatus` int NOT NULL
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
)

DROP TABLE IF EXISTS `asaAssessment`;
CREATE TABLE `asaAssessment` (
    `id` int NOT NULL AUTO_INCREMENT,
    `tumorId` int NOT NULL,
    `daysSinceDiagnosis` int NOT NULL,
    `asaClassification` varchar(10) NOT NULL
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
)


DROP TABLE IF EXISTS `tnmClassification`;
CREATE TABLE `tnmClassification` (
    `id` int NOT NULL AUTO_INCREMENT,
    `tumor` varchar(10),
    `lymphNodes` varchar(10),
    `metastasis` varchar(10)
)

DROP TABLE IF EXISTS `primaryDiagnosis`;
CREATE TABLE `primaryDiagnosis` (
    `id` int NOT NULL AUTO_INCREMENT,
    `tumorId` int NOT NULL,
    `tumorBasisOfDiagnosis` varchar(255) NOT NULL,
    `hasDoublePrimaryTumor` bool,
    `primaryTumorType` varchar(255) NOT NULL,
    `primaryTumorLocation` varchar(255) NOT NULL,
    `differentiationGrade` varchar(255),
    `clinicalTnmClassification` int NOT NULL,
    `pathologicalTnmClassification` int NOT NULL,

    -- [AE] TODO complete

    CONSTRAINT fk_ctnmc FOREIGN KEY (`clinicalTnmClassification`) REFERENCES tnmClassification(`id`) ON DELETE RESTRICT,
    CONSTRAINT fk_ptnmc FOREIGN KEY (`pathologicalTnmClassification`) REFERENCES tnmClassification(`id`) ON DELETE RESTRICT,
    CONSTRAINT unique_ctnmc UNIQUE (`clinicalTnmClassification`),
    CONSTRAINT unique_ptnmc UNIQUE (`pathologicalTnmClassification`),
    FOREIGN KEY (`patientId`) REFERENCES `patient`(`id`),
    FOREIGN KEY (`tumorId`) REFERENCES `tumor`(`id`),
    PRIMARY KEY (`id`)
)




SET FOREIGN_KEY_CHECKS = 1;
