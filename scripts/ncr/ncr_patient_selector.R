library(dplyr)

rm(list = ls())
source(paste0(Sys.getenv("HOME"), "/hmf/repos/actin-analysis/scripts/ncr/ncr_data_exploration_functions.R"))
source(paste0(Sys.getenv("HOME"), "/hmf/repos/actin-analysis/scripts/ncr/ncr_patients_like_me_functions.R"))

ncr <- load_ncr_data()

## Define parameters to match against
patient_age <- 71
patient_who <- 0

ncr_matches <- find_similar_patients(ncr, patient_age, patient_who)

ncr_matches %>% summarise(count_key_nkr = n(), distinct_count_key_nkr = n_distinct(key_nkr))


