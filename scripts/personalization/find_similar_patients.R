library(dplyr)
library(tibble)
library(RMySQL)
library(DBI)

rm(list=ls())

source(paste0("~/hmf/repos/actin-personalization/scripts/personalization/find_similar_patients_functions.R"))

# Retrieve data ------------------------------------------------------------------
dbActinPersonalization <- dbConnect(MySQL(), dbname='actin_personalization', groups="RAnalysis")

query_ref <-"select * from diagnosisTreatments;"
ref <- dbGetQuery(dbActinPersonalization, query_ref)
dbDisconnect(dbActinPersonalization)

# Find similar patients -----------------------------------------------------------
patient_age <- X #Eg 85
patient_who <- X #Eg 2
patient_ras_status <- X #Eg 1
patient_lesion_list <- X #Eg c("Lung")

ref_general <- find_similar_patients_general(ref)
range<-5
ref_age <- find_similar_patients_age(ref_general, patient_age=patient_age, range=range)
ref_who <- find_similar_patients_who(ref_general, patient_who=patient_who)
ref_ras <- find_similar_patients_ras(ref_general, patient_ras_status=patient_ras_status)
ref_lesions <- find_similar_patients_lesions(ref_general, patient_lesions=patient_lesion_list)

# Generate data for treatment decision table--------------------------------------
treatments_to_exclude <- c("NA","OTHER")

df_td_gen <- ref_general %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  count(systemicTreatmentPlan) %>%
  mutate(perc_gen = round(n/sum(n)*100,1)) %>%
  rename(n_gen=n)

df_td_age <- ref_age %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  count(systemicTreatmentPlan) %>%
  mutate(perc_age = round(n/sum(n)*100,1)) %>%
  rename(n_age=n)

df_td_who <- ref_who %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  count(systemicTreatmentPlan) %>%
  mutate(perc_who = round(n/sum(n)*100,1)) %>%
  rename(n_who=n)

df_td_ras <- ref_ras %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  count(systemicTreatmentPlan) %>%
  mutate(perc_ras = round(n/sum(n)*100,1)) %>%
  rename(n_ras=n)

df_td_lesions <- ref_lesions %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  count(systemicTreatmentPlan)
sum <- sum(df_td_lesions$n)
df_td_lesions <- df_td_lesions %>%
  mutate(perc_lesions =round(n/sum*100,1)) %>%
  rename(n_lesions=n)

dfs_td <- merge(df_td_gen, df_td_age, by="systemicTreatmentPlan", all.x=T) %>%
  merge(df_td_who, by="systemicTreatmentPlan", all.x=T) %>%
  merge(df_td_ras, by="systemicTreatmentPlan", all.x=T) %>%
  merge(df_td_lesions, by="systemicTreatmentPlan", all.x=T) %>%
  replace(is.na(.), 0) 

# Generate data for PFS table-----------------------------------------------------
df_pfs_gen <- ref_general %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  dplyr::filter(!is.na(systemicTreatmentPlanPfs)) %>%
  group_by(systemicTreatmentPlan) %>%
  summarize(n_gen=n(), median_gen=median(as.numeric(systemicTreatmentPlanPfs)), min_gen=min(as.numeric(systemicTreatmentPlanPfs)), max_gen=max(as.numeric(systemicTreatmentPlanPfs))) %>%
  ungroup()

df_pfs_age <- ref_age %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  dplyr::filter(!is.na(systemicTreatmentPlanPfs)) %>%
  group_by(systemicTreatmentPlan) %>%
  summarize(n_age=n(), median_age=median(as.numeric(systemicTreatmentPlanPfs)), min_age=min(as.numeric(systemicTreatmentPlanPfs)), max_age=max(as.numeric(systemicTreatmentPlanPfs))) %>%
  ungroup()

df_pfs_who <- ref_who %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  dplyr::filter(!is.na(systemicTreatmentPlanPfs)) %>%
  group_by(systemicTreatmentPlan) %>%
  summarize(n_who=n(), median_who=median(as.numeric(systemicTreatmentPlanPfs)), min_who=min(as.numeric(systemicTreatmentPlanPfs)), max_who=max(as.numeric(systemicTreatmentPlanPfs))) %>%
  ungroup()

df_pfs_ras <- ref_ras %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  dplyr::filter(!is.na(systemicTreatmentPlanPfs)) %>%
  group_by(systemicTreatmentPlan) %>%
  summarize(n_ras=n(), median_ras=median(as.numeric(systemicTreatmentPlanPfs)), min_ras=min(as.numeric(systemicTreatmentPlanPfs)), max_ras=max(as.numeric(systemicTreatmentPlanPfs))) %>%
  ungroup()

df_pfs_lesions <- ref_lesions %>%
  dplyr::filter(!systemicTreatmentPlan %in% treatments_to_exclude) %>%
  dplyr::filter(!is.na(systemicTreatmentPlanPfs)) %>%
  group_by(systemicTreatmentPlan) %>%
  summarize(n_lesions=n(), median_lesions=median(as.numeric(systemicTreatmentPlanPfs)), min_lesions=min(as.numeric(systemicTreatmentPlanPfs)), max_lesions=max(as.numeric(systemicTreatmentPlanPfs))) %>%
  ungroup()

dfs_pfs <- merge(df_pfs_gen, df_pfs_age, by="systemicTreatmentPlan", all.x=T) %>%
  merge(df_pfs_who, by="systemicTreatmentPlan", all.x=T) %>%
  merge(df_pfs_ras, by="systemicTreatmentPlan", all.x=T) %>%
  merge(df_pfs_lesions, by="systemicTreatmentPlan", all.x=T) 

# Add formatting to treatment decision data----------------------------------------
dfs_td_disp <- dfs_td %>%
  arrange(desc(perc_gen)) %>%
  mutate(across(contains("perc"), ~ paste0(., "%")))

n_sums <- sapply(dfs_td_disp[, grep("^n_", names(dfs_td_disp))], sum)
perc_columns <- grep("^perc_", names(dfs_td_disp), value=T)

patient_formatted_lesions <- format_lesions(patient_lesion_list)
intended_names <- c("General",paste0("Age=",patient_age-range,"-",patient_age+range,"y"),paste0("WHO=",patient_who),paste0("RAS status=",if (patient_ras_status==1) {"positive"} else {"negative"}), paste0("Lesions=",patient_formatted_lesions))
new_perc_column_names <- paste0(intended_names, " (n=", n_sums, ")")
names(dfs_td_disp)[grep("^perc", names(dfs_td_disp))] <- new_perc_column_names

dfs_td_disp <- dfs_td_disp[, !names(dfs_td_disp) %in% c("n_age", "n_who", "n_gen", "n_ras", "n_lesions")]

# Add formatting to PFS data----------------------------------------
dfs_pfs_disp <- dfs_pfs %>%
  mutate(across(starts_with("n_"), ~ coalesce(., 0)))

dfs_pfs_disp <- dfs_pfs_disp[match(dfs_td_disp$systemicTreatmentPlan, dfs_pfs_disp$systemicTreatmentPlan), ] #reorder
  
median_columns <- grep("^median_", names(dfs_pfs_disp), value=T)
dfs_pfs_disp <- dfs_pfs_disp %>%
  rowwise() %>%
  mutate(across(all_of(median_columns), ~ paste0(.x, " (", get(paste0("min_",substring(cur_column(), 8))),"-",get(paste0("max_",substring(cur_column(), 8))),")", " (n=", get(paste0("n_", substring(cur_column(), 8))), ")"))) %>%
  ungroup()

n_sums <- sapply(dfs_pfs_disp[, grep("^n_", names(dfs_pfs_disp))], sum)
intended_names <- c("PFS general (mdn, range)",paste0("PFS age=",patient_age-range,"-",patient_age+range,"y (mdn, range)"),paste0("PFS WHO=",patient_who, " (mdn, range)"),paste0("PFS RAS status=",if (patient_ras_status==1) {"positive"} else {"negative"}, " (mdn, range)"), paste0("PFS lesions=",patient_formatted_lesions, " (mdn, range)"))
new_median_column_names <- paste0(intended_names, " (n=", n_sums, ")")
names(dfs_pfs_disp)[grep("^median", names(dfs_pfs_disp))] <- new_median_column_names

columns_to_drop <- grep(paste(c("^n_","^max_","^min_"), collapse="|"), names(dfs_pfs_disp), value = T)
dfs_pfs_disp <- dfs_pfs_disp[, !names(dfs_pfs_disp) %in% columns_to_drop]





