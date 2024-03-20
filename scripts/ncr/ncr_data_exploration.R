library(dplyr)
library(tibble)

rm(list=ls())
ncr <- read.csv(paste0(Sys.getenv("HOME"), "/hmf/tmp/ncr_crc_dataset.csv"), sep=";")

source(paste0(Sys.getenv("HOME"), "/hmf/repos/actin-analysis/scripts/ncr/ncr_data_exploration_functions.R"))

## Keys, epis, meta_epis, teller
ncr %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr), distinct_count_key_zid=n_distinct(key_zid), distinct_count_key_eid=n_distinct(key_eid))

ncr %>% group_by(epis) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, meta_epis) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, teller) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, meta_epis, teller) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))

## epis=DIA
ncr_dia <- ncr %>% dplyr::filter(epis=='DIA') %>% arrange(key_nkr)
ncr_dia %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))

pts_with_key_occurring_more_than_once_dia <- names(table(ncr_dia$key_nkr)[table(ncr_dia$key_nkr) > 1])
ncr_dia_pts_multiple_dia <- ncr_dia[ncr_dia$key_nkr %in% pts_with_key_occurring_more_than_once_dia, ]
n_distinct(ncr_dia_pts_multiple_dia$key_nkr)

## epis=VERB
ncr_verb <- ncr %>% dplyr::filter(epis=='VERB') %>% arrange(key_nkr)
ncr_verb %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))

pts_with_key_occurring_more_than_once_verb <- names(table(ncr_verb$key_nkr)[table(ncr_verb$key_nkr) > 1])
ncr_verb_pts_multiple_verb <- ncr_verb[ncr_verb$key_nkr %in% pts_with_key_occurring_more_than_once_verb, ]
pts_with_verb <- names(table(ncr_verb$key_nkr))
ncr_dia_pts_with_verb <- ncr_dia[ncr_dia$key_nkr %in% pts_with_verb, ]

## meta_epis
df_easy <- c('key_nkr', 'epis', 'meta_epis', 'cstadium', 'pstadium', 'stadium', 'meta_topo_sublok1', 'meta_topo_sublok2', 'meta_topo_sublok3', 'meta_int1', 'meta_int2', 'meta_int3', 'meta_prog1', 'meta_prog2', 'meta_prog3',
             'tumgericht_ther', 'mdl_res', 'mdl_res_int1', 'chir', 'chir_type1', 'chir_int1', 'rt', 'chemort', 'rt_start_int1', 'meta_rt_code1', 'meta_rt_start_int1', 'meta_chir_int1', 'hipec', 'hipec_int1', 'chemo', 'target',
              'syst_start_int1', 'syst_stop_int1','syst_kuren1', 'respons_int', 'pfs_event1', 'pfs_int1')

ncr_pts_with_immediate_mets_but_low_stage <- ncr %>%
  select(all_of(df_easy)) %>%
  dplyr::filter(stadium %in% c(1,2,3,'2A','2B','2C','3A','3B','3C') & meta_epis==1) %>%
  arrange(stadium)

ncr_pts_with_later_mets_but_high_stage <- ncr %>%
  select(all_of(df_easy)) %>%
  dplyr::filter(stadium %in% c(4,'4A','4B','4C') & meta_epis==2) %>%
  arrange(stadium)

## Other data exploration
### Age, vit stat, perf stat, scales
c(min(ncr$leeft), max(ncr$leeft), median(ncr$leeft))
hist(ncr$leeft, breaks=(max(ncr$leeft)-min(ncr$leeft)))

ncr %>% group_by(epis, vit_stat) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, perf_stat) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))

ncr %>% group_by(epis, asa) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, cci) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
hist(ncr$cci, breaks=(max(ncr$cci, na.rm=T)-min(ncr$cci, na.rm=T)))

ncr %>% group_by(epis, morf_cat) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
hist(ncr$cci, breaks=(max(ncr$cci, na.rm=T)-min(ncr$cci, na.rm=T)))

### Molecular 
ncr %>% group_by(epis, msi_stat) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, braf_mut) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, ras_mut) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))

### Lab
unknown_value <- 9999
hist(ncr$prechir_cea[ncr$prechir_cea!=unknown_value], breaks = 100)
hist(ncr$postchir_cea[ncr$postchir_cea!=unknown_value], breaks = 100)
hist(ncr$ldh1[ncr$ldh1!=unknown_value], breaks = 100)
hist(ncr$af1[ncr$af1!=unknown_value], breaks = 100)
hist(ncr$neutro1[ncr$neutro1!=unknown_value], breaks = 100)
hist(ncr$albumine1[ncr$albumine1!=unknown_value], breaks = 100)
hist(ncr$leuko1[ncr$leuko1!=unknown_value], breaks = 100)

### Treatment
ncr %>% group_by(epis, tumgericht_ther) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% dplyr::filter(epis=='DIA') %>% dplyr::filter(tumgericht_ther==0) %>% group_by(geen_ther_reden) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))

ncr_unknown_which_treatment <- ncr %>%
  dplyr::filter(tumgericht_ther==1) %>%
  dplyr::filter(mdl_res == 0 &
                  chir == 0 &
                  rt == 0 &
                  chemort == 0 &
                  chemo == 0 &
                  target == 0 &
                  hipec == 0 &
                  meta_chir_code1 == "" &
                  meta_rt_code1 == "")

ncr %>% dplyr::filter(tumgericht_ther==1) %>% group_by(mdl_res, chir) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% dplyr::filter(tumgericht_ther==1) %>% group_by(rt, chemort) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% dplyr::filter(tumgericht_ther==1) %>% group_by(chemo) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% dplyr::filter(tumgericht_ther==1) %>% group_by(target) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% dplyr::filter(tumgericht_ther==1) %>% group_by(hipec) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))

### Treatment outcome
ncr %>% dplyr::filter(tumgericht_ther==1) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(tumgericht_ther, respons_uitslag) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
hist(ncr$respons_int, breaks=100)

ncr %>%
  group_by(epis, tumgericht_ther, pfs_event1) %>%
  summarise(count = n(), distinct_count_key_nkr = n_distinct(key_nkr))

## Tasks
## Select systemic therapy lines for every patient
ncr_lines_substance_prep <- ncr %>%
  dplyr::filter(tumgericht_ther==1) %>%
  select(c('key_nkr','key_zid'),starts_with(c('syst_schemanum','syst_code'))) %>%
  pivot_longer(cols = starts_with("syst_schemanum"), names_to = "syst_schemanum_key", values_to = "syst_schemanum_value") %>%
  pivot_longer(cols = starts_with("syst_code"), names_to = "syst_code_key", values_to = "syst_code_value")

ncr_lines_substance <- ncr_lines_substance_prep %>%
  add_column(schemanum_code_value = as.numeric(gsub("\\D", "", ncr_lines_substance_prep$syst_schemanum_key)), .after = 2) %>%
  add_column(code_schemanum_value = as.numeric(gsub("\\D", "", ncr_lines_substance_prep$syst_code_key)), .after = 6) %>%
  distinct() %>%
  group_by(key_nkr, key_zid) %>%
  do(concat_syst_code_values(.)) %>%
  ungroup()

ncr_lines_substance[] <- lapply(ncr_lines_substance, function(x) gsub("^c\\((.*)\\)$", "\\1", x))
ncr_lines_substance[ncr_lines_substance == "character(0)"] <- ""

ncr_lines_substance_written <- ncr_lines_substance %>%
  add_column(line1_written = sapply(ncr_lines_substance$line1, translate_atc)) %>%
  add_column(line2_written = sapply(ncr_lines_substance$line2, translate_atc)) %>%
  add_column(line3_written = sapply(ncr_lines_substance$line3, translate_atc)) %>%
  add_column(line4_written = sapply(ncr_lines_substance$line4, translate_atc)) %>%
  add_column(line5_written = sapply(ncr_lines_substance$line5, translate_atc)) %>%
  add_column(line6_written = sapply(ncr_lines_substance$line6, translate_atc)) %>%
  add_column(line7_written = sapply(ncr_lines_substance$line7, translate_atc)) %>%
  select(matches("key") | matches("written"))

ncr_first_lines_summary <- ncr_lines_substance_written %>% dplyr::filter(line1 != "") %>% group_by(line1_read) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))

### Select start and stop interval for every line, merge with substances and calculate duration
ncr_lines_start_prep <- ncr %>%
  dplyr::filter(tumgericht_ther==1) %>%
  select(c('key_nkr','key_zid'),starts_with(c('syst_schemanum','syst_start_int'))) %>%
  pivot_longer(cols = starts_with("syst_schemanum"), names_to = "syst_schemanum_key", values_to = "syst_schemanum_value") %>%
  pivot_longer(cols = starts_with("syst_start_int"), names_to = "syst_start_int_key", values_to = "syst_start_int_value")

ncr_lines_start <- ncr_lines_start_prep %>%
  add_column(schemanum_line_start_value = as.numeric(gsub("\\D", "", ncr_lines_start_prep$syst_schemanum_key)), .after = 2) %>%
  add_column(line_start_schemanum_value = as.numeric(gsub("\\D", "", ncr_lines_start_prep$syst_start_int_key)), .after = 6) %>%
  distinct() %>%
  group_by(key_nkr, key_zid) %>%
  do(concat_start_int_values(.)) %>%
  ungroup()

ncr_lines_start[] <- lapply(ncr_lines_start, function(x) gsub("^c\\((.*)\\)$", "\\1", x))
ncr_lines_start[ncr_lines_start == "integer(0)"] <- ""
ncr_lines_start$line_start_1 <- sapply(ncr_lines_start$line_start_1, extract_min)
ncr_lines_start$line_start_2 <- sapply(ncr_lines_start$line_start_2, extract_min)
ncr_lines_start$line_start_3 <- sapply(ncr_lines_start$line_start_3, extract_min)
ncr_lines_start$line_start_4 <- sapply(ncr_lines_start$line_start_4, extract_min)
ncr_lines_start$line_start_5 <- sapply(ncr_lines_start$line_start_5, extract_min)
ncr_lines_start$line_start_6 <- sapply(ncr_lines_start$line_start_6, extract_min)
ncr_lines_start$line_start_7 <- sapply(ncr_lines_start$line_start_7, extract_min)

ncr_lines_stop_prep <- ncr %>%
  dplyr::filter(tumgericht_ther==1) %>%
  select(c('key_nkr','key_zid'),starts_with(c('syst_schemanum','syst_stop_int'))) %>%
  pivot_longer(cols = starts_with("syst_schemanum"), names_to = "syst_schemanum_key", values_to = "syst_schemanum_value") %>%
  pivot_longer(cols = starts_with("syst_stop_int"), names_to = "syst_stop_int_key", values_to = "syst_stop_int_value")

ncr_lines_stop <- ncr_lines_stop_prep %>%
  add_column(schemanum_line_stop_value = as.numeric(gsub("\\D", "", ncr_lines_stop_prep$syst_schemanum_key)), .after = 2) %>%
  add_column(line_stop_schemanum_value = as.numeric(gsub("\\D", "", ncr_lines_stop_prep$syst_stop_int_key)), .after = 6) %>%
  distinct() %>%
  group_by(key_nkr, key_zid) %>%
  do(concat_stop_int_values(.)) %>%
  ungroup()

ncr_lines_stop[] <- lapply(ncr_lines_stop, function(x) gsub("^c\\((.*)\\)$", "\\1", x))
ncr_lines_stop[ncr_lines_stop == "integer(0)"] <- ""
ncr_lines_stop$line_stop_1 <- sapply(ncr_lines_stop$line_stop_1, extract_max)
ncr_lines_stop$line_stop_2 <- sapply(ncr_lines_stop$line_stop_2, extract_max)
ncr_lines_stop$line_stop_3 <- sapply(ncr_lines_stop$line_stop_3, extract_max)
ncr_lines_stop$line_stop_4 <- sapply(ncr_lines_stop$line_stop_4, extract_max)
ncr_lines_stop$line_stop_5 <- sapply(ncr_lines_stop$line_stop_5, extract_max)
ncr_lines_stop$line_stop_6 <- sapply(ncr_lines_stop$line_stop_6, extract_max)
ncr_lines_stop$line_stop_7 <- sapply(ncr_lines_stop$line_stop_7, extract_max)

ncr_lines_details <- inner_join(ncr_lines_substance_written,ncr_lines_start, by=c('key_nkr','key_zid')) %>%
  inner_join(ncr_lines_stop, by=c('key_nkr','key_zid')) %>%
  add_column(line_duration_1 = ifelse(join$line_stop_1 == "","", as.integer(join$line_stop_1)-as.integer(join$line_start_1))) %>%
  add_column(line_duration_2 = ifelse(join$line_stop_2 == "","", as.integer(join$line_stop_2)-as.integer(join$line_start_2))) %>%
  add_column(line_duration_3 = ifelse(join$line_stop_3 == "","", as.integer(join$line_stop_3)-as.integer(join$line_start_3))) %>%
  add_column(line_duration_4 = ifelse(join$line_stop_4 == "","", as.integer(join$line_stop_4)-as.integer(join$line_start_4))) %>%
  add_column(line_duration_5 = ifelse(join$line_stop_5 == "","", as.integer(join$line_stop_5)-as.integer(join$line_start_5))) %>%
  add_column(line_duration_6 = ifelse(join$line_stop_6 == "","", as.integer(join$line_stop_6)-as.integer(join$line_start_6))) %>%
  add_column(line_duration_7 = ifelse(join$line_stop_7 == "","", as.integer(join$line_stop_7)-as.integer(join$line_start_7)))


### Overall survival exploration (WIP)
ncr_surv <- ncr %>% dplyr::filter(tumgericht_ther==1 & (chemo==4 | target==4)) %>%
  select(c('key_nkr','epis'),matches(c('vit_')),c('syst_code1','syst_code2','syst_code3','syst_code4','syst_code5','syst_code6','syst_code7','syst_code8','syst_code9','syst_code10','syst_code11','syst_code12'),matches(c('syst_schemanum','syst_start_','pfs')))

### Patients who started with (palliative) systemic therapy? (WIP)
ncr_outcome <- ncr %>% dplyr::filter(tumgericht_ther==1 & (chemo==4 | target==4)) %>% select(all_of(df_easy))

## Trifluridine exploration (WIP)
atc_trifluridine <- "L01BC59"

ncr_trifluridine <- ncr %>%
  dplyr::filter(syst_code1==atc_trifluridine | syst_code2==atc_trifluridine | syst_code3==atc_trifluridine | syst_code4==atc_trifluridine | syst_code5==atc_trifluridine | syst_code6==atc_trifluridine | syst_code7==atc_trifluridine | syst_code8==atc_trifluridine | syst_code9==atc_trifluridine | syst_code10==atc_trifluridine | syst_code11==atc_trifluridine | syst_code12==atc_trifluridine) %>%
  select(c('key_nkr','epis'),matches(c('vit_')),c('syst_code1','syst_code2','syst_code3','syst_code4','syst_code5','syst_code6','syst_code7','syst_code8','syst_code9','syst_code10','syst_code11','syst_code12'),matches(c('syst_schemanum','syst_start_','pfs')))



