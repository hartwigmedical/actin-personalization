library(dplyr)
library(tibble)

rm(list=ls())

source(paste0(Sys.getenv("HOME"), "/hmf/repos/actin-personalization/scripts/ncr/ncr_data_exploration_functions.R"))

ncr_latest <- load_ncr_data_latest()
ncr_orig <- load_ncr_data_1()

# general impression of data sets by incjr 
ncr_latest %>% group_by(incjr) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr_orig %>% group_by(incjr) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))

# check column name differences
cols_ncr_latest <- colnames(ncr_latest)
cols_ncr_orig <- colnames(ncr_orig)
cols_unique_to_latest <- setdiff(cols_ncr_latest, cols_ncr_orig)
cols_unique_to_orig <- setdiff(cols_ncr_orig, cols_ncr_latest)

# check numbers of key_ncr data that are gained or lost
results <- anti_join(ncr_latest, ncr_orig, by = c("key_nkr"))

ncr_key_orig <- ncr_orig %>% select(key_nkr) %>% distinct()
ncr_key_latest <- ncr_latest %>% select(key_nkr) %>% distinct()

lost_keys_ncr <- setdiff(ncr_key_orig$key_nkr, ncr_key_latest$key_nkr)
gained_keys_ncr <- setdiff(ncr_key_latest$key_nkr, ncr_key_orig$key_nkr)

ncr_latest_lost <- ncr_orig %>% dplyr::filter(key_nkr %in% lost_keys_ncr) 
ncr_latest_lost %>% group_by(incjr) %>% summarise(distinct_count_key_nkr=n_distinct(key_nkr))

ncr_latest_gained <- ncr_latest %>% dplyr::filter(key_nkr %in% gained_keys_ncr) 
ncr_latest_gained %>% group_by(incjr) %>% summarise(distinct_count_key_nkr=n_distinct(key_nkr))

# check for unexpected events in column 'administratienummer'
ncr_latest_unexpected <- ncr_latest %>% dplyr::filter(administratienummer != key_eid)
nrow(ncr_latest_unexpected)

# analyze changes in vital status
ncr_combined <- full_join(ncr_latest, ncr_orig, by=("key_eid"), suffix = c("_latest","_orig"))
changed_vit_status_0_to_1 <- ncr_combined %>% dplyr::filter(vit_stat_orig == 0 & vit_stat_latest == 1) 
changed_vit_status_1_to_0 <- ncr_combined %>% dplyr::filter(vit_stat_orig == 1 & vit_stat_latest == 0) 

changed_vit_status_0_to_0 <- ncr_combined %>% dplyr::filter(vit_stat_orig == 0 & vit_stat_latest == 0) %>% 
  mutate(vit_stat_int_diff = vit_stat_int_latest-vit_stat_int_orig) %>% 
  dplyr::select(c("key_nkr_orig","vit_stat_orig","vit_stat_int_orig","vit_stat_latest","vit_stat_int_latest","vit_stat_int_diff"))

changed_vit_status_1_to_1 <- ncr_combined %>% dplyr::filter(vit_stat_orig == 1 & vit_stat_latest == 1) %>%
  mutate(vit_stat_int_diff = vit_stat_int_latest-vit_stat_int_orig) %>%
  dplyr::select(c("key_nkr_orig","vit_stat_orig","vit_stat_int_orig","vit_stat_latest","vit_stat_int_latest","vit_stat_int_diff"))

## check if content has changed for 300 random samples
ncr_latest <- ncr_latest %>% subset(select = -administratienummer) 
random_key_eid_values <- sample(ncr_orig$key_eid, 300, replace = FALSE)

compare_columns <- function(df1, df2, key_column, key_values) {
  result <- list()
  for (key in key_values) {
    row1 <- df1[df1[[key_column]] == key,]
    row2 <- df2[df2[[key_column]] == key,]
    if (nrow(row1) > 0 & nrow(row2) > 0) {
      comparison <- row1 == row2
      result[[as.character(key)]] <- comparison
    } else {
      result[[as.character(key)]] <- NA
    }
  }
  return(result)
}

comparison_result <- compare_columns(ncr_latest, ncr_orig, "key_eid", random_key_eid_values)

locations <- list()
for (i in seq_along(comparison_result)) {
  sublist <- comparison_result[[i]]
  test_indices <- which(sublist == "FALSE") # FALSE implies data that has changed
  if (length(test_indices) > 0) {
    for (j in test_indices) {
      locations <- append(locations, list(c(i, j)))
    }
  }
}
