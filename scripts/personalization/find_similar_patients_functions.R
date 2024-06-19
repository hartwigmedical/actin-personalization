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

find_similar_patients_lesions <- function(ref_data, patient_lesions) {
  
  out <- ref_data %>% 
    dplyr::filter(distantMetastasesStatus == 'AT_START' &
                 !is.na(systemicTreatmentPlan) &
                  is.na(surgeries)) %>%
    filter_lesions(lesion_list=patient_lesions, column_name="metastasisLocationGroupsPriorToSystemicTreatment")
  
  return(out)
}


filter_lesions <- function(data, lesion_list, column_name) {

    sort_and_collapse <- function(x) {
    if (is.na(x) || x == "") {
      return(NA)
    } else {
      return(paste(sort(unlist(strsplit(x, ","))), collapse = ","))
    }
  }
  
  column_name_sym <- sym(column_name)  
    
  data <- data %>%
    mutate(sorted_lesions = sapply(!!column_name_sym, sort_and_collapse))
  
  if (is.null(lesion_list) || length(lesion_list) == 0) {
    filtered_data <- data %>%
      dplyr::filter(is.na(!!column_name_sym) | !!column_name_sym == "")
  } else {
    target <- paste(sort(lesion_list), collapse = ",")
    filtered_data <- data %>%
      dplyr::filter(sorted_lesions == target)
  }
  
  filtered_data <- filtered_data %>%
    select(-sorted_lesions)
  
  return(filtered_data)
}








