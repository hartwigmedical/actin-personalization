library(dplyr)
library(tibble)

rm(list=ls())

# Retrieve data ------------------------------------------------------------------
dbActinPersonalization <- dbConnect(MySQL(), dbname='actin_personalization', groups="RAnalysis")

query_ref <-"select * from diagnosisTreatments;"
ref <- dbGetQuery(dbActinPersonalization, query_ref)
dbDisconnect(dbActinPersonalization)

# Find similar patients -----------------------------------------------------------
ref_general <- find_similar_patients_general(ref)

patient_age=X
ref_age <- find_similar_patients_age(ref, patient_age=patient_age, range=5)

patient_who=X
ref_who <- find_similar_patients_who(ref, patient_who=patient_who)

patient_ras_status=X
ref_ras <- find_similar_patients_ras(ref, patient_ras_status=patient_ras_status)



