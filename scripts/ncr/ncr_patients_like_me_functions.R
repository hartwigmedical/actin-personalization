find_similar_patients <- function(ncr_ref_data, patient_age, patient_who) {
  
  ## Rules:
  ##  - Age within one year of patient and matching WHO status (perf_stat)
  ##  - DIA episodes where metastases where present at start of episode
  ##  - No prior malignancies (mal1_int NA)
  ##  - Adenocarcinoma (morf_cat == 1) without any other tumors (dubbeltum == 0)
  ##  - Has had treatment (tumgericht_ther == 1)
  
  out <- ncr_ref_data %>% dplyr::filter(epis == 'DIA' &
                                         meta_epis == 1 &
                                         is.na(mal1_int) &
                                         morf_cat == 1 &
                                         dubbeltum == 0 &
                                         tumgericht_ther == 1 &
                                         leeft >= patient_age - 1 &
                                         leeft <= patient_age + 1 &
                                         perf_stat == patient_who)

  return(out)
}

