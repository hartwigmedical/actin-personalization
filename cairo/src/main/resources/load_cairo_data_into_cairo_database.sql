USE actin_cairo;

LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C2_patient.csv'
INTO TABLE C2_patient
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C2_labvalues.csv'
INTO TABLE C2_labvalues
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C2_cycles.csv'
INTO TABLE C2_cycles
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C2_mutations.csv'
INTO TABLE C2_mutations
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C2_outcome.csv'
INTO TABLE C2_outcome
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C3_patient.csv'
INTO TABLE C3_patient
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C3_endtreatment.csv'
INTO TABLE C3_endtreatment
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C3_outcome.csv'
INTO TABLE C3_outcome
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C3_tumor.csv'
INTO TABLE C3_tumor
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C3_mutations.csv'
INTO TABLE C3_mutations
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;


LOAD DATA LOCAL INFILE '/data/experiments/250228_hh_load_actin_cairo_database/csv/C3_labvalues.csv'
INTO TABLE C3_labvalues
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

