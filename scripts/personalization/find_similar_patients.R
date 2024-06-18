library(dplyr)
library(tibble)

rm(list=ls())

source(paste0("~/hmf/repos/actin-personalization/scripts/personalization/find_similar_patients_functions.R"))

# Retrieve data ------------------------------------------------------------------
dbActinPersonalization <- dbConnect(MySQL(), dbname='actin_personalization', groups="RAnalysis")

query_ref <-"select * from diagnosisTreatments;"
ref <- dbGetQuery(dbActinPersonalization, query_ref)
dbDisconnect(dbActinPersonalization)

# Find similar patients -----------------------------------------------------------
patient_age<-X
patient_who<-X
patient_ras_status<-X

ref_general <- find_similar_patients_general(ref)
range<-5
ref_age <- find_similar_patients_age(ref, patient_age=patient_age, range=range)
ref_who <- find_similar_patients_who(ref, patient_who=patient_who)
ref_ras <- find_similar_patients_ras(ref, patient_ras_status=patient_ras_status)

# Generate data -----------------------------------------------------------
treatments_to_exclude <- c("NA","OTHER")

df_gen <- ref_general %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  count(systemicTreatmentPlan) %>%
  mutate(perc_gen = round(n/sum(n)*100,1)) %>%
  rename(n_gen=n)

df_age <- ref_age %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  count(systemicTreatmentPlan) %>%
  mutate(perc_age = round(n/sum(n)*100,1)) %>%
  rename(n_age=n)

df_who <- ref_who %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  count(systemicTreatmentPlan) %>%
  mutate(perc_who = round(n/sum(n)*100,1)) %>%
  rename(n_who=n)

df_ras <- ref_ras %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  count(systemicTreatmentPlan) %>%
  mutate(perc_ras = round(n/sum(n)*100,1)) %>%
  rename(n_ras=n)

dfs <- merge(df_gen, df_age, by="systemicTreatmentPlan", all.x=T) %>%
  merge(df_who, by="systemicTreatmentPlan", all.x=T) %>%
  merge(df_ras, by="systemicTreatmentPlan", all.x=T) %>%
  replace(is.na(.), 0) 

row.names(dfs) <- dfs$systemicTreatmentPlan  

# Add formatting to data -----------------------------------------------------------
dfs_disp <- dfs %>%
  arrange(desc(perc_gen)) %>%
  mutate(across(contains("perc"), ~ paste0(., "%")))

n_sums <- sapply(dfs_disp[, grep("^n_", names(dfs_disp))], sum)
perc_columns <- grep("^perc_", names(dfs_disp), value=T)

intended_names <- c("General",paste0("Age=",patient_age-range,"-",patient_age+range,"y"),paste0("WHO=",patient_who),paste0("RAS status=",if (patient_ras_status==1) {"positive"} else {"negative"}))
new_perc_column_names <- paste0(intended_names, " (n=", n_sums, ")")
names(dfs_disp)[grep("^perc", names(dfs_disp))] <- new_perc_column_names

dfs_disp <- dfs_disp[, !names(dfs_disp) %in% c("systemicTreatmentPlan", "n_age", "n_who", "n_gen", "n_ras")]


