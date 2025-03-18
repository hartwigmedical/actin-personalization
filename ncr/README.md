## NCR

This module contains the datamodel mapping for data that was shared via the [NCR](https://iknl.nl/en/ncr) (Netherlands Cancer Registry)

The NCR ingestion application requires Java 17+ and can be run as follows:

```
java -cp actin.jar com.hartwig.actin.personalization.ncr.NcrIngestionApplication \
   -ncr_file /path/to/ncr_file.csv \
   -output_fle /path/to/output_reference_patients.json
```

### Assumptions about the NCR data

- Patient/General properties:
    - Sex is identical for all records belonging to same keyNkr
    - There is a single DIA episode per keyZid
    - primaryDiagnosis.incjr is equal between all records with identical keyZid
    - vitStatInt and vitStat are always available for DIA records and always null for VERB records
- Prior tumors:
    - prior tumors are only defined on DIA episodes and are always null in VERB episodes
    - mal1Int (etc) are never positive numbers (related: it can be 0, what does that mean?)
    - mal1Syst (and 2/3/4) are trivially derivable from the drug list.
    - Every prior molecular tumor has at least an interval (int), type (morf), location (topo_sublok), category (tumSoort) and systemic
      treatment (syst) -> only stage is optional.
- Primary diagnosis:
    - Has double tumor (dubbeltum) is always present for DIA and always 0 for any VERB episode and can be ignored.
    - primaryTumorType (morf_cat) is always present for DIA and always null for VERB.
    - primaryTumorLocation (topo_sublok) is always equal for DIA and VERB episodes of the same keyZid
    - Anorectal verge distance (“anus_afst”) is only populated in DIA episodes and always NULL for VERB episodes.
    - Clinical and pathological stage (cstadium and pstadium) are always populated in DIA episodes, and always NULL for VERB episodes.
- Metastatic diagnosis
    - There is only ever one episode with metastatic detection either AT_START or AT_PROGRESSION. This episode contains all the metastatic
      details.
    - Episodes with metastatic detection ABSENT (meta_epis = 0) have all “meta_” fields null.
        - To check: Every metastasis is linked to progression in case the episode has metastatic detection AT_PROGRESSION (meta_epis=2). For
          every metastasis this is false when AT_START (meta_epis=1)
- Comorbidities:
    - All comorbidity fields are only ever populated for DIA episodes and always null for VERB episodes.
    - If the field “cci” is populated, then all other cci fields are present as well.
- Molecular results:
    - All fields (braf_mut, ras_mut, msi_stat) are potentially present during DIA but always null at VERB
- Lab measurements:
    - If no interval has been provided we can take the minimum days since diagnosis for the record.
    - All lab measures are only included if they are below an upper bound.
- Treatment:
    - hasReceivedTumorDirectedTreatment (‘ ‘) can be trivially derived from the set of all treatments
    - reasonRefrainmentFromTreatment (‘ ‘) is always null in case a patient got treatment and always not-null in other cases.
    - It is assumed that types of treatments can be resolved from actual treatments. The following fields are assumed to be redundant:
        - “mdlRes” in gastroenterology resection.
        - “chir” in primary surgery
        - “rt” and “chemort” in primary radiotherapy
        - “chemo” in systemic treatment
        - “target” in systemic treatment
    - It is assumed that when “rt” and “chemort” imply pre- or post-surgery only, their intervals match with expected from chir.
    - It is assumed that when a syst_code is provided, there is also a syst_schemanum
        - Note: This is false in 1 EID case

### Data dropped from NCR

- Primary diagnosis
    - We only care about the basis of diagnosis of the DIA episode (“diag_basis”), not about any VERB episodes. even though they may differ
    - We only care about mesorectal fascia information (“mrf_afst”) in DIA episode and not in VERB
    - We only care about the differentiation grade of the DIA episode (“diffgrad”), not about any VERB episodes even though they may differ
    - We only care about clinical and pathological TNM of the DIA episode (ct/cn/cm and pt/pn/pm), not about any later TNMs.
    - The “stadium” field is irrelevant and dropped (typically the worst of clinical and pathological stage).
    - We only care about venous invasion, lymphatic invasion, extra-mural invasion, tumor regression from the DIA episodes (note: VERBs seem
      null generally but not always)
    - We only care about ileus and perforation for DIA episodes (note: only present in VERB in <50 of cases)
- Metastatic diagnosis:
    - Number and size of liver metastases are not-null in rare (<10) occasions when meta_epis = 0. This is dropped.
- Treatments:
    - hasParticipatedInTrial is dropped since it is too abstract (not clear what trial, or even what kind of trial (surgery. systemic
      trial etc.)).

### Data issues/questions in NCR

- diffgrad is documented as “tekst” in the NCR data dictionary even though it is actually an int (code).
- “meta_lever_aantal" and “meta_lever_afm" are not-null in rare (<10) occasions when meta_epis = 0
- What do ond_lymf and pos_lymf mean in the context of the episode in which metastases were discovered and not in DIA?
