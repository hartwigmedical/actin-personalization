find_similar_patients_general <- function(ref_data) {
  
  out <- ref_data %>% dplyr::filter(distantMetastasesStatus == 'AT_START' &
                                    !is.na(systemicTreatmentPlan) &
                                    is.na(surgeries))
  
  return(out)
}

find_similar_patients_age <- function(ref_data, patient_age, range) {
  
  out <- ref_data %>% dplyr::filter(distantMetastasesStatus == 'AT_START' &
                                    !is.na(systemicTreatmentPlan) &
                                    is.na(surgeries) &
                                    ageAtDiagnosis >= patient_age - range &
                                    ageAtDiagnosis <= patient_age + range)
  
  return(out)
}

find_similar_patients_who <- function(ref_data, patient_who) {
  
  out <- ref_data %>% dplyr::filter(distantMetastasesStatus == 'AT_START' &
                                      !is.na(systemicTreatmentPlan) &
                                      is.na(surgeries) &
                                      whoStatusPreTreatmentStart == patient_who)
  
  return(out)
}

find_similar_patients_ras <- function(ref_data, patient_ras_status) {
  
  out <- ref_data %>% dplyr::filter(distantMetastasesStatus == 'AT_START' &
                                      !is.na(systemicTreatmentPlan) &
                                      is.na(surgeries) &
                                      hasRasMutation == patient_ras_status)
  
  return(out)
}
