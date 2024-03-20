library(dplyr)

rm(list = ls())
source(paste0(Sys.getenv("HOME"), "/hmf/repos/actin-analysis/scripts/ncr/ncr_data_exploration_functions.R"))

ncr <- load_ncr_data()

## Define parameters to match against
patient_age <- 71
patient_who <- 0

## Rules:
##  - Age within one year of patient and matching WHO status (perf_stat)
##  - DIA episodes where metastases where present at start of episode
##  - No prior malignancies (mal1_int NA)
##  - Adenocarcinoma (morf_cat == 1) without any other tumors (dubbeltum == 0)
##  - Has had treatment (tumgericht_ther == 1)
ncr_matches <- ncr %>% dplyr::filter(epis == 'DIA' &
                                       meta_epis == 1 &
                                       is.na(mal1_int) &
                                       morf_cat == 1 &
                                       dubbeltum == 0 &
                                       tumgericht_ther == 1 &
                                       leeft >= patient_age - 1 &
                                       leeft <= patient_age + 1 &
                                       perf_stat == patient_who)

ncr_matches %>% summarise(count_key_nkr = n(), distinct_count_key_nkr = n_distinct(key_nkr))


