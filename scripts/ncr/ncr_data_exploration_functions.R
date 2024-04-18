load_ncr_data <- function() {
    
  ncr <- read.csv(paste0(Sys.getenv("HOME"), "/hmf/tmp/ncr_crc_dataset.csv"), sep = ";")

  return(ncr)
}

load_ncr_data_notebook <- function() {
    
  ncr <- read.csv(paste0("/data/patient_like_me/ncr/K23244.csv"), sep = ";")

  return(ncr)
}

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

concat_start_int_values <- function(data) {

  out <- data.frame(
    line_start_1 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 1 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    line_start_2 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 2 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    line_start_3 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 3 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    line_start_4 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 4 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    line_start_5 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 5 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    line_start_6 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 6 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    line_start_7 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 7 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", "))
  )

  return(out)
}

concat_stop_int_values <- function(data) {

  out <- data.frame(
    line_stop_1 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 1 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    line_stop_2 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 2 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    line_stop_3 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 3 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    line_stop_4 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 4 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    line_stop_5 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 5 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    line_stop_6 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 6 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    line_stop_7 = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 7 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", "))
  )

  return(out)
}

extract_min <- function(string) {

  if (string == "") {
    return("")
  } else if (grepl(":", string)) {
    numbers <- as.integer(strsplit(string, ":")[[1]])
    return(min(numbers))
  } else {
    numbers <- as.integer(strsplit(string, ",")[[1]])
    return(min(numbers))
  }
}

extract_max <- function(string) {

  if (string == "") {
    return("")
  } else if (grepl(":", string)) {
    numbers <- as.integer(strsplit(string, ":")[[1]])
    return(max(numbers))
  } else {
    numbers <- as.integer(strsplit(string, ",")[[1]])
    return(max(numbers))
  }
}


translate_atc <- function(atc_codes) {

  atc_translation_table <- list("L01BC06" = "Capecitabine",
                                "L01XA01" = "Cisplatin",
                                "L01XA02" = "Carboplatin",
                                "L01XA03" = "Oxaliplatin",
                                "L01CD01" = "Paclitaxel",
                                "L01BC05" = "Gemcitabine",
                                "L01BC02" = "Fluorouracil",
                                "L01CE02" = "Irinotecan",
                                "L01BC53" = "Tegafur/gimeracil/oteracil",
                                "L01BC59" = "Trifluridine/tipiracil",
                                "420000" = "Systemic chemotherapy",
                                "422000" = "Platinum chemotherapy",
                                "690420" = "Chemotherapy abroad",
                                "L01F" = "Monoclonal antibodies",
                                "L01FG01" = "Bevacizumab",
                                "L01FE02" = "Panitumumab",
                                "L01FE01" = "Cetuximab",
                                "L01EC03" = "Encorafenib",
                                "L01EE03" = "Binimetinib",
                                "L01FF01" = "Nivolumab",
                                "L01FF02" = "Pembrolizumab",
                                "L01FX04" = "Ipilimumab"
  )
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

