find_similar_patients <- function(ncr_ref_data, patient_age, patient_who) {
  
  ## Rules:
  ##  - Age within one year of patient and matching WHO status (perf_stat)
  ##  - DIA episodes where metastases were present at start of episode
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

find_similar_patients_2 <- function(ncr_ref_data, patient_age, patient_who, patient_has_had_surgery) {
  
  ## Rules:
  ##  - Age within one year of patient and matching WHO status (perf_stat)
  ##  - DIA episodes where metastases were present at start of episode
  ##  - Adenocarcinoma (morf_cat == 1)
  ##  - Has had systemic treatment (tumgericht_ther == 1)
  ##  - Only select patients who received systemic therapy after surgery (if pt had surgery) or had no surgery (if pt had no surgery)
  
  if (patient_has_had_surgery == 1) {
    code <- 2
  } else if (patient_has_had_surgery == 0) {
    code <- 4
  } else {
    stop("has_had_chemotherapy not set or inadequately set")
  }
  
  out <- ncr_ref_data %>% dplyr::filter(epis == 'DIA' &
                                          meta_epis == 1 &
                                          morf_cat == 1 &
                                          tumgericht_ther == 1 &
                                          (chemo == code || target == code) &
                                          leeft >= patient_age - 1 &
                                          leeft <= patient_age + 1 &
                                          perf_stat == patient_who)

  return(out)
}
