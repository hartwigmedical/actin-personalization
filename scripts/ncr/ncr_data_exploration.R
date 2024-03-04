library(dplyr)

rm(list=ls())
ncr <- read.csv(paste0(Sys.getenv("HOME"), "/hmf/tmp/K23244.csv"), sep=";")

## Keys, epis, meta_epis, teller
ncr %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr), distinct_count_key_zid=n_distinct(key_zid), distinct_count_key_eid=n_distinct(key_eid))

ncr %>% group_by(epis) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, meta_epis) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, teller) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, meta_epis, teller) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))

## epis=DIA
ncr_dia <- ncr %>% dplyr::filter(epis=='DIA') %>% arrange(key_nkr)
ncr_dia %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))

pts_with_key_occurring_more_than_once_dia <- names(table(ncr_dia$key_nkr)[table(ncr_dia$key_nkr)>1]) 
ncr_dia_pts_multiple_dia <- ncr_dia[ncr_dia$key_nkr %in% pts_with_key_occurring_more_than_once_dia, ]
n_distinct(ncr_dia_pts_multiple_dia$key_nkr)

## epis=VERB
ncr_verb <- ncr %>% dplyr::filter(epis=='VERB') %>% arrange(key_nkr)
ncr_verb %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))

pts_with_key_occurring_more_than_once_verb <- names(table(ncr_verb$key_nkr)[table(ncr_verb$key_nkr)>1]) 
ncr_verb_pts_multiple_verb <- ncr_verb[ncr_verb$key_nkr %in% pts_with_key_occurring_more_than_once_verb, ]
pts_with_verb <- names(table(ncr_verb$key_nkr)) 
ncr_dia_pts_with_verb <- ncr_dia[ncr_dia$key_nkr %in% pts_with_verb, ]

## Other data exploration
c(min(ncr$leeft), max(ncr$leeft), median(ncr$leeft))
hist(ncr$leeft, breaks=(max(ncr$leeft)-min(ncr$leeft)))

ncr %>% group_by(epis, vit_stat) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, perf_stat) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))

ncr %>% group_by(epis, asa) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, cci) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
hist(ncr$cci, breaks=(max(ncr$cci, na.rm=T)-min(ncr$cci, na.rm=T)))

ncr %>% group_by(epis, morf_cat) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
hist(ncr$cci, breaks=(max(ncr$cci, na.rm=T)-min(ncr$cci, na.rm=T)))

ncr %>% group_by(epis, msi_stat) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, braf_mut) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, ras_mut) %>% summarise(count=n(), distinct_count_key_nkr=n_distinct(key_nkr))

unknown_value = 9999
hist(ncr$prechir_cea[ncr$prechir_cea!=unknown_value], breaks = 100)
hist(ncr$postchir_cea[ncr$postchir_cea!=unknown_value], breaks = 100)
hist(ncr$ldh1[ncr$ldh1!=unknown_value], breaks = 100)
hist(ncr$af1[ncr$af1!=unknown_value], breaks = 100)
hist(ncr$neutro1[ncr$neutro1!=unknown_value], breaks = 100)
hist(ncr$albumine1[ncr$albumine1!=unknown_value], breaks = 100)
hist(ncr$leuko1[ncr$leuko1!=unknown_value], breaks = 100)


