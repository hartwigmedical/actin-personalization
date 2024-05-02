find_similar_patients <- function(ncr_ref_data, patient_age, patient_who) {
  
  ## Rules:
  ##  - Age within one year of patient and matching WHO status (perf_stat)
  ##  - DIA episodes where metastases were present at start of episode
  ##  - No prior malignancies (mal1_int NA)
  ##  - Adenocarcinoma (morf_cat == 1) without any other tumors (dubbeltum == 0)
  ##  - Has had treatment (tumgericht_ther == 1)
  
  out <- ncr_ref_data %>% dplyr::filter( epis =='DIA' &
                                         meta_epis == 1 &
                                         topo_sublok == 'C209' &
                                         morf_cat == 1 &
                                         meta_topo_sublok1 == 'C220' &
                                         meta_topo_sublok2 == "" &
                                         pos_lymf == 0 &
                                         tumgericht_ther == 1 &
                                         (chemo == 4 | target == 4) &
                                         #respons_uitslag %in% c('CR','MR','PD','PR','SD')
                                         #pfs_event1 %in% c('0','1','2')
                                         leeft >= patient_age - 5 &
                                         leeft <= patient_age + 5 
                                         #perf_stat == patient_who
                                         )

  return(out)
}

find_similar_patients_2 <- function(ncr_ref_data, patient_age, patient_who, patient_has_had_surgery, patient_morf_cat, patient_topo_sublok, patient_meta_topo_sublok1) {
  
  ## Rules:
  ##  - Age within one year of patient and matching WHO status (perf_stat)
  ##  - DIA episodes where metastases were present at start of episode
  ##  - Adenocarcinoma (morf_cat == 1)
  ##  - Has had systemic treatment (tumgericht_ther == 1)
  ##  - Only select patients who received systemic therapy after surgery (if pt had surgery) or had no surgery (if pt had no surgery)
  
 # if (patient_has_had_surgery == 1) {
 #   code <- 2
 # } else if (patient_has_had_surgery == 0) {
 #   code <- 4
 # } else {
 #   stop("patient_has_had_surgery not set or inadequately set")
 # }
    
  
  out <- ncr_ref_data %>% dplyr::filter(epis == 'DIA' &
                                          meta_epis == 1 &
                                          morf_cat == patient_morf_cat &
                                          topo_sublok == patient_topo_sublok &
                                          meta_topo_sublok1 %in% patient_meta_topo_sublok1 &
                                          meta_topo_sublok2 == "" &
                                          pos_lymf == 0 
                                          #tumgericht_ther == 1 &
                                          #(chemo == code | target == code) &
                                          #leeft >= patient_age - 2 &
                                          #leeft <= patient_age + 2 &
                                          #perf_stat == patient_who
                                          )

  return(out)
}

find_similar_patients_3 <- function(ncr_ref_data, patient_age, patient_who, patient_has_had_surgery, patient_morf_cat, patient_topo_sublok, patient_meta_topo_sublok1) {
  
  ## Rules:
  ##  - Age within one year of patient and matching WHO status (perf_stat)
  ##  - DIA episodes where metastases were present at start of episode
  ##  - Adenocarcinoma (morf_cat == 1)
  ##  - Has had systemic treatment (tumgericht_ther == 1)
  ##  - Only select patients who received systemic therapy after surgery (if pt had surgery) or had no surgery (if pt had no surgery)
  
 # if (patient_has_had_surgery == 1) {
 #   code <- 2
 # } else if (patient_has_had_surgery == 0) {
 #   code <- 4
 # } else {
 #   stop("patient_has_had_surgery not set or inadequately set")
 # }
    
  
  out <- ncr_ref_data %>% dplyr::filter(epis == 'DIA' &
                                          meta_epis == 1 &
                                          morf_cat == patient_morf_cat &
                                          topo_sublok == patient_topo_sublok &
                                          meta_topo_sublok1 %in% patient_meta_topo_sublok1 &
                                          meta_topo_sublok2 == "" &
                                          pos_lymf == 0 &
                                          #tumgericht_ther == 1 &
                                          #(chemo == code | target == code) &
                                          leeft >= patient_age - 2 &
                                          leeft <= patient_age + 2 &
                                          perf_stat == patient_who
                                          )

  return(out)
}