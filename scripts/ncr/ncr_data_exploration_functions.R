concat_syst_code_values <- function(data) {
  
  out <- data.frame(
    line1 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 1 & data$code_schemanum_value == data$schemanum_code_value, "syst_code_value"]), collapse = ", ")),
    line2 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 2 & data$code_schemanum_value == data$schemanum_code_value, "syst_code_value"]), collapse = ", ")),
    line3 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 3 & data$code_schemanum_value == data$schemanum_code_value, "syst_code_value"]), collapse = ", ")),
    line4 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 4 & data$code_schemanum_value == data$schemanum_code_value, "syst_code_value"]), collapse = ", ")),
    line5 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 5 & data$code_schemanum_value == data$schemanum_code_value, "syst_code_value"]), collapse = ", ")),
    line6 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 6 & data$code_schemanum_value == data$schemanum_code_value, "syst_code_value"]), collapse = ", ")),
    line7 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 7 & data$code_schemanum_value == data$schemanum_code_value, "syst_code_value"]), collapse = ", "))
    )
  
  return(out)
}

translate_atc <- function(atc_codes) {
  
  atc_translation_table <- list("L01BC06" = "Capecitabine", "L01XA03" = "Oxaliplatin", "L01FG01" = "Bevacizumab", "L01BC02" = 'Fluorouracil', "L01CE02" = 'Irinotecan')
  atc_codes <- unlist(strsplit(atc_codes, ", "))
  
  translated_atc_codes <- lapply(atc_codes, function(atc) {
    if (atc %in% names(atc_translation_table)) {
      return(atc_translation_table[[atc]])
    } else {
      return(atc)
    }
  })
  return(paste(translated_atc_codes, collapse = ", "))
}
