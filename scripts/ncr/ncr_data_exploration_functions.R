load_ncr_data_latest <- function() {
  ncr <- read.csv(paste0(Sys.getenv("HOME"), "/hmf/tmp/ncr_crc_latest.csv"), sep = ";")

  return(ncr)
}

load_ncr_data_1 <- function() {
  ncr <- read.csv(paste0(Sys.getenv("HOME"), "/hmf/tmp/ncr_crc_dataset_1.csv"), sep = ";")

  return(ncr)
}

load_ncr_data_2 <- function() {
  ncr <- read.csv(paste0(Sys.getenv("HOME"), "/hmf/tmp/ncr_crc_dataset_2.csv"), sep = ";")

  return(ncr)
}

load_ncr_data_notebook_latest <- function() {
  ncr <- read.csv(paste0("/data/patient_like_me/ncr/latest/K2400223.csv"), sep = ";")

  return(ncr)
}

load_ncr_data_notebook_1 <- function() {
  ncr <- read.csv(paste0("/data/patient_like_me/ncr/1/K23244.csv"), sep = ";")

  return(ncr)
}

collect_all_treatments_written <- function(ncr) {
    
ncr_treatments_prep <- ncr %>%
  dplyr::filter(tumgericht_ther==1) %>%
  select(c('key_nkr','key_zid','key_eid'),starts_with(c('syst_schemanum','syst_code'))) %>%
  pivot_longer(cols = starts_with("syst_schemanum"), names_to = "syst_schemanum_key", values_to = "syst_schemanum_value") %>%
  pivot_longer(cols = starts_with("syst_code"), names_to = "syst_code_key", values_to = "syst_code_value")

ncr_treatments <- ncr_treatments_prep %>%
  add_column(schemanum_code_value = as.numeric(gsub("\\D", "", ncr_treatments_prep$syst_schemanum_key)), .after = 2) %>%
  add_column(code_schemanum_value = as.numeric(gsub("\\D", "", ncr_treatments_prep$syst_code_key)), .after = 6) %>%
  distinct() %>%
  group_by(key_nkr, key_zid, key_eid) %>%
  do(concat_syst_code_values(.)) %>%
  ungroup()

ncr_treatments[] <- lapply(ncr_treatments, function(x) gsub("^c\\((.*)\\)$", "\\1", x))
ncr_treatments[ncr_treatments == "character(0)"] <- ""

ncr_treatments_written <- ncr_treatments %>%
  add_column(treatment_plan_part_1_written = sapply(ncr_treatments$line1, translate_atc)) %>%
  add_column(treatment_plan_part_2_written = sapply(ncr_treatments$line2, translate_atc)) %>%
  add_column(treatment_plan_part_3_written = sapply(ncr_treatments$line3, translate_atc)) %>%
  add_column(treatment_plan_part_4_written = sapply(ncr_treatments$line4, translate_atc)) %>%
  add_column(treatment_plan_part_5_written = sapply(ncr_treatments$line5, translate_atc)) %>%
  add_column(treatment_plan_part_6_written = sapply(ncr_treatments$line6, translate_atc)) %>%
  add_column(treatment_plan_part_7_written = sapply(ncr_treatments$line7, translate_atc)) %>%
  select(matches("key") | matches("written")) 

ncr_treatments_written$key_nkr <- as.integer(ncr_treatments_written$key_nkr)
ncr_treatments_written$key_zid <- as.integer(ncr_treatments_written$key_zid)
ncr_treatments_written$key_eid <- as.integer(ncr_treatments_written$key_eid)
          
    return(ncr_treatments_written)
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

collect_all_treatments_start_and_end_dates <- function(ncr) {

ncr_treatments_start_prep <- ncr %>%
  dplyr::filter(tumgericht_ther==1) %>%
  select(c('key_nkr','key_zid', 'key_eid'),starts_with(c('syst_schemanum','syst_start_int'))) %>%
  pivot_longer(cols = starts_with("syst_schemanum"), names_to = "syst_schemanum_key", values_to = "syst_schemanum_value") %>%
  pivot_longer(cols = starts_with("syst_start_int"), names_to = "syst_start_int_key", values_to = "syst_start_int_value")

ncr_treatments_start <- ncr_treatments_start_prep %>%
  add_column(schemanum_line_start_value = as.numeric(gsub("\\D", "", ncr_treatments_start_prep$syst_schemanum_key)), .after = 2) %>%
  add_column(line_start_schemanum_value = as.numeric(gsub("\\D", "", ncr_treatments_start_prep$syst_start_int_key)), .after = 6) %>%
  distinct() %>%
  group_by(key_nkr, key_zid, key_eid) %>%
  do(concat_start_int_values(.)) %>%
  ungroup()

#Use min of start of individual compounds as start
ncr_treatments_start[] <- lapply(ncr_treatments_start, function(x) gsub("^c\\((.*)\\)$", "\\1", x))
ncr_treatments_start[ncr_treatments_start == "integer(0)"] <- ""
                                 
ncr_treatments_start$treatment_plan_part_1_start <- sapply(ncr_treatments_start$treatment_plan_part_1_start, extract_min)
ncr_treatments_start$treatment_plan_part_2_start <- sapply(ncr_treatments_start$treatment_plan_part_2_start, extract_min)
ncr_treatments_start$treatment_plan_part_3_start <- sapply(ncr_treatments_start$treatment_plan_part_3_start, extract_min)
ncr_treatments_start$treatment_plan_part_4_start <- sapply(ncr_treatments_start$treatment_plan_part_4_start, extract_min)
ncr_treatments_start$treatment_plan_part_5_start <- sapply(ncr_treatments_start$treatment_plan_part_5_start, extract_min)
ncr_treatments_start$treatment_plan_part_6_start <- sapply(ncr_treatments_start$treatment_plan_part_6_start, extract_min)
ncr_treatments_start$treatment_plan_part_7_start <- sapply(ncr_treatments_start$treatment_plan_part_7_start, extract_min)

ncr_treatments_start$key_nkr <- as.integer(ncr_treatments_start$key_nkr)
ncr_treatments_start$key_zid <- as.integer(ncr_treatments_start$key_zid)
ncr_treatments_start$key_eid <- as.integer(ncr_treatments_start$key_eid)

ncr_treatments_stop_prep <- ncr %>%
  dplyr::filter(tumgericht_ther==1) %>%
  select(c('key_nkr','key_zid', 'key_eid'),starts_with(c('syst_schemanum','syst_stop_int'))) %>%
  pivot_longer(cols = starts_with("syst_schemanum"), names_to = "syst_schemanum_key", values_to = "syst_schemanum_value") %>%
  pivot_longer(cols = starts_with("syst_stop_int"), names_to = "syst_stop_int_key", values_to = "syst_stop_int_value")

ncr_treatments_stop <- ncr_treatments_stop_prep %>%
  add_column(schemanum_line_stop_value = as.numeric(gsub("\\D", "", ncr_treatments_stop_prep$syst_schemanum_key)), .after = 2) %>%
  add_column(line_stop_schemanum_value = as.numeric(gsub("\\D", "", ncr_treatments_stop_prep$syst_stop_int_key)), .after = 6) %>%
  distinct() %>%
  group_by(key_nkr, key_zid, key_eid) %>%
  do(concat_stop_int_values(.)) %>%
  ungroup()

#Use max of stop of individual compounds as stop
ncr_treatments_stop[] <- lapply(ncr_treatments_stop, function(x) gsub("^c\\((.*)\\)$", "\\1", x))
ncr_treatments_stop[ncr_treatments_stop == "integer(0)"] <- ""
ncr_treatments_stop$treatment_plan_part_1_stop <- sapply(ncr_treatments_stop$treatment_plan_part_1_stop, extract_max)
ncr_treatments_stop$treatment_plan_part_2_stop <- sapply(ncr_treatments_stop$treatment_plan_part_2_stop, extract_max)
ncr_treatments_stop$treatment_plan_part_3_stop <- sapply(ncr_treatments_stop$treatment_plan_part_3_stop, extract_max)
ncr_treatments_stop$treatment_plan_part_4_stop <- sapply(ncr_treatments_stop$treatment_plan_part_4_stop, extract_max)
ncr_treatments_stop$treatment_plan_part_5_stop <- sapply(ncr_treatments_stop$treatment_plan_part_5_stop, extract_max)
ncr_treatments_stop$treatment_plan_part_6_stop <- sapply(ncr_treatments_stop$treatment_plan_part_6_stop, extract_max)
ncr_treatments_stop$treatment_plan_part_7_stop <- sapply(ncr_treatments_stop$treatment_plan_part_7_stop, extract_max)

ncr_treatments_stop$key_nkr <- as.integer(ncr_treatments_stop$key_nkr)
ncr_treatments_stop$key_zid <- as.integer(ncr_treatments_stop$key_zid)
ncr_treatments_stop$key_eid <- as.integer(ncr_treatments_stop$key_eid)
 
#Merge start and stop output    
ncr_treatments_start_and_stop <- inner_join(ncr_treatments_start, ncr_treatments_stop, by=c('key_nkr','key_zid','key_eid')) 
       
  return(ncr_treatments_start_and_stop)             
                                
}
                                
                           
concat_start_int_values <- function(data) {

  out <- data.frame(
    treatment_plan_part_1_start = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 1 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    treatment_plan_part_2_start = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 2 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    treatment_plan_part_3_start = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 3 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    treatment_plan_part_4_start = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 4 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    treatment_plan_part_5_start = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 5 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    treatment_plan_part_6_start = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 6 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", ")),
    treatment_plan_part_7_start = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 7 & data$line_start_schemanum_value == data$schemanum_line_start_value, "syst_start_int_value"]), collapse = ", "))
  )

  return(out)
}

concat_stop_int_values <- function(data) {

  out <- data.frame(
    treatment_plan_part_1_stop = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 1 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    treatment_plan_part_2_stop = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 2 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    treatment_plan_part_3_stop = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 3 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    treatment_plan_part_4_stop = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 4 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    treatment_plan_part_5_stop = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 5 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    treatment_plan_part_6_stop = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 6 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", ")),
    treatment_plan_part_7_stop = gsub('"', '', paste(na.omit(data[data$syst_schemanum_value == 7 & data$line_stop_schemanum_value == data$schemanum_line_stop_value, "syst_stop_int_value"]), collapse = ", "))
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
                                "L01CB01" = "Etoposide",
                                "L01BC05" = "Gemcitabine",
                                "L01BC02" = "Fluorouracil",
                                "L01CE02" = "Irinotecan",
                                "L01EC02" = "Dabrafenib",
                                "L01EE01" = "Trametinib",
                                "L01BC53" = "Tegafur/gimeracil/oteracil",
                                "L01BC59" = "Trifluridine/tipiracil",
                                "L03AB04" = "Interferon alpha 2a",
                                "L01BA04" = "Pemetrexed",
                                "L01DC03" = "Mytomycin",
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

treatment_plan_translation <- function(treatments) {

  treatment_plan_translation_table <- list("Capecitabine, Bevacizumab, Oxaliplatin" = "CAPOX-B",
                                            "Capecitabine" = "Capecitabine",
                                            "Capecitabine, Oxaliplatin" = "CAPOX",
                                            "Capecitabine, Bevacizumab" = "Capecitabine-B",
                                            "Fluorouracil, Bevacizumab, Oxaliplatin" = "FOLFOX-B",
                                            "Fluorouracil, Oxaliplatin" = "FOLFOX",
                                            "Fluorouracil, Irinotecan, Bevacizumab, Oxaliplatin" = "FOLFOXIRI-B",
                                            "Fluorouracil" = "Fluorouracil",
                                            "Pembrolizumab" = "Pembrolizumab",
                                            "Fluorouracil, Irinotecan, Bevacizumab" = "FOLFIRI-B",
                                            "Fluorouracil, Panitumumab, Oxaliplatin" = "FOLFOX-P",
                                            "Fluorouracil, Irinotecan, Oxaliplatin" = "FOLFOXIRI",
                                            "Fluorouracil, Irinotecan" = "FOLFIRI",
                                            "Systemic chemotherapy" = "Systemic chemotherapy",
                                            "Fluorouracil, Bevacizumab" = "Fluorouracil-B",
                                            "Systemic chemotherapy, Bevacizumab" = "Systemic chemotherapy-B"
  )

  treatment_plans <- lapply(treatments, function(treatments_raw) {
    if (treatments_raw %in% names(treatment_plan_translation_table)) {
      return(treatment_plan_translation_table[[treatments_raw]])
    } else {
      return(treatments_raw)
    }
  })
  return(treatment_plans)
}
                                
                                
