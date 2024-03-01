library(dplyr)

ncr <- read.csv(paste0(Sys.getenv("HOME"), "/hmf/tmp/K23244.csv"), sep=";")

ncr %>% group_by(epis, meta_epis) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))
ncr %>% group_by(epis, teller) %>% summarise(count_key_nkr=n(), distinct_count_key_nkr=n_distinct(key_nkr))

c(min(ncr$leeft), max(ncr$leeft), median(ncr$leeft))
hist(ncr$leeft, breaks=(max(ncr$leeft)-min(ncr$leeft)))

ncr %>% group_by(vit_stat, epis) %>% summarise(count=n())
ncr %>% group_by(perf_stat) %>% summarise(count=n())

