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
library(class)
library(tidymodels) 
library(ggsurvfit)
#library(gtsummary)

# Set working dir to tmp ------------------------------------------------------------------
 wd <- paste0(Sys.getenv("HOME"), "/hmf/tmp/")
 setwd(wd)

# Get access to functions ---------------------------------------------------------------
source(paste0(Sys.getenv("HOME"), "/hmf/repos/actin-analysis/scripts/cpct_data_analysis_functions.R"))
source(paste0(Sys.getenv("HOME"), "/hmf/repos/actin-analysis/scripts/cpct_treatment_curation.R"))
 
# Retrieve data ------------------------------------------------------------------
dbProd <- dbConnect(MySQL(), dbname='hmfpatients', groups="RAnalysis")

queryCPCT <-"select d.sampleId, biopsy.patientId, gender, birthYear, registrationDate, deathDate, primaryTumorLocation, primaryTumorSubLocation, primaryTumorType, primaryTumorSubType, hasSystemicPreTreatment, hasRadiotherapyPreTreatment, preTreatments, preTreatmentsType, preTreatmentsMechanism, d.treatmentGiven, d.radiotherapyGiven, treatmentStartDate, treatmentEndDate, treatment, consolidatedTreatmentType, concatenatedTreatmentType, consolidatedTreatmentMechanism, concatenatedTreatmentMechanism, d.firstResponse as firstResponseOrig, d.responseDate as firstResponseDateOrig, firstMatchedResponse.response as firstResponse, firstMatchedResponse.responseDate as firstResponseDate, firstMatchedPDResponse.response as firstResponsePD, firstMatchedPDResponse.responseDate as firstResponsePDDate, bestResponse
from sample s
inner join datarequest d on d.sampleId=s.sampleId
left join biopsy on biopsy.sampleId = s.sampleId
left join treatment on treatment.biopsyId = biopsy.id
left join (select * 
          from
          treatmentResponse as tr
          where measurementDone='Yes' and response not in ('ND','NE') and
          not exists ( 
            select * 
            from treatmentResponse as tr1
            where tr1.measurementDone='Yes' and tr1.response not in ('ND','NE') 
            and tr1.treatmentId = tr.treatmentId
		        and tr1.responseDate <= tr.responseDate
            and tr1.id != tr.id
        )
        and not(isnull(treatmentId))
) as firstMatchedResponse
on treatment.id = firstMatchedResponse.treatmentId
left join ( select *
            from
            treatmentResponse as tr
            where response = 'PD' and measurementDone= 'Yes' and
            not exists (
              select *
              from treatmentResponse as tr1
              where tr1.treatmentId = tr.treatmentId
              and tr1.responseDate <= tr.responseDate
              and tr1.id != tr.id
              and tr1.response = tr.response
          )
          and not(isnull(treatmentId))
) as firstMatchedPDResponse
on treatment.id = firstMatchedPDResponse.treatmentId
left join (select treatmentId, case when (group_concat(response) like '%CR%') then 'CR' when (group_concat(response) like '%PR%') then 'PR' when (group_concat(response) like '%SD%') then 'SD' when (group_concat(response) like '%PD%') then 'PD' else group_concat(response) end as bestResponse
            from
            treatmentResponse as tr
            where measurementDone= 'Yes' and response not in ('Non-CR/Non-PD', 'Clinical progression', 'ND', 'NE') and
            not exists (
              select *
              from treatmentResponse as tr1
              where tr1.treatmentId = tr.treatmentId
              and tr1.responseDate <= tr.responseDate
              and tr1.id != tr.id
              and tr1.response = tr.response
            )
            and not(isnull(treatmentId)) 
            group by 1
) as bestResponse
on treatment.id = bestResponse.treatmentId
where d.sampleId like 'CPCT%' order by registrationDate;"

queryCPCTDrivers <- "select a.sampleId,
if(krasStatus='positive', 0,1) as isKrasWildtype,
if(nrasStatus='positive', 0,1) as isNrasWildtype,
if(b2mStatus='positive', 0, 1) as isB2MWildtype,
if(erbb2AmpStatus='positive', 1,0) as hasErbb2Amp,
if(msStatus='MSI',1,0) as hasMsi,
tml as tumorMutationalLoad,
if(driverCount is null, 0, driverCount) + if(fusionCount is null, 0, fusionCount) as totalDriverCount
from
(select sampleId from datarequest where sampleId like 'CPCT%') as a
left join (select sampleId, 'positive' as krasStatus from driverCatalog where driverLikelihood>0.8 and gene='KRAS') as b1 on a.sampleId=b1.sampleId
left join (select sampleId, 'positive' as nrasStatus from driverCatalog where driverLikelihood>0.8 and gene='NRAS' and driver='MUTATION') as c on a.sampleId=c.sampleId
left join (select sampleId, 'positive' as b2mStatus from driverCatalog where driverLikelihood>0.8 and gene='B2M') as d on a.sampleId=d.sampleId
left join (select sampleId, msStatus, tml from purity) as f on a.sampleId=f.sampleId
left join (select sampleId, 'positive' as erbb2AmpStatus from driverCatalog where gene='ERBB2' and likelihoodMethod='AMP') as g on a.sampleId=g.sampleId
left join (select sampleId, count(*) as driverCount from driverCatalog where driverLikelihood>0.8 group by 1) as h on a.sampleId=h.sampleId
left join (select sampleId, count(*) as fusionCount from svFusion where reported group by 1) as i on a.sampleId=i.sampleId
;"

queryKrasG12 <- "select distinct sampleId, 1 as krasG12Status from somaticVariant where reported and gene='KRAS' and canonicalHgvsProteinImpact like 'p.Gly12%';"
queryKrasG13 <- "select distinct sampleId, 1 as krasG13Status from somaticVariant where reported and gene='KRAS' and canonicalHgvsProteinImpact like 'p.Gly13%';"
queryKrasNonG12G13 <- "select distinct sampleId, 1 as krasNonG12G13Status from somaticVariant where reported and gene='KRAS' and canonicalHgvsProteinImpact not like 'p.Gly12%' and canonicalHgvsProteinImpact not like 'p.Gly13%';"
queryBRAFV600E <- "select distinct sampleId, 1 as brafV600EStatus from somaticVariant where gene='BRAF' and reported and canonicalHgvsProteinImpact='p.Val600Glu';"
queryCPCTDr <- "select sampleId from datarequest where sampleId like 'CPCT%';"

cpctOrig <- dbGetQuery(dbProd, queryCPCT)
cpctDrivers <- dbGetQuery(dbProd, queryCPCTDrivers)
krasG12 <- dbGetQuery(dbProd, queryKrasG12)
krasG13 <- dbGetQuery(dbProd, queryKrasG13)
krasNonG12G13 <- dbGetQuery(dbProd, queryKrasNonG12G13)
brafV600E <- dbGetQuery(dbProd, queryBRAFV600E)
cpctDr <- dbGetQuery(dbProd, queryCPCTDr)

dbDisconnect(dbProd)

# 1. Generate general testing data frames ------------------------------------------------------------------
cpctVariants <- cpctDr %>%
  left_join(krasG12, by=c('sampleId'='sampleId')) %>%
  left_join(krasG13, by=c('sampleId'='sampleId')) %>%
  left_join(krasNonG12G13, by=c('sampleId'='sampleId')) %>%
  left_join(brafV600E, by=c('sampleId'='sampleId'))

cpct <- cpctOrig %>% 
  inner_join(cpctDrivers, by=c('sampleId'='sampleId')) %>%
  inner_join(cpctVariants, by=c('sampleId'='sampleId')) 

cpct <- add_column(cpct, isFemale = as.logical(ifelse(cpct$gender == "female", 1,0), .after = "gender"))
cpct <- add_column(cpct, treatmentStartYear = as.integer(format(as.Date(cpct$treatmentStartDate), "%Y")), .before = "treatmentStartDate")
cpct <- add_column(cpct, ageAtTreatmentStart = as.integer(cpct$treatmentStartYear-cpct$birthYear, .before = "treatmentStartYear"))
cpct <- add_column(cpct, hasImmunoPreTreatment = as.logical(ifelse(grepl("Immunotherapy", cpct$preTreatmentsType), 1, 0), .after = "preTreatmentsType"))
cpct <- add_column(cpct, hasKrasG12Mut = as.logical(ifelse(is.na(cpct$krasG12Status), 0, 1), .after = "krasG12Status"))
cpct <- add_column(cpct, hasKrasG13Mut = as.logical(ifelse(is.na(cpct$krasG13Status), 0, 1), .after = "krasG13Status"))
cpct <- add_column(cpct, hasKrasNonG12G13Mut = as.logical(ifelse(is.na(cpct$krasNonG12G13Status), 0, 1), .after = "hasKrasG13Mut"))
cpct <- add_column(cpct, isBRAFV600EWildtype = as.logical(ifelse(is.na(cpct$brafV600EStatus), 0, 1), .after = "brafV600EStatus"))
cpct <- add_column(cpct, os = as.numeric(round(difftime(cpct$deathDate, cpct$treatmentStartDate, units="days"),0), .after = "deathDate"))
cpct <- add_column(cpct, pfs = as.numeric(round(difftime(cpct$firstResponsePDDate, cpct$treatmentStartDate, units="days"),0), .after = "firstResponsePD"))

cpct$hasSystemicPreTreatment <- as.logical(ifelse(cpct$hasSystemicPreTreatment == "Yes", 1, 0))
cpct$hasMsi <- as.logical(cpct$hasMsi)
cpct$isKrasWildtype <- as.logical(cpct$isKrasWildtype)
cpct$isNrasWildtype <- as.logical(cpct$isNrasWildtype)
cpct$isB2MWildtype <- as.logical(cpct$isB2MWildtype)
cpct$hasErbb2Amp <- as.logical(cpct$hasErbb2Amp)

## Pembrolizumab df
pembrolizumab <- cpct %>% 
  dplyr::filter(treatment == 'Pembrolizumab' | cpct$treatment == 'Pembrolizumab/Pembrolizumab') %>%
  subset(select = c(sampleId, patientId, isFemale, ageAtTreatmentStart, treatment, hasSystemicPreTreatment, hasImmunoPreTreatment, primaryTumorLocation, isKrasWildtype, isB2MWildtype, hasMsi, tumorMutationalLoad, bestResponse, pfs, os)) %>%
  dplyr::filter(primaryTumorLocation %in% c('Urothelial tract','Lung','Skin','Prostate','Mesothelium'))

## Colorectal df
colorectal <- cpct %>% 
  dplyr::filter(primaryTumorLocation == 'Colorectum') %>%
  cpct_colorectal_treatment_curation() %>%
  subset(select = c(sampleId, patientId, isFemale, ageAtTreatmentStart, hasSystemicPreTreatment, primaryTumorLocation, isKrasWildtype, hasKrasG12Mut, hasKrasG13Mut, hasKrasNonG12G13Mut, isNrasWildtype, isBRAFV600EWildtype, hasErbb2Amp, hasMsi, tumorMutationalLoad, totalDriverCount, treatment, treatmentCurated, bestResponse, pfs, os))

# 1.1 ANALYSIS - Trifluridine/Tipiracil in CRC ---------------------------------------------------------------
colorectalTri <- colorectal %>% 
  dplyr::filter(treatment == 'Trifluridine')

## 1.1.0 Additional cleaning (none)
## 1.1.1 Generate OS/PFS plots based on KRAS status
colorectalTri <- colorectalTri %>%
  add_column(krasG12vsNonG12 = ifelse((colorectalTri$hasKrasG12Mut == TRUE), "hasKrasG12", "krasG12Wildtype")) %>%
  add_column(krasG12G13vsNonG12G13 = ifelse((colorectalTri$hasKrasG12Mut == TRUE | colorectalTri$hasKrasG13Mut == TRUE), "hasKrasG12G13", "krasG12G13Wildtype"))

colorectalTri$statusOs <- ifelse(!is.na(colorectalTri$os), 1, 0)
colorectalTri$statusPfs <- ifelse(!is.na(colorectalTri$pfs), 1, 0)

paste0("Nr of uncensored (included) patients for OS: ", sum(colorectalTri$statusOs == 1))
paste0("Nr of uncensored (included) patients for PFS: ", sum(colorectalTri$statusPfs == 1))

event_at_time_os = 365.25 #Year
event_at_time_pfs = event_at_time_os/4 #3months

output <- generate_survival_plot(data_set=colorectalTri, survival_var=colorectalTri$os, censor_status_var=colorectalTri$statusOs, split_var = colorectalTri$krasG12vsNonG12, type = "OS", event_at_time=event_at_time_os)
osKrasG12vsNonG12Fit <- output[[1]]
osKrasG12vsNonG12FitSig <- output[[2]]
osKrasG12vsNonG12FitPlot <- output[[3]]
osKrasG12vsNonG12FitTimeEvent <- output[[4]]

output <- generate_survival_plot(data_set=colorectalTri, survival_var=colorectalTri$os, censor_status_var=colorectalTri$statusOs, split_var = colorectalTri$krasG12G13vsNonG12G13, type = "OS", event_at_time=event_at_time_os)
osKrasG12G13vsNonG12G13Fit <- output[[1]]
osKrasG12G13vsNonG12G13FitSig <- output[[2]]
osKrasG12G13vsNonG12G13FitPlot <- output[[3]]
osKrasG12G13vsNonG12G13FitTimeEvent <- output[[4]]

output <- generate_survival_plot(data_set=colorectalTri, survival_var=colorectalTri$pfs, censor_status_var=colorectalTri$statusPfs, split_var = colorectalTri$krasG12vsNonG12, type = "PFS", event_at_time=event_at_time_pfs)
pfsKrasG12vsNonG12Fit <- output[[1]]
pfsKrasG12vsNonG12FitSig <- output[[2]]
pfsKrasG12vsNonG12FitPlot <- output[[3]]
pfsKrasG12vsNonG12FitTimeEvent <- output[[4]]

output <- generate_survival_plot(data_set=colorectalTri, survival_var=colorectalTri$pfs, censor_status_var=colorectalTri$statusPfs, split_var = colorectalTri$krasG12G13vsNonG12G13, type = "PFS", event_at_time=event_at_time_pfs)
pfsKrasG12G13vsNonG12G13Fit <- output[[1]]
pfsKrasG12G13vsNonG12G13FitSig <- output[[2]]
pfsKrasG12G13vsNonG12G13FitPlot <- output[[3]]
pfsKrasG12G13vsNonG12G13FitTimeEvent <- output[[4]]

coxph(Surv(colorectalTri$os, colorectalTri$statusOs) ~ colorectalTri$krasG12G13vsNonG12G13, data=colorectalTri) 
#%>% tbl_regression(exp = TRUE) 

osKrasG12vsNonG12FitPlot
osKrasG12G13vsNonG12G13FitPlot
pfsKrasG12vsNonG12FitPlot
pfsKrasG12G13vsNonG12G13FitPlot

## 1.1.2 Fit KNN model for OS and PFS using various variables
## Removal of instances with missing data (NaN), subset data
colorectalTriKnn_os <- colorectalTri %>% subset(select = c(ageAtTreatmentStart, hasKrasG12Mut, hasKrasG13Mut, hasKrasNonG12G13Mut, tumorMutationalLoad, os)) %>% na.omit()
colorectalTriKnn_pfs <- colorectalTri %>% subset(select = c(ageAtTreatmentStart, hasKrasG12Mut, hasKrasG13Mut, hasKrasNonG12G13Mut, tumorMutationalLoad, pfs)) %>% na.omit()

outcomes <- c("os","pfs")
predictor_vectors <- data.frame(multi = c("ageAtTreatmentStart", "hasKrasG12Mut", "hasKrasG13Mut", "hasKrasNonG12G13Mut", "tumorMutationalLoad"), 
                                age = c("ageAtTreatmentStart", NA, NA, NA, NA),
                                krasG12 = c("hasKrasG12Mut", NA, NA, NA, NA))

for (i in 1:length(outcomes)) {
  for (j in 1:length(predictor_vectors)) {
  
set.seed(15039)
split <- initial_split(get(paste0("colorectalTriKnn_",outcomes[i])), prop=0.8)
train <- training(split)
test <- testing(split)
set.seed(NULL)

predictor_vars=c(unname(unlist(drop_na(predictor_vectors[j]))))
output <- knn_cross_validation(training_set=train, outcome_var=c(outcomes[i]), predictor_vars=predictor_vars, vfold=5, kmax=12)
recipe <- output[[1]]
results <- output[[2]]
assign(paste0("colorectalTriKnn_cross_recipe_",outcomes[i],"_",names(predictor_vectors[j])), output[[1]])
assign(paste0("colorectalTriKnn_cross_results_",outcomes[i],"_",names(predictor_vectors[j])), output[[2]])

optimal_k <- knn_cross_validation_optimal_k(knn_cross_results=results)
assign(paste0("colorectalTriKnn_cross_results_optimal_k_",outcomes[i],"_",names(predictor_vectors[j])), optimal_k)

output <- knn_run_on_test_set(training_set=train, test_set=test, k=optimal_k, recipe=recipe, truth_var=c(outcomes[i]))
assign(paste0("colorectalTriKnn_results_fit_",outcomes[i],"_",names(predictor_vectors[j])), output[[1]])
assign(paste0("colorectalTriKnn_results_summary_",outcomes[i],"_",names(predictor_vectors[j])), output[[2]])

  }
}

# 1.2 ANALYSIS - CAPOX/CAPOX-B in CRC untreated patients ---------------------------------------------------------------
colorectalCapox <- colorectal %>% 
  dplyr::filter(treatmentCurated == 'CAPOX' | treatmentCurated == 'CAPOX-B') %>%
  dplyr::filter(hasSystemicPreTreatment == 'FALSE')

colorectalCapox <- colorectalCapox %>%
  add_column(krasG12vsNonG12 = ifelse((colorectalCapox$hasKrasG12Mut == TRUE), "hasKrasG12", "krasG12Wildtype")) %>%
  add_column(krasG12G13vsNonG12G13 = ifelse((colorectalCapox$hasKrasG12Mut == TRUE | colorectalCapox$hasKrasG13Mut == TRUE), "hasKrasG12G13", "krasG12G13Wildtype"))

# 1.2.0 Cleaning (TODO) ---------------------------------------------------------------

# 1.2.1 Survival analyses -------------------------------------------------------------
colorectalCapox$statusOs <- ifelse(!is.na(colorectalCapox$os), 1, 0)
colorectalCapox$statusPfs <- ifelse(!is.na(colorectalCapox$pfs), 1, 0)

paste0("Nr of uncensored (included) patients for OS: ", sum(colorectalCapox$statusOs == 1))
paste0("Nr of uncensored (included) patients for PFS: ", sum(colorectalCapox$statusPfs == 1))

event_at_time_os = 365.25 #Year
event_at_time_pfs = event_at_time_os/4 #3months

split_vars=c("krasG12vsNonG12", "treatmentCurated", "isFemale")

for (i in 1:length(split_vars)) {
  output <- generate_survival_plot(data_set=colorectalCapox, survival_var=colorectalCapox$os, censor_status_var=colorectalCapox$statusOs, split_var = unlist(select(colorectalCapox, split_vars[i])), type = "OS", event_at_time=event_at_time_os)
  assign(paste0("os_capox_fit_",split_vars[i]), output[[1]])
  assign(paste0("os_capox_sig_",split_vars[i]), output[[2]])
  assign(paste0("os_capox_plot_",split_vars[i]), output[[3]])
  assign(paste0("os_capox_timeEvent_",split_vars[i]), output[[4]])
}

for (i in 1:length(split_vars)) {
  output <- generate_survival_plot(data_set=colorectalCapox, survival_var=colorectalCapox$pfs, censor_status_var=colorectalCapox$statusPfs, split_var = unlist(select(colorectalCapox, split_vars[i])), type = "PFS", event_at_time=event_at_time_pfs)
  assign(paste0("pfs_capox_fit_",split_vars[i]), output[[1]])
  assign(paste0("pfs_capox_sig_",split_vars[i]), output[[2]])
  assign(paste0("pfs_capox_plot_",split_vars[i]), output[[3]])
  assign(paste0("pfs_capox_timeEvent_",split_vars[i]), output[[4]])
}

plot(os_capox_plot_isFemale)
plot(os_capox_plot_krasG12vsNonG12)
plot(os_capox_plot_treatmentCurated)
plot(pfs_capox_plot_isFemale)
plot(pfs_capox_plot_krasG12vsNonG12)
plot(pfs_capox_plot_treatmentCurated)

# 1.3 ANALYSIS - CRC untreated patients ---------------------------------------------------------------




# 2 General data exploration ------------------------------------------------------------------
## Age at registration, start date after registration date
cpct <- add_column(cpct, registrationYear = as.integer(format(as.Date(cpct$registrationDate), "%Y")), .before = "registrationDate")
cpct <- add_column(cpct, ageAtRegistration = cpct$registrationYear-cpct$birthYear, .before = "registrationDate")
hist(cpct$ageAtRegistration)
min(cpct$ageAtRegistration)
max(cpct$ageAtRegistration)
min(cpct$registrationDate)
max(cpct$registrationDate)

cpct <- add_column(cpct, daysStartDateAfterRegistration = round(difftime(cpct$treatmentStartDate, cpct$registrationDate, units="days"),0), .after = "treatmentStartDate")
min(cpct$daysStartDateAfterRegistration, na.rm=T)
table(cpct$daysStartDateAfterRegistration<0)

## Tumor details
barchart(cpct$primaryTumorLocation)

## Pre-treated details
pie(table(cpct$hasSystemicPreTreatment), main="Has had systemic pretreatment?", col=c("red","blue"), labels=paste0(row.names(table(cpct$hasSystemicPreTreatment)), " (", round(prop.table(table(cpct$hasSystemicPreTreatment))*100,0), "%)", sep = ""))

cpct$hasRadiotherapyPreTreatment <- as.logical(ifelse(cpct$hasRadiotherapyPreTreatment == "Yes", 1, 0))
pie(table(cpct$hasRadiotherapyPreTreatment), main="Has had radiotherapy pretreatment?", col=c("red","blue"), labels=paste0(row.names(table(cpct$hasRadiotherapyPreTreatment)), " (", round(prop.table(table(cpct$hasRadiotherapyPreTreatment))*100,0), "%)", sep = ""))

cpct <- add_column(cpct, hasBeenUntreated = (cpct$hasSystemicPreTreatment == "FALSE" & cpct$hasRadiotherapyPreTreatment == "FALSE"), .after = "hasRadiotherapyPreTreatment")
pie(table(cpct$hasBeenUntreated), main="Has been untreated?", col=c("red","blue"), labels=paste0(row.names(table(cpct$hasBeenUntreated)), " (", round(prop.table(table(cpct$hasBeenUntreated))*100,0), "%)", sep = ""))

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

cpct <- add_column(cpct, os = round(difftime(cpct$deathDate, cpct$treatmentStartDate, units="days"),0), .after = "daysStartDateAfterRegistration")
paste0("Duration start date - death date missing in ", round(sum(is.na(cpct$os))/length(cpct$os)*100,0), "%")

cpct <- add_column(cpct, pfs = round(difftime(cpct$firstResponsePDDate, cpct$treatmentStartDate, units="days"),0), .after = "firstResponsePD")
paste0("PFS missing in ", round(sum(is.na(cpct$pfs))/length(cpct$pfs)*100,0), "%")

## Response dates
cpct <- add_column(cpct, daysResponseAfterTreatmentStartDate = round(difftime(cpct$firstResponseDate, cpct$treatmentStartDate, units="days"),0), .after = "firstResponseDate")
cpct <- add_column(cpct, daysDeathDateAfterFirstResponseDate = round(difftime(cpct$deathDate, cpct$firstResponseDate, units="days"),0), .after = "firstResponseDate")
cpct <- add_column(cpct, daysEndDateAfterFirstResponseDate = round(difftime(cpct$treatmentEndDate, cpct$firstResponseDate, units="days"),0), .after = "daysResponseAfterTreatmentStartDate")

min(cpct$daysResponseAfterTreatmentStartDate, na.rm=T)
max(cpct$daysResponseAfterTreatmentStartDate, na.rm=T)
min(cpct$daysDeathDateAfterFirstResponseDate, na.rm=T)
max(cpct$daysDeathDateAfterFirstResponseDate, na.rm=T)
min(cpct$daysEndDateAfterFirstResponseDate, na.rm=T)
max(cpct$daysEndDateAfterFirstResponseDate, na.rm=T)

## Response dates cleanup in case difference with treatment end date too large
end_date_after_response_date_min <- -30
cpct <- cpct %>% 
  mutate(firstResponseDate = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, firstResponseDate)) %>% 
  mutate(firstResponse = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, firstResponse)) %>%
  mutate(daysResponseAfterTreatmentStartDate = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, daysResponseAfterTreatmentStartDate)) %>%
  mutate(daysDeathDateAfterFirstResponseDate = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, daysDeathDateAfterFirstResponseDate)) %>%
  mutate(daysEndDateAfterFirstResponseDate = ifelse(daysEndDateAfterFirstResponseDate < end_date_after_response_date_min, NA, daysEndDateAfterFirstResponseDate))

# 2.1 CRC exploration---------------------------------------------
## General numbers & tumor types
cpctCrc <- subset(cpct, subset = (primaryTumorLocation == 'Colorectum'))
n_distinct(cpctCrc$sampleId)
n_distinct(cpctCrc$patientId)
qplot(cpctCrc$registrationYear, binwidth=0.5)

qplot(cpctCrc$primaryTumorType)
cpctCrc <- subset(cpctCrc, subset = (primaryTumorType %in% c('','Carcinoma')))
n_distinct(cpctCrc$sampleId)
n_distinct(cpctCrc$patientId)

table(cpctCrc$hasBeenUntreated)

## Adding CRC driver information
cpctCrc <- inner_join(cpctCrc, cpctCrcDrivers, by=c('sampleId'='sampleId'))

## MSI
cpctCrc %>% group_by(msStatus) %>% summarise(count = n())
qplot(cpctCrc$msStatus)

cpctCrcMsi <- subset(cpctCrc, subset = (msStatus == 'MSI'))
cpctCrcMsi <- add_column(cpctCrcMsi, immunotherapyAsPreTreatment = ifelse(is.na(str_detect(cpctCrcMsi$preTreatmentsType, "Immunotherapy")), FALSE, str_detect(cpctCrcMsi$preTreatmentsType, "Immuno")), .before = "treatmentGiven")
cpctCrcMsi <- add_column(cpctCrcMsi, immunotherapyAsTreatment = ifelse(is.na(str_detect(cpctCrcMsi$concatenatedTreatmentType, "Immunotherapy")), FALSE, str_detect(cpctCrcMsi$concatenatedTreatmentType, "Immuno")), .before = "responseMeasured")

table(cpctCrcMsi$immunotherapyAsPreTreatment)
table(cpctCrcMsi$immunotherapyAsTreatment)

cpctCrcImmunoInNonMSI <- cpctCrc %>% 
  add_column(immunotherapyAsPreTreatment = ifelse(is.na(str_detect(cpctCrc$preTreatmentsType, "Immunotherapy")), FALSE, str_detect(cpctCrc$preTreatmentsType, "Immuno")), .before = "treatmentGiven") %>%
  add_column(immunotherapyAsTreatment = ifelse(is.na(str_detect(cpctCrc$concatenatedTreatmentType, "Immunotherapy")), FALSE, str_detect(cpctCrc$concatenatedTreatmentType, "Immuno")), .before = "responseMeasured") %>%
  subset(immunotherapyAsTreatment == 'TRUE' | immunotherapyAsPreTreatment == 'TRUE') %>%
  subset(msStatus == 'MSS')

table(cpctCrcImmunoInNonMSI$immunotherapyAsPreTreatment)
table(cpctCrcImmunoInNonMSI$immunotherapyAsTreatment)

## BRAF
cpctCrc %>% group_by(brafV600EStatus) %>% summarise(count = n())
qplot(cpctCrc$brafV600EStatus)

cpctCrcBraf <- subset(cpctCrc, subset = (brafV600EStatus == 'positive'))
cpctCrcBraf <- add_column(cpctCrcBraf, brafTreatmentAsPreTreatment = ifelse(is.na(str_detect(cpctCrcBraf$preTreatmentsMechanism, "BRAF inhibitor")), FALSE, str_detect(cpctCrcBraf$preTreatmentsMechanism, "BRAF inhibitor")), .before = "treatmentGiven")
cpctCrcBraf <- add_column(cpctCrcBraf, brafTreatmentAsTreatment = ifelse(is.na(str_detect(cpctCrcBraf$concatenatedTreatmentMechanism, "BRAF inhibitor")), FALSE, str_detect(cpctCrcBraf$concatenatedTreatmentMechanism, "BRAF inhibitor")), .before = "responseMeasured")
qplot(cpctCrcBraf$brafTreatmentAsPreTreatment)
qplot(cpctCrcBraf$brafTreatmentAsTreatment)

cpctCrcBRAFInNonBRAF <- cpctCrc %>% 
  add_column(brafTreatmentAsPreTreatment = ifelse(is.na(str_detect(cpctCrc$preTreatmentsMechanism, "BRAF inhibitor")), FALSE, str_detect(cpctCrc$preTreatmentsMechanism, "BRAF inhibitor")), .before = "treatmentGiven") %>%
  add_column(brafTreatmentAsTreatment = ifelse(is.na(str_detect(cpctCrc$concatenatedTreatmentMechanism, "BRAF inhibitor")), FALSE, str_detect(cpctCrc$concatenatedTreatmentMechanism, "BRAF inhibitor")), .before = "responseMeasured") %>%
  subset(brafTreatmentAsTreatment == 'TRUE' | brafTreatmentAsPreTreatment == 'TRUE') %>%
  subset(brafV600EStatus == 'wildtype')

table(cpctCrcBRAFInNonBRAF$brafTreatmentAsPreTreatment)
table(cpctCrcBRAFInNonBRAF$brafTreatmentAsTreatment)

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

## TRIFLURIDINE
cpctCrcTrifluridine <-  subset(cpctCrc, subset = (treatmentCurated == 'Trifluridine'))
table(cpctCrcTrifluridine$hasBeenUntreated)


# 3 Implement various data mining algorithms
# 3.0 General data preparation

summary(cpct)
cpct$pfs <- as.numeric(cpct$pfs)
cpct$os <- as.numeric(cpct$os)
cpct$krasStatusBoolean <- ifelse(cpct$krasStatus=="positive",1,0) %>% as.integer()
summary(cpct)

## Removal of instances with missing data (NaN), subset data
os <- cpct %>% subset(select = c(ageAtTreatmentStart, pfs, os, krasStatusBoolean)) %>% na.omit()

## Split in training and test data set
set.seed(15039)
os_split <- initial_split(os, prop=0.8, strata=os)
os_train <- training(os_split)
os_test <- testing(os_split)
set.seed(NULL)

# 2.1 Linear regression model
# 2.1.1 Use PFS to predict OS
outcome_var <- c("os")
predictor_var <- c("pfs")
results <- linear_regression_model(training_set=os_train, test_set=os_test, outcome_var=outcome_var, predictor_vars=predictor_var, truth_var=c("os"))
lm_fit <- results[[1]]
lm_test_results <- results[[2]]

## Visualize output
pfs_prediction_grid <- tibble(
  pfs = c(
    os %>% select(pfs) %>% min(),
    os %>% select(pfs) %>% max()
  )
)

os_preds <- lm_fit %>%
  predict(pfs_prediction_grid) %>%
  bind_cols(pfs_prediction_grid)

ggplot(os, aes(x = pfs, y = os)) +
  geom_point(alpha = 0.4) +
  geom_line(data = os_preds,
            mapping = aes(x = pfs, y = .pred),
            color = "steelblue",
            linewidth = 1) +
  xlab(predictor_var) +
  ylab(outcome_var) +
  scale_y_continuous(labels = dollar_format()) +
  theme(text = element_text(size = 12))

# 2.1.2 Use PFS & ageAtTreatmentStart to predict OS
results <- linear_regression_model(training_set=os_train, test_set=os_test, outcome_var=c("os"), predictor_vars=c("ageAtTreatmentStart","pfs"), truth_var=c("os"))
lm_fit <- results[[1]]
lm_test_results <- results[[2]]

# 2.2 KNN regression (single-variable and multivariable)
## 2.2.1 Use PFS to predict OS using knn
ggplot(os, aes(x=pfs, y=os)) + geom_point(alpha=0.4)

output <- knn_cross_validation(training_set=os_train, outcome_var=c("os"), predictor_vars=c("pfs"), vfold=5, strata="os", kmax=250)
recipe <- output[[1]]
results <- output[[2]]
optimal_k <- knn_cross_validation_optimal_k(knn_cross_results=results)

ggplot(results, aes(x=neighbors, y=mean)) +
  geom_point(alpha=0.4) +
  geom_point(data=subset(results, neighbors == optimal_k), colour="blue")

output <- knn_run_on_test_set(training_set=os_train, test_set=os_test, k=optimal_k, recipe=recipe, truth_var=c("os"))
fit <- output[[1]]
summary <- output[[2]]

## Visualize output
pfs_prediction_grid <- tibble(
  pfs = seq(
    from = os %>% select(pfs) %>% min(),
    to = os %>% select(pfs) %>% max(),
    by=10
  )
)

preds <- fit %>%
  predict(pfs_prediction_grid) %>%
  bind_cols(pfs_prediction_grid)

ggplot(os, aes(x = pfs, y = os)) +
  geom_point(alpha = 0.4) +
  geom_line(data = preds,
            mapping = aes(x = pfs, y = .pred),
            color = "steelblue",
            linewidth = 1) +
  ggtitle(paste0("K = ", optimal_k)) +
  theme(text = element_text(size = 12))

## 2.2.2: Use PFS & ageAtTreatmentStart +/- a boolean of krasStatus to predict OS using knn
ggplot(os,aes(x=ageAtTreatmentStart, y=os)) + geom_point(alpha=0.4)
ggplot(os,aes(x=krasStatusBoolean, y=os)) + geom_point(alpha=0.4)

outcome_var <- c("os")
predictor_vars <- c("pfs","ageAtTreatmentStart")
predictor_vars_2 <- c("pfs","ageAtTreatmentStart","krasStatusBoolean")

output <- knn_cross_validation(training_set=os_train, outcome_var=outcome_var, predictor_vars=predictor_vars, vfold=5, strata="os")
recipe <- output[[1]]
results <- output[[2]]
output_2 <- knn_cross_validation(training_set=os_train, outcome_var=outcome_var, predictor_vars=predictor_vars_2, vfold=5, strata="os")
recipe_2 <- output_2[[1]]
results_2 <- output_2[[2]]

optimal_k <- knn_cross_validation_optimal_k(knn_cross_results=results)
optimal_k_2 <- knn_cross_validation_optimal_k(knn_cross_results=results_2)

ggplot(results, aes(x=neighbors, y=mean)) +
  geom_point(alpha=0.4) +
  geom_point(data=subset(results, neighbors == optimal_k), colour="blue")

output <- knn_run_on_test_set(training_set=os_train, test_set=os_test, k=optimal_k, recipe=recipe, truth_var=c("os"))
output_2 <- knn_run_on_test_set(training_set=os_train, test_set=os_test, k=optimal_k_2, recipe=recipe_2, truth_var=c("os"))
fit <- output[[1]]
summary <- output[[2]]
fit_2 <- output_2[[1]]
summary_2 <- output_2[[2]]

# 2.3 Implement KNN regression with categorical variables


# 2.4 Play around with survival plots
## OS survival plots for for untreated patients for gender, tumor location, first response
## Consider censoring
survival <- cpct %>% dplyr::filter(hasBeenUntreated=='TRUE') %>% subset(select = c(os, gender, primaryTumorLocation, firstResponse))
survival$status <- ifelse(!is.na(survival$os), 1, 0)
paste0("Nr of uncensored (included) patients: ", sum(survival$status == 1))

## General survival plot labels
y_lab_surv <- "Proportion"
x_lab_surv <- "Time (days)"

survGender <- survfit(Surv(survival$os, survival$status) ~ survival$gender, data = survival)
autoplot(survGender) + 
  labs(title="Overall survival from start treatment (in untreated patients)", y=y_lab_surv, x=x_lab_surv, color="Gender", fill = "Gender")
survdiff(Surv(survival$os, survival$status) ~ survival$gender, data = survival)

survivalTumorLocation <- survival %>% dplyr::filter(primaryTumorLocation %in% c('Breast','Colorectum','Lung'))
paste0("Nr of uncensored (included) patients: ", sum(survivalTumorLocation$status == 1))
survTumorLocation <- survfit(Surv(survivalTumorLocation$os, survivalTumorLocation$status) ~ survivalTumorLocation$primaryTumorLocation, data = survivalTumorLocation)
autoplot(survTumorLocation) + 
  labs(title="Overall survival from start treatment (in untreated patients)", y=y_lab_surv, x=x_lab_surv, color="Tumor location", fill = "Tumor location")
survdiff(Surv(survivalTumorLocation$os, survivalTumorLocation$status) ~ survivalTumorLocation$primaryTumorLocation, data = survivalTumorLocation)

survivalFirstResponse <- survival %>% dplyr::filter(firstResponse %in% c('PD','PR','SD'))
paste0("Nr of uncensored  (included) patients: ", sum(survivalFirstResponse$status == 1))
survFirstResponse <- survfit(Surv(survivalFirstResponse$os, survivalFirstResponse$status) ~ survivalFirstResponse$firstResponse, data = survivalFirstResponse)
autoplot(survFirstResponse) + 
  labs(title="Overall survival from start treatment (in untreated patients)", y=y_lab_surv, x=x_lab_surv, color="First response", fill = "First response")
survdiff(Surv(survivalFirstResponse$os, survivalFirstResponse$status) ~ survivalFirstResponse$firstResponse, data = survivalFirstResponse)

## PFS plots for for untreated patients for gender, tumor location, first response
## Consider censoring
survival <- cpct %>% dplyr::filter(hasBeenUntreated=='TRUE') %>% subset(select = c(pfs, gender, primaryTumorLocation, firstResponse))
survival$status <- ifelse(!is.na(survival$pfs), 1, 0)
paste0("Nr of uncensored patients ", sum(survival$status == 1))

survGender <- survfit(Surv(survival$pfs, survival$status) ~ survival$gender, data = survival)
autoplot(survGender) + 
  labs(title="PFS (in untreated patients)", y=y_lab_surv, x=x_lab_surv, color="Gender", fill = "Gender")
survdiff(Surv(survival$pfs, survival$status) ~ survival$gender, data = survival)

survivalTumorLocation <- survival %>% dplyr::filter(primaryTumorLocation %in% c('Breast','Colorectum','Lung'))
paste0("Nr of uncensored patients ", sum(survivalTumorLocation$status == 1))
survTumorLocation <- survfit(Surv(survivalTumorLocation$pfs, survivalTumorLocation$status) ~ survivalTumorLocation$primaryTumorLocation, data = survivalTumorLocation)
autoplot(survTumorLocation) + 
  labs(title="PFS (in untreated patients)", y=y_lab_surv, x=x_lab_surv, color="Tumor location", fill = "Tumor location")
survdiff(Surv(survivalTumorLocation$pfs, survivalTumorLocation$status) ~ survivalTumorLocation$primaryTumorLocation, data = survivalTumorLocation)

survivalFirstResponse <- survival %>% dplyr::filter(firstResponse %in% c('PD','PR','SD'))
paste0("Nr of uncensored patients ", sum(survivalFirstResponse$status == 1))
survFirstResponse <- survfit(Surv(survivalFirstResponse$pfs, survivalFirstResponse$status) ~ survivalFirstResponse$firstResponse, data = survivalFirstResponse)
autoplot(survFirstResponse) + 
  labs(title="PFS (in untreated patients)", y=y_lab_surv, x=x_lab_surv, color="First response", fill = "First response")
survdiff(Surv(survivalFirstResponse$pfs, survivalFirstResponse$status) ~ survivalFirstResponse$firstResponse, data = survivalFirstResponse)


# 3. All Investigate relationship between age at start treatment & treatment duration ------------------------------------------------------------------
categoriesTherapy <- c("Immunotherapy", "Hormonal therapy", "Chemotherapy", "Targeted therapy")
categoriesTumor <- c("Lung", "Breast", "Prostate", "Skin")

corValueTherapy <- data.frame()
corValueTumor <- data.frame()

i <- 1
par(mfrow = c(2, 2))
for (x in categoriesTherapy) {
  corValueTherapy[1,i] <- cor(cpct[cpct$consolidatedTreatmentType == x,]$treatmentDuration, cpct[cpct$consolidatedTreatmentType == x,]$ageAtRegistration, use='complete.obs')
  plot(treatmentDuration ~ ageAtRegistration, data=subset(cpct, consolidatedTreatmentType == x), main = paste0(x, " cc = ", round(corValueTherapy[i],4)))
  i <- i+1
}

colnames(corValueTherapy) <- categoriesTherapy

i <- 1
par(mfrow = c(2, 2))
for (x in categoriesTumor) {
  corValueTumor[1,i] <- cor(cpct[cpct$primaryTumorLocation == x,]$treatmentDuration, cpct[cpct$primaryTumorLocation == x,]$ageAtRegistration, use='complete.obs')
  plot(treatmentDuration ~ ageAtRegistration, data=subset(cpct, primaryTumorLocation == x), main = paste0(x, " cc = ", round(corValueTumor[i],4)))
  i <- i+1
}

colnames(corValueTumor) <- categoriesTumor

# 4. All Investigate relationship between age at start treatment & treatment response ------------------------------------------------------------------
## 4.1 Matrix of response based on bucketed age at start treatment

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
  responseDetailsPerAgeBucket[1, i] <- nrow(cpct[cpct$ageAtRegistration <= x[2] & cpct$ageAtRegistration >= x[1] & !is.na(cpct$ageAtRegistration) & !is.na(cpct$firstResponse) & cpct$firstResponse != "ND" & cpct$firstResponse != "Non-CR/Non-PD" & cpct$firstResponse != "Clinical progression", ])
  responseDetailsPerAgeBucket[2, i] <- nrow(cpct[cpct$ageAtRegistration <= x[2] & cpct$ageAtRegistration >= x[1] & !is.na(cpct$ageAtRegistration) & !is.na(cpct$firstResponse) & cpct$firstResponse == "PD", ])
  responseDetailsPerAgeBucket[3, i] <- responseDetailsPerAgeBucket[2,i]/responseDetailsPerAgeBucket[1,i]
  responseDetailsPerAgeBucket[4, i] <- nrow(cpct[cpct$ageAtRegistration <= x[2] & cpct$ageAtRegistration >= x[1] & !is.na(cpct$ageAtRegistration) & !is.na(cpct$firstResponse) & (cpct$firstResponse == "PR" | cpct$firstResponse =="CR"), ])
  responseDetailsPerAgeBucket[5, i] <- responseDetailsPerAgeBucket[4,i]/responseDetailsPerAgeBucket[1,i]
  responseDetailsPerAgeBucket[6, i] <- nrow(cpct[cpct$ageAtRegistration <= x[2] & cpct$ageAtRegistration >= x[1] & !is.na(cpct$ageAtRegistration) & !is.na(cpct$firstResponse) & cpct$firstResponse == "SD", ])
  responseDetailsPerAgeBucket[7, i] <- responseDetailsPerAgeBucket[6,i]/responseDetailsPerAgeBucket[1,i]
  i <- i+1
}

colnames(responseDetailsPerAgeBucket) <- c("20-40y", "40-50y", "50-60y", "60-70y", "70-80y", "80-100y")
rownames(responseDetailsPerAgeBucket) <- c("n","n with PD first response","fraction PD first response","n with PR/CR first response","fraction PR/CR first response", "n with SD first response","fraction SD first response")


## 4.2. Plot progressive-disease fraction per age at start treatment

adultAge <- c(18:100)

progressiveDiseaseFractionPerAge <- data.frame()
i<-1

for (x in adultAge) {
  tmp <- cpct[cpct$ageAtRegistration == x & !is.na(cpct$ageAtRegistration) & !is.na(cpct$firstResponse) & cpct$firstResponse != "ND" & cpct$firstResponse != "Non-CR/Non-PD", ]
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

