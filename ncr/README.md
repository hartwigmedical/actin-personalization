## ACTIN-Personalization - NCR

This module contains the datamodel mapping for data provided by [NCR](https://iknl.nl/en/ncr) (Netherlands Cancer Registry)

The NCR ingestion application requires Java 17+ and can be run as follows:

```
java -cp actin.jar com.hartwig.actin.personalization.ncr.NcrIngestionApplication \
   -ncr_file /path/to/ncr_file.csv \
   -output_file /path/to/output_reference_entry.json
```

In addition, the NCR inspection application can be used to generate an overview of an NCR dataset on command line. This application requires
Java 17+ and can be run as follows:

```
java -cp actin.jar com.hartwig.actin.personalization.ncr.NcrInspectionApplication \
   -ncr_file /path/to/ncr_file.csv
```

### NCR datamodel schematic

The NCR data is organized by patient, then by tumor. Data is collected per episode with at least a DIA (diagnosis) episode for every tumor,
and potentially multiple followup episodes per patient. The datamodel collected is identical per episode.

![NCR Datamodel Schematic](/src/main/resources/ncr_datamodel_schematic.png)

### Filtering NCR data

The ingestion application starts with filtering NCR records that are for whatever reason considered unreliable. Individual episodes are
checked for reliability and tumor entries are filtered in entirety if a single episode for that tumor fails any reliability check.

The parameter `-log-filtered-records` can be passed to log all filtered records via commandline with a reference to the tumor id (`key_zid`)

The following records are deemed unreliable and are removed prior to ingestion:

- Any record with indication of treatment (`tumgericht_ther == 1`) but having no actual treatment defined (no primary surgery, no primary
  radiotherapy, no gastroenterology surgery, no HIPEC, no systemic chemo, no systemic targeted therapy, no primary radiotherapy, no
  metastatic surgery or radiotherapy)

The following assumptions are made about the NCR data _but are currently not explicitly filtered out upon violation_

- Patient/General properties:
    - `gesl` (sex) is identical for all records belonging to same patient (`key_nkr`)
    - There is exactly one DIA episode per tumor (`key_zid`)
    - `inc_jr` (year of incidence) is equal between all records with identical `key_zid`
    - `vit_stat_int` and `vit_stat` (vital status) are always available for DIA records and always null for VERB records
- Prior tumors:
    - prior tumors are only defined on DIA episodes and are always null in VERB episodes
    - `mal1_int` (e.t.c.) are never positive numbers
    - `mal1_syst` (and 2/3/4) are trivially derivable from the drug list.
    - Every prior molecular tumor has at least an interval (`int`), type (`morf`), location (`topo_sublok`), category (`tum_soort`) and
      systemic
      treatment (`syst`), only `stadium` (stage) is optional.
- Primary diagnosis:
    - Has double tumor (`dubbeltum`) is always present for DIA and always 0 for any VERB episode and can be ignored.
    - primaryTumorType (`morf_cat`) is always present for DIA and always null for VERB.
    - primaryTumorLocation (`topo_sublok`) is always equal for DIA and VERB episodes of the same `key_zid`
    - Anorectal verge distance (`anus_afst`) is only populated in DIA episodes and always null for VERB episodes.
- Metastatic diagnosis
    - There is only ever one episode with metastatic detection either `AT_START` or `AT_PROGRESSION`. This episode contains all the
      information and interventions done during or after metastatic diagnosis.
    - Episodes with metastatic detection `ABSENT` (meta_epis = 0) have all `meta_` fields null.
    - Every metastasis is linked to progression in case the episode has metastatic detection `AT_PROGRESSION` (meta_epis = 2). For every
      metastasis this is false when `AT_START` (meta_epis = 1)
- Comorbidities:
    - All comorbidity fields are only ever populated for DIA episodes and always null for VERB episodes.
    - If the field `cci` is populated, then all other cci fields are present as well.
- Molecular results:
    - All fields (`braf_mut`, `ras_mut`, `msi_stat`) are potentially present during DIA but always null at VERB
- Lab measurements:
    - If no interval has been provided we can take the minimum days since diagnosis for the record.
    - All lab measures are only included if they are below an upper bound.
- Treatment:
    - `geen_ther_reden` (reason for refraining from treatment) is always null in case a patient got treatment and always not-null in other
      cases.
    - It is assumed that types of treatments can be resolved from actual treatments. The following fields are assumed to be redundant:
        - `mdl_res` in gastroenterology resection.
        - `chir` in primary surgery
        - `rt` and `chemort` in primary radiotherapy
        - `chemo` in systemic treatment
        - `target` in systemic treatment
    - It is assumed that when `rt` and `chemort` imply pre- or post-surgery only, their intervals match with expected from chir.
    - It is assumed that when a `syst_code` is provided, there is also a `syst_schemanum`

### Data dropped from NCR

- For various fields relevant for primary diagnosis, the data is only taken from DIA episodes and ignored from further episodes (even if it
  differs between DIA and VERB episodes):
    - `diag_basis`: basis of diagnosis
    - `mrf_afst`: distance to mesorectal fascia
    - `diffgrad`: differentiation grade
    - `venous_invas`: venous invasion
    - `lymf_invas`: lymphatic invasion
    - `emi`: extra-mural invasion
    - `tumregres`: tumor regression
    - `ileus`: presented with ileus?
    - `perforation`: presented with perforation?
- Metastatic diagnosis:
    - Number and size of liver metastases are not-null in rare (<10) occasions when meta_epis = 0. This is dropped.
- Treatments:
    - `deelname_studie` (has participated in trial?) is dropped since it is too abstract (not clear what trial, or even what kind of trial (
      surgery. systemic trial etc.)).

Finally, the `stadium` field is considered irrelevant and dropped (typically the worst of clinical and pathological stage).

### Data issues in NCR

- `diffgrad` is documented as “tekst” in the NCR data dictionary even though it is actually an int (code).
- `meta_lever_aantal` and `meta_lever_afm` are not-null in rare (<10) occasions when meta_epis = 0
