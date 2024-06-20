find_similar_patients_general <- function(ref_data) {
  
  out <- ref_data %>% dplyr::filter(distantMetastasesStatus == 'AT_START' &
                                      !is.na(systemicTreatmentPlan) &
                                      hasHadPreSurgerySystemicChemotherapy == 0 &
                                      hasHadPostSurgerySystemicChemotherapy == 0 &
                                      hasHadPreSurgerySystemicTargetedTherapy == 0 &
                                      hasHadPostSurgerySystemicTargetedTherapy == 0)
  
  return(out)
}

find_similar_patients_age <- function(ref_data_general, patient_age, range) {
  
  out <- ref_data_general %>% dplyr::filter(ageAtDiagnosis >= patient_age - range &
                                              ageAtDiagnosis <= patient_age + range)
  
  return(out)
}

find_similar_patients_who <- function(ref_data_general, patient_who) {
  
  out <- ref_data_general %>% dplyr::filter(whoStatusPreTreatmentStart == patient_who)
  
  return(out)
}

find_similar_patients_ras <- function(ref_data_general, patient_ras_status) {
  
  out <- ref_data_general %>% dplyr::filter(hasRasMutation == patient_ras_status)
  
  return(out)
}

find_similar_patients_lesions <- function(ref_data_general, patient_lesions) {
  
  translation_vector <- c("Brain"="BRAIN", "Colon"="COLON", "Lung"="BRONCHUS_AND_LUNG", "Liver"="LIVER_AND_INTRAHEPATIC_BILE_DUCTS", "Lymph node"="LYMPH_NODES", "Other"="OTHER", "Peritoneal"="RETROPERITONEUM_AND_PERITONEUM")
  
  translate_lesions <- function(patient_lesions) {
    if (patient_lesions %in% names(translation_vector)) {
      return(translation_vector[patient_lesions])
    } else {
      return(NA) 
    }
  }
  
  translated_patient_lesions <- sapply(patient_lesions, translate_lesions)
  
  out <- ref_data_general %>%
    filter_lesions(lesion_list=translated_patient_lesions, column_name="metastasisLocationGroupsPriorToSystemicTreatment")
  
  return(out)
}


filter_lesions <- function(data, lesion_list, column_name) {
  
  sort_and_collapse <- function(x) {
    if (is.na(x) || x == "") {
      return(NA)
    } else {
      return(unique(sort(unlist(strsplit(x, ","))), collapse = ","))
    }
  }
  
  column_name_sym <- sym(column_name)  
  
  data <- data %>%
    mutate(sorted_lesions = sapply(!!column_name_sym, sort_and_collapse))
  
  if (is.null(lesion_list) || length(lesion_list) == 0) {
    filtered_data <- data %>%
      dplyr::filter(is.na(!!column_name_sym) | !!column_name_sym == "")
  } else {
    target <- paste(sort(lesion_list))
    filtered_data <- data %>%
      rowwise %>%
      dplyr::filter(all(target %in% sorted_lesions) && all(sorted_lesions %in% target))
  }
  
  filtered_data <- filtered_data %>%
    select(-sorted_lesions)
  
  return(filtered_data)
}

format_lesions <- function(lesions) {
  
  if (length(lesions) == 0) {
    return(paste("None"))
  } else if (length(lesions) == 1) {
    return(paste0(lesions, " only"))
  } else {
    return(paste(lesions, collapse = " & "))
  }
}  







