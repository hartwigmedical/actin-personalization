library(Rmisc)
library(dplyr)
library(tidyr)
library(grid)
library(gridExtra)
library(ggplot2)
library(stringi)
library(gtable)
library(DBI)
library(RMySQL)
library(plotrix)
library(ggrepel)
library(tidyverse)
library(tibble)
library(survival)
library(knitr)
library(ggfortify)

# Set working dir to tmp ------------------------------------------------------------------
 wd <- paste0(Sys.getenv("HOME"), "/hmf/tmp/")
 setwd(wd)

# Retrieve data ------------------------------------------------------------------
dbProd <- dbConnect(MySQL(), dbname='hmfpatients', groups="RAnalysis")

queryCPCT <-"select d.sampleId, d.patientId, gender, birthYear, registrationDate, deathDate, primaryTumorLocation, primaryTumorSubLocation, primaryTumorType, primaryTumorSubType, hasSystemicPreTreatment, hasRadiotherapyPreTreatment, preTreatments, preTreatmentsType, preTreatmentsMechanism, d.treatmentGiven, d.radiotherapyGiven, treatmentStartDate, treatmentEndDate, treatment, consolidatedTreatmentType, concatenatedTreatmentType, consolidatedTreatmentMechanism, concatenatedTreatmentMechanism, responseMeasured, firstResponse, d.responseDate, firstMatchedPDResponse.response as firstResponsePD, firstMatchedPDResponse.responseDate as firstResponsePDDate
from sample s
inner join datarequest d on d.sampleId=s.sampleId
left join biopsy on biopsy.sampleId = s.sampleId
left join treatment on treatment.biopsyId = biopsy.id
left join ( select *
            from
            treatmentResponse as tr
            where response = 'PD' and
            not exists (
              select *
              from treatmentResponse as tr1
              where tr1.treatmentId = tr.treatmentId
              and tr1.responseDate <= tr.responseDate
              and tr1.id != tr.id
              and tr1.response = tr.response)
            and not(isnull(treatmentId))
) as firstMatchedPDResponse
on treatment.id = firstMatchedPDResponse.treatmentId
where d.sampleId like 'CPCT%' order by registrationDate;"

queryCPCTResponse <- "select dr.patientId, t.responseDate, t.response
from treatmentResponse t
inner join patient p on p.id=t.patientId
inner join datarequest dr on dr.patientId=p.patientIdentifier
where dr.patientId like 'CPCT%' and response not in ('ND','NE') and response is not null;"

queryCPCTCrcDrivers <- "select a.sampleId,
if(krasStatus='positive','positive','wildtype') as krasStatus,
if(nrasStatus='positive','positive','wildtype') as nrasStatus,
if(brafV600EStatus='positive','positive','wildtype') as brafV600EStatus,
msStatus
from
(select sampleId from datarequest where sampleId like 'CPCT%' and primaryTumorLocation='Colorectum') as a
left join (select sampleId, 'positive' as krasStatus from driverCatalog where driverLikelihood>0.8 and gene='KRAS' and driver='MUTATION') as b on a.sampleId=b.sampleId
left join (select sampleId, 'positive' as nrasStatus from driverCatalog where driverLikelihood>0.8 and gene='NRAS' and driver='MUTATION') as c on a.sampleId=c.sampleId
left join (select sampleId, 'positive' as brafV600EStatus from somaticVariant where gene='BRAF' and reported and canonicalHgvsProteinImpact='p.Val600Glu') as d on a.sampleId=d.sampleId
left join (select sampleId, msStatus from purity) as e on a.sampleId=e.sampleId;"

cpct <- dbGetQuery(dbProd, queryCPCT)
cpctResponse <- dbGetQuery(dbProd, queryCPCTResponse)
cpctCrcDrivers <- dbGetQuery(dbProd, queryCPCTCrcDrivers)
dbDisconnect(dbProd)

# General data cleanup/exploration ------------------------------------------------------------------
## Age at registration, start date after registration date
cpct <- add_column(cpct, registrationYear = as.integer(format(as.Date(cpct$registrationDate), "%Y")), .before = "registrationDate")
cpct <- add_column(cpct, ageAtRegistration = cpct$registrationYear-cpct$birthYear, .before = "registrationDate")
hist(cpct$ageAtRegistration)
min(cpct$ageAtRegistration)
max(cpct$ageAtRegistration)

cpct <- add_column(cpct, daysStartDateAfterRegistration = round(difftime(cpct$treatmentStartDate, cpct$registrationDate, units="days"),0), .after = "treatmentStartDate")
min(cpct$daysStartDateAfterRegistration, na.rm=T)
table(cpct$daysStartDateAfterRegistration<0)

## Start/end dates, treatment duration, death date
min(cpct$treatmentStartDate, na.rm=T)
max(cpct$treatmentStartDate, na.rm=T)
paste0("Treatment start date missing in ", round(sum(is.na(cpct$treatmentStartDate))/length(cpct$treatmentStartDate)*100,0), "%")

min(cpct$treatmentEndDate, na.rm=T)
cpct$treatmentEndDate[cpct$treatmentEndDate == '1900-01-01'] <- NA
max(cpct$treatmentEndDate, na.rm=T)
paste0("Treatment end date missing in ", round(sum(is.na(cpct$treatmentEndDate))/length(cpct$treatmentEndDate)*100,0), "%")

cpct <- add_column(cpct, treatmentDuration = as.integer(round(difftime(cpct$treatmentEndDate,cpct$treatmentStartDate, units="days")),0), .after = "treatmentEndDate")
min(cpct$treatmentDuration, na.rm=T)
hist(cpct$treatmentDuration, breaks=100)
hist(cpct$treatmentDuration, breaks=500, xlim=c(0,500))

paste0("Death date missing in ", round(sum(is.na(cpct$deathDate))/length(cpct$deathDate)*100,0), "%")
cpct <- add_column(cpct, daysDeathDateAfterEndDate = round(difftime(cpct$deathDate, cpct$treatmentEndDate, units="days"),0), .after = "treatmentDuration")
min(cpct$daysDeathDateAfterEndDate, na.rm=T)

cpct <- add_column(cpct, daysDeathDateAfterStartDate = round(difftime(cpct$deathDate, cpct$treatmentStartDate, units="days"),0), .after = "daysStartDateAfterRegistration")
paste0("Duration start date - death date missing in ", round(sum(is.na(cpct$daysDeathDateAfterStartDate))/length(cpct$daysDeathDateAfterStartDate)*100,0), "%")

cpct <- add_column(cpct, pfs = round(difftime(cpct$firstResponsePDDate, cpct$treatmentStartDate, units="days"),0), .after = "firstResponsePD")
paste0("PFS missing in ", round(sum(is.na(cpct$pfs))/length(cpct$pfs)*100,0), "%")

## Response dates
cpct <- add_column(cpct, daysResponseAfterTreatmentStartDate = round(difftime(cpct$responseDate, cpct$treatmentStartDate, units="days"),0), .after = "responseDate")
cpct <- add_column(cpct, daysDeathDateAfterFirstResponseDate = round(difftime(cpct$deathDate, cpct$responseDate, units="days"),0), .after = "responseDate")
cpct <- add_column(cpct, daysEndDateAfterFirstResponseDate = round(difftime(cpct$treatmentEndDate, cpct$responseDate, units="days"),0), .after = "daysResponseAfterTreatmentStartDate")

min(cpct$daysResponseAfterTreatmentStartDate, na.rm=T)
max(cpct$daysResponseAfterTreatmentStartDate, na.rm=T)
min(cpct$daysDeathDateAfterFirstResponseDate, na.rm=T)
max(cpct$daysDeathDateAfterFirstResponseDate, na.rm=T)
min(cpct$daysEndDateAfterFirstResponseDate, na.rm=T)
max(cpct$daysEndDateAfterFirstResponseDate, na.rm=T)

## Response dates cleanup
end_date_after_response_date_min = -30
cpct <- cpct %>% 
  mutate(responseDate = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, responseDate)) %>% 
  mutate(responseMeasured = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, responseMeasured)) %>%
  mutate(firstResponse = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, firstResponse)) %>%
  mutate(daysResponseAfterTreatmentStartDate = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, daysResponseAfterTreatmentStartDate)) %>%
  mutate(daysDeathDateAfterFirstResponseDate = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, daysDeathDateAfterFirstResponseDate)) %>%
  mutate(daysEndDateAfterFirstResponseDate = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, daysEndDateAfterFirstResponseDate))

## Tumor details
barchart(cpct$primaryTumorLocation)

## Pre-treated details
pie(table(cpct$hasSystemicPreTreatment), main="Has had systemic pretreatment?", col=c("red","blue"), labels=paste0(row.names(table(cpct$hasSystemicPreTreatment)), " (", round(prop.table(table(cpct$hasSystemicPreTreatment))*100,0), "%)", sep = ""))
pie(table(cpct$hasRadiotherapyPreTreatment), main="Has had radiotherapy pretreatment?", col=c("red","blue"), labels=paste0(row.names(table(cpct$hasRadiotherapyPreTreatment)), " (", round(prop.table(table(cpct$hasRadiotherapyPreTreatment))*100,0), "%)", sep = ""))

cpct <- add_column(cpct, hasBeenUntreated = (cpct$hasSystemicPreTreatment == "No" & cpct$hasRadiotherapyPreTreatment == "No"), .after = "hasRadiotherapyPreTreatment")
pie(table(cpct$hasBeenUntreated), main="Has been untreated?", col=c("red","blue"), labels=paste0(row.names(table(cpct$hasBeenUntreated)), " (", round(prop.table(table(cpct$hasBeenUntreated))*100,0), "%)", sep = ""))

## OS survival plots for for untreated patients for gender, tumor location, first response
## Consider censoring
survival <- cpct %>% dplyr::filter(hasBeenUntreated=='TRUE') %>% subset(select = c(daysDeathDateAfterStartDate, gender, primaryTumorLocation, firstResponse))
survival$status <- ifelse(!is.na(survival$daysDeathDateAfterStartDate), 1, 0)
paste0("Nr of uncensored patients ", sum(survival$status == 1))

survGender <- survfit(Surv(survival$daysDeathDateAfterStartDate, survival$status) ~ survival$gender, data = survival)
autoplot(survGender) + 
  labs(title="Overall survival from start treatment (in untreated patients)", y="Proportion", x="Time (days)", color="Gender", fill = "Gender")
survdiff(Surv(survival$daysDeathDateAfterStartDate, survival$status) ~ survival$gender, data = survival)

survivalTumorLocation <- survival %>% dplyr::filter(primaryTumorLocation %in% c('Breast','Colorectum','Lung'))
paste0("Nr of uncensored patients ", sum(survivalTumorLocation$status == 1))
survTumorLocation <- survfit(Surv(survivalTumorLocation$daysDeathDateAfterStartDate, survivalTumorLocation$status) ~ survivalTumorLocation$primaryTumorLocation, data = survivalTumorLocation)
autoplot(survTumorLocation) + 
  labs(title="Overall survival from start treatment (in untreated patients)", y="Proportion", x="Time (days)", color="Tumor location", fill = "Tumor location")
survdiff(Surv(survivalTumorLocation$daysDeathDateAfterStartDate, survivalTumorLocation$status) ~ survivalTumorLocation$primaryTumorLocation, data = survivalTumorLocation)

survivalFirstResponse <- survival %>% dplyr::filter(firstResponse %in% c('PD','PR','SD'))
paste0("Nr of uncensored patients ", sum(survivalFirstResponse$status == 1))
survFirstResponse <- survfit(Surv(survivalFirstResponse$daysDeathDateAfterStartDate, survivalFirstResponse$status) ~ survivalFirstResponse$firstResponse, data = survivalFirstResponse)
autoplot(survFirstResponse) + 
  labs(title="Overall survival from start treatment (in untreated patients)", y="Proportion", x="Time (days)", color="First response", fill = "First response")
survdiff(Surv(survivalFirstResponse$daysDeathDateAfterStartDate, survivalFirstResponse$status) ~ survivalFirstResponse$firstResponse, data = survivalFirstResponse)

## PFS plots for for untreated patients for gender, tumor location, first response
## Consider censoring
survival <- cpct %>% dplyr::filter(hasBeenUntreated=='TRUE') %>% subset(select = c(pfs, gender, primaryTumorLocation, firstResponse))
survival$status <- ifelse(!is.na(survival$pfs), 1, 0)
paste0("Nr of uncensored patients ", sum(survival$status == 1))

survGender <- survfit(Surv(survival$pfs, survival$status) ~ survival$gender, data = survival)
autoplot(survGender) + 
  labs(title="PFS (in untreated patients)", y="Proportion", x="Time (days)", color="Gender", fill = "Gender")
survdiff(Surv(survival$pfs, survival$status) ~ survival$gender, data = survival)

survivalTumorLocation <- survival %>% dplyr::filter(primaryTumorLocation %in% c('Breast','Colorectum','Lung'))
paste0("Nr of uncensored patients ", sum(survivalTumorLocation$status == 1))
survTumorLocation <- survfit(Surv(survivalTumorLocation$pfs, survivalTumorLocation$status) ~ survivalTumorLocation$primaryTumorLocation, data = survivalTumorLocation)
autoplot(survTumorLocation) + 
  labs(title="PFS (in untreated patients)", y="Proportion", x="Time (days)", color="Tumor location", fill = "Tumor location")
survdiff(Surv(survivalTumorLocation$pfs, survivalTumorLocation$status) ~ survivalTumorLocation$primaryTumorLocation, data = survivalTumorLocation)

survivalFirstResponse <- survival %>% dplyr::filter(firstResponse %in% c('PD','PR','SD'))
paste0("Nr of uncensored patients ", sum(survivalFirstResponse$status == 1))
survFirstResponse <- survfit(Surv(survivalFirstResponse$pfs, survivalFirstResponse$status) ~ survivalFirstResponse$firstResponse, data = survivalFirstResponse)
autoplot(survFirstResponse) + 
  labs(title="PFS (in untreated patients)", y="Proportion", x="Time (days)", color="First response", fill = "First response")
survdiff(Surv(survivalFirstResponse$daysDeathDateAfterStartDate, survivalFirstResponse$status) ~ survivalFirstResponse$firstResponse, data = survivalFirstResponse)

# 1.1 CRC exploration---------------------------------------------
## General numbers & tumor types
cpctCrc <- subset(cpct, subset = (primaryTumorLocation == 'Colorectum'))
n_distinct(cpctCrc$sampleId)
n_distinct(cpctCrc$patientId)
qplot(cpctCrc$registrationYear, binwidth=0.5)

qplot(cpctCrc$primaryTumorType)
cpctCrc <- subset(cpctCrc, subset = (primaryTumorType %in% c('','Carcinoma')))
n_distinct(cpctCrc$sampleId)
n_distinct(cpctCrc$patientId)

## Adding CRC driver information
cpctCrc <- inner_join(cpctCrc, cpctCrcDrivers, by=c('sampleId'='sampleId'))

## MSI
cpctCrc %>% group_by(msStatus) %>% summarise(count = n())
qplot(cpctCrc$msStatus)

cpctCrcMsi <- subset(cpctCrc, subset = (msStatus == 'MSI'))
cpctCrcMsi <- add_column(cpctCrcMsi, immunotherapyAsPreTreatment = ifelse(is.na(str_detect(cpctCrcMsi$preTreatmentsType, "Immunotherapy")), FALSE, str_detect(cpctCrcMsi$preTreatmentsType, "Immuno")), .before = "treatmentGiven")
cpctCrcMsi <- add_column(cpctCrcMsi, immunotherapyAsTreatment = ifelse(is.na(str_detect(cpctCrcMsi$concatenatedTreatmentType, "Immunotherapy")), FALSE, str_detect(cpctCrcMsi$concatenatedTreatmentType, "Immuno")), .before = "responseMeasured")

qplot(cpctCrcMsi$immunotherapyAsPreTreatment)
qplot(cpctCrcMsi$immunotherapyAsTreatment)

cpctCrcImmunoInNonMSI <- cpctCrc %>% 
  add_column(cpctCrc, immunotherapyAsTreatment = ifelse(is.na(str_detect(cpctCrc$concatenatedTreatmentType, "Immunotherapy")), FALSE, str_detect(cpctCrc$concatenatedTreatmentType, "Immuno")), .before = "treatmentGiven") %>%
  subset(immunotherapyAsTreatment == 'TRUE') %>%
  subset(msStatus == 'MSS')

## BRAF
cpctCrc %>% group_by(brafV600EStatus) %>% summarise(count = n())
qplot(cpctCrc$brafV600EStatus)

cpctCrcBraf <- subset(cpctCrc, subset = (brafV600EStatus == 'positive'))
cpctCrcBraf <- add_column(cpctCrcBraf, brafTreatmentAsPreTreatment = ifelse(is.na(str_detect(cpctCrcBraf$preTreatmentsMechanism, "BRAF inhibitor")), FALSE, str_detect(cpctCrcBraf$preTreatmentsMechanism, "BRAF inhibitor")), .before = "treatmentGiven")
cpctCrcBraf <- add_column(cpctCrcBraf, brafTreatmentAsTreatment = ifelse(is.na(str_detect(cpctCrcBraf$concatenatedTreatmentMechanism, "BRAF inhibitor")), FALSE, str_detect(cpctCrcBraf$concatenatedTreatmentMechanism, "BRAF inhibitor")), .before = "responseMeasured")
qplot(cpctCrcBraf$brafTreatmentAsPreTreatment)
qplot(cpctCrcBraf$brafTreatmentAsTreatment)

cpctCrcBRAFInNonBRAF <- cpctCrc %>% 
  add_column(brafTreatmentAsTreatment = ifelse(is.na(str_detect(cpctCrc$concatenatedTreatmentMechanism, "BRAF inhibitor")), FALSE, str_detect(cpctCrc$concatenatedTreatmentMechanism, "BRAF inhibitor")), .before = "responseMeasured") %>%
  subset(brafTreatmentAsTreatment == 'TRUE') %>%
  subset(brafV600EStatus == 'wildtype')

## RAS/BRAF WILDTYPE
cpctCrc <- add_column(cpctCrc, rasBrafWildtype = (cpctCrc$krasStatus == "wildtype" & cpctCrc$nrasStatus == "wildtype" & cpctCrc$brafV600EStatus == "wildtype"))

cpctCrc %>% group_by(rasBrafWildtype) %>% summarise(count = n())
qplot(cpctCrc$rasBrafWildtype)

cpctCrcRasRafWildtype <- subset(cpctCrc, subset = (rasBrafWildtype == 'TRUE'))
cpctCrcRasRafWildtype <- add_column(cpctCrcRasRafWildtype, egfrTreatmentAsPreTreatment = ifelse(is.na(str_detect(cpctCrcRasRafWildtype$preTreatmentsMechanism, "EGFR")), FALSE, str_detect(cpctCrcRasRafWildtype$preTreatmentsMechanism, "EGFR")), .before = "treatmentGiven")
cpctCrcRasRafWildtype <- add_column(cpctCrcRasRafWildtype, egfrTreatmentAsTreatment = ifelse(is.na(str_detect(cpctCrcRasRafWildtype$concatenatedTreatmentMechanism, "EGFR")), FALSE, str_detect(cpctCrcRasRafWildtype$concatenatedTreatmentMechanism, "EGFR")), .before = "responseMeasured")
cpctCrcRasRafWildtype <- add_column(cpctCrcRasRafWildtype, cetuximabAsPreTreatment = ifelse(is.na(str_detect(cpctCrcRasRafWildtype$preTreatments, "Cetuximab")), FALSE, str_detect(cpctCrcRasRafWildtype$preTreatments, "Cetuximab")), .before = "treatmentGiven")
cpctCrcRasRafWildtype <- add_column(cpctCrcRasRafWildtype, panitumumabAsPreTreatment = ifelse(is.na(str_detect(cpctCrcRasRafWildtype$preTreatments, "Panitumumab")), FALSE, str_detect(cpctCrcRasRafWildtype$preTreatments, "Panitumumab")), .before = "treatmentGiven")
cpctCrcRasRafWildtype <- add_column(cpctCrcRasRafWildtype, cetuximabAsTreatment = ifelse(is.na(str_detect(cpctCrcRasRafWildtype$treatment, "Cetuximab")), FALSE, str_detect(cpctCrcRasRafWildtype$treatment, "Cetuximab")), .before = "responseMeasured")
cpctCrcRasRafWildtype <- add_column(cpctCrcRasRafWildtype, panitumumabAsTreatment = ifelse(is.na(str_detect(cpctCrcRasRafWildtype$treatment, "Panitumumab")), FALSE, str_detect(cpctCrcRasRafWildtype$treatment, "Panitumumab")), .before = "responseMeasured")

qplot(cpctCrcRasRafWildtype$egfrTreatmentAsPreTreatment)
qplot(cpctCrcRasRafWildtype$egfrTreatmentAsTreatment)

cpctCrcRasRafWildtype %>% group_by(egfrTreatmentAsPreTreatment) %>% summarise(count = n())
cpctCrcRasRafWildtype %>% group_by(egfrTreatmentAsTreatment) %>% summarise(count = n())
cpctCrcRasRafWildtype %>% group_by(cetuximabAsPreTreatment) %>% summarise(count = n())
cpctCrcRasRafWildtype %>% group_by(panitumumabAsPreTreatment) %>% summarise(count = n())
cpctCrcRasRafWildtype %>% group_by(cetuximabAsTreatment) %>% summarise(count = n())
cpctCrcRasRafWildtype %>% group_by(panitumumabAsTreatment) %>% summarise(count = n())

cpctCrcPanitumumabInWT <- subset(cpctCrcRasRafWildtype, subset = (panitumumabAsTreatment == 'TRUE'))
cpctCrcPanitumumabInNonWT <- cpctCrc %>% 
  add_column(panitumumabAsTreatment = ifelse(is.na(str_detect(cpctCrc$treatment, "Panitumumab")), FALSE, str_detect(cpctCrc$treatment, "Panitumumab")), .before = "responseMeasured") %>%
  subset(panitumumabAsTreatment == 'TRUE') %>%
  subset(rasBrafWildtype == 'FALSE')

cpctCrcCetuximabInWT <- subset(cpctCrcRasRafWildtype, subset = (cetuximabAsTreatment == 'TRUE'))
cpctCrcCetuximabInNonWT <- cpctCrc %>% 
  add_column(cetuximabAsTreatment = ifelse(is.na(str_detect(cpctCrc$treatment, "Cetuximab")), FALSE, str_detect(cpctCrc$treatment, "Cetuximab")), .before = "responseMeasured") %>%
  subset(cetuximabAsTreatment == 'TRUE') %>%
  subset(rasBrafWildtype == 'FALSE')

# 1.2 CRC survival plots 
## CRC OS survival plots from treatment start (in treated and untreated patients)
## censoring must be taken into account
survivalCrc <- cpctCrc %>% subset(select = c(daysDeathDateAfterStartDate, gender, hasSystemicPreTreatment, hasRadiotherapyPreTreatment, hasBeenUntreated, firstResponse, msStatus))
survivalCrc$status <- ifelse(!is.na(survivalCrc$daysDeathDateAfterStartDate), 1, 0)

survSystemicTreatment <- survfit(Surv(survivalCrc$daysDeathDateAfterStartDate, survivalCrc$status) ~ survivalCrc$hasSystemicPreTreatment, data = survivalCrc)
autoplot(survSystemicTreatment) + 
  labs(title="Overall survival from start treatment", y="Proportion", x="Time (days)", color="Has had systemic treatment?", fill = "Has had systemic treatment?")

survRadioTreatment <- survfit(Surv(survivalCrc$daysDeathDateAfterStartDate, survivalCrc$status) ~ survivalCrc$hasRadiotherapyPreTreatment, data = survivalCrc)
autoplot(survRadioTreatment) + 
  labs(title="Overall survival from start treatment", y="Proportion", x="Time (days)", color="Has had radio treatment?", fill = "Has had radio treatment?")

survUntreated <- survfit(Surv(survivalCrc$daysDeathDateAfterStartDate, survivalCrc$status) ~ survivalCrc$hasBeenUntreated, data = survivalCrc)
autoplot(survUntreated) + 
  labs(title="Overall survival from start treatment", y="Proportion", x="Time (days)", color="Has been untreated?", fill = "Has been untreated?")

survMSI <- survfit(Surv(survivalCrc$daysDeathDateAfterStartDate, survivalCrc$status) ~ survivalCrc$msStatus, data = survivalCrc)
autoplot(survMSI) + 
  labs(title="Overall survival from start treatment", y="Proportion", x="Time (days)", color="MS status", fill = "MS status")

## CRC OS survival plots from treatment start (only include untreated patients)
survivalCrcUntreated <- survivalCrc %>% dplyr::filter(hasBeenUntreated == 'TRUE')

survivalCrcUntreatedResponse <- survivalCrcUntreated %>% dplyr::filter(firstResponse %in% c('PD','PR','CR','SD'))
survFirstResponse <- survfit(Surv(survivalCrcUntreatedResponse$daysDeathDateAfterStartDate, survivalCrcUntreatedResponse$status) ~ survivalCrcUntreatedResponse$firstResponse, data = survivalCrcUntreatedResponse)
autoplot(survFirstResponse) + 
  labs(title="Overall survival from start treatment (untreated patients)", y="Proportion", x="Time (days)", color="First response", fill = "First response")
paste0("Nr of uncensored patients ", sum(survivalCrcUntreatedResponse$status == 1))

## CRC PFS plots for gender, tumor location, first response
## Consider censoring
survivalCrc <- cpctCrc %>% 
  subset(select = c(pfs, gender, hasSystemicPreTreatment, hasRadiotherapyPreTreatment, hasBeenUntreated, firstResponse, msStatus))
survivalCrc$status <- ifelse(!is.na(survivalCrc$pfs), 1, 0)
paste0("PFS missing in ", round(sum(is.na(survivalCrc$pfs))/length(survivalCrc$pfs)*100,0), "%, or available in ", sum(!is.na(survivalCrc$pfs)), " patients")

survSystemicTreatment <- survfit(Surv(survivalCrc$pfs, survivalCrc$status) ~ survivalCrc$hasSystemicPreTreatment, data = survivalCrc)
autoplot(survSystemicTreatment) + 
  labs(title="PFS", y="Proportion", x="Time (days)", color="Has had systemic treatment?", fill = "Has had systemic treatment?")

survUntreated <- survfit(Surv(survivalCrc$pfs, survivalCrc$status) ~ survivalCrc$hasBeenUntreated, data = survivalCrc)
autoplot(survUntreated) + 
  labs(title="PFS", y="Proportion", x="Time (days)", color="Has been untreated?", fill = "Has been untreated?")

survMSI <- survfit(Surv(survivalCrc$pfs, survivalCrc$status) ~ survivalCrc$msStatus, data = survivalCrc)
autoplot(survMSI) + 
  labs(title="PFS", y="Proportion", x="Time (days)", color="MS status", fill = "MS status")

# 2. All Investigate relationship between age at start treatment & treatment duration ------------------------------------------------------------------
categoriesTherapy = c("Immunotherapy", "Hormonal therapy", "Chemotherapy", "Targeted therapy")
categoriesTumor = c("Lung", "Breast", "Prostate", "Skin")

corValueTherapy <- data.frame()
corValueTumor <- data.frame()

i <- 1
par(mfrow = c(2, 2))
for (x in categoriesTherapy) {
  corValueTherapy[1,i] <- cor(cpct[cpct$consolidatedTreatmentType == x,]$treatmentDuration, cpct[cpct$consolidatedTreatmentType == x,]$ageAtStartTreatment, use='complete.obs')
  plot(treatmentDuration ~ ageAtStartTreatment, data=subset(cpct, consolidatedTreatmentType == x), main = paste0(x, " cc = ", round(corValueTherapy[i],4)))
  i <- i+1
}

colnames(corValueTherapy) <- categoriesTherapy

i <- 1
par(mfrow = c(2, 2))
for (x in categoriesTumor) {
  corValueTumor[1,i] <- cor(cpct[cpct$primaryTumorLocation == x,]$treatmentDuration, cpct[cpct$primaryTumorLocation == x,]$ageAtStartTreatment, use='complete.obs')
  plot(treatmentDuration ~ ageAtStartTreatment, data=subset(cpct, primaryTumorLocation == x), main = paste0(x, " cc = ", round(corValueTumor[i],4)))
  i <- i+1
}

colnames(corValueTumor) <- categoriesTumor

# 3. All Investigate relationship between age at start treatment & treatment response ------------------------------------------------------------------
## 3.1 Matrix of response based on bucketed age at start treatment

bucket_20_40 <- c(20, 40)
bucket_40_50 <- c(40, 50)
bucket_50_60 <- c(50, 60)
bucket_60_70 <- c(60, 70)
bucket_70_80 <- c(70, 80)
bucket_80_100 <- c(80, 100)
ageBuckets <- list(bucket_20_40, bucket_40_50, bucket_50_60, bucket_60_70, bucket_70_80, bucket_80_100)

responseDetailsPerAgeBucket <- data.frame()
i<-1

for (x in ageBuckets) {
  responseDetailsPerAgeBucket[1, i] <- nrow(cpct[cpct$ageAtStartTreatment <= x[2] & cpct$ageAtStartTreatment >= x[1] & !is.na(cpct$ageAtStartTreatment) & !is.na(cpct$firstResponse) & cpct$firstResponse != "ND" & cpct$firstResponse != "Non-CR/Non-PD" & cpct$firstResponse != "Clinical progression", ])
  responseDetailsPerAgeBucket[2, i] <- nrow(cpct[cpct$ageAtStartTreatment <= x[2] & cpct$ageAtStartTreatment >= x[1] & !is.na(cpct$ageAtStartTreatment) & !is.na(cpct$firstResponse) & cpct$firstResponse == "PD", ])
  responseDetailsPerAgeBucket[3, i] <- responseDetailsPerAgeBucket[2,i]/responseDetailsPerAgeBucket[1,i]
  responseDetailsPerAgeBucket[4, i] <- nrow(cpct[cpct$ageAtStartTreatment <= x[2] & cpct$ageAtStartTreatment >= x[1] & !is.na(cpct$ageAtStartTreatment) & !is.na(cpct$firstResponse) & (cpct$firstResponse == "PR" | cpct$firstResponse =="CR"), ])
  responseDetailsPerAgeBucket[5, i] <- responseDetailsPerAgeBucket[4,i]/responseDetailsPerAgeBucket[1,i]
  responseDetailsPerAgeBucket[6, i] <- nrow(cpct[cpct$ageAtStartTreatment <= x[2] & cpct$ageAtStartTreatment >= x[1] & !is.na(cpct$ageAtStartTreatment) & !is.na(cpct$firstResponse) & cpct$firstResponse == "SD", ])
  responseDetailsPerAgeBucket[7, i] <- responseDetailsPerAgeBucket[6,i]/responseDetailsPerAgeBucket[1,i]
  i <- i+1
}

colnames(responseDetailsPerAgeBucket) <- c("20-40y", "40-50y", "50-60y", "60-70y", "70-80y", "80-100y")
rownames(responseDetailsPerAgeBucket) <- c("n","n with PD first response","fraction PD first response","n with PR/CR first response","fraction PR/CR first response", "n with SD first response","fraction SD first response")


## 3.2. Plot progressive-disease fraction per age at start treatment

adultAge = c(18:100)

progressiveDiseaseFractionPerAge <- data.frame()
i<-1

for (x in adultAge) {
  tmp <- cpct[cpct$ageAtStartTreatment == x & !is.na(cpct$ageAtStartTreatment) & !is.na(cpct$firstResponse) & cpct$firstResponse != "ND" & cpct$firstResponse != "Non-CR/Non-PD", ]
  tmp2 <- nrow(tmp[(tmp$firstResponse == "PD" | tmp$firstResponse == "Clinical progression"), ])

  if (is.null(tmp2) == TRUE) {
    tmp2 <- 0e-15
  }

  progressiveDiseaseFractionPerAge[i,1] <- x
  progressiveDiseaseFractionPerAge[i,2] <- tmp2
  progressiveDiseaseFractionPerAge[i,3] <- nrow(tmp)
  progressiveDiseaseFractionPerAge[i,4] <- tmp2/nrow(tmp)

  i <- i+1
}

colnames(progressiveDiseaseFractionPerAge) <- c("age", "n non-response", "n evaluable", "progressive disease fraction")
rownames(progressiveDiseaseFractionPerAge) <- adultAge

# drop NA values and ages with n_evaluable<10
progressiveDiseaseFractionPerAgeClean <- subset(progressiveDiseaseFractionPerAge, !is.na(progressiveDiseaseFractionPerAge$`progressive disease fraction`) & progressiveDiseaseFractionPerAge$`n evaluable` > 10)
plot(progressiveDiseaseFractionPerAgeClean$age, progressiveDiseaseFractionPerAgeClean$`progressive disease fraction`, xlab="age", ylab="progressive disease fraction")

