## ACTIN-Personalization - Database

This module writes a personalization reference database to MySQL. In addition to writing the datamodel itself, the application creates
the `reference` table containing a flattened view on reference entries. This table is meant to serve as the starting point for training
prediction models and other types of analyses.

This application requires Java 17+ and can be run as follows:

```
java -cp actin-personalizaton.jar com.hartwig.actin.personalization.database.PersonalizationLoaderApplicationKt \
   -reference_entry_json /path/to/reference_entry_json.json \
   -db_user ${sql_user} -db_pass ${sql_pass} -db_url ${sql_url}
```

The `reference_entry_json` should be created from single or multiple input sources, see for example [NCR Ingestion](../ncr).

### Creation of the flattened reference table

A number of key properties are derived from a `ReferenceEntry` as follows:

| Property                                       | Description                                                                                                                                                                          |
|------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `daysBetweenPrimaryAndMetastaticDiagnosis`     | For synchronous entries this field is set to 0. For metachronous entries, this field is set to the `daysSinceDiagnosis` of the first metastasis defined in the `metastaticDiagnosis` |
| `metastaticTreatmentEpisode`                   | This is defined as the first treatment episode where metastases were present `AT_START` or `AT_PROGRESSION`.                                                                         |
| `daysBetweenPrimaryDiagnosisAndTreatmentStart` | This is defined as the lowest `daysBetweenDiagnosisAndStart` of all systemic treatments that are given as part of the `metastaticTreatmentEpisode`                                   |

Reference entries are filtered in case `daysBetweenPrimaryAndMetastaticDiagnosis` is not defined: Since we care about metastatic reference
entries only, every entry in the flattened table should have a clearly defined moment of becoming metastatic.

All other fields of the `reference` table are either trivially derived from the underlying datamodel, or via a simple algo in the following
cases:

| Property                          | Description                                                                                                                                                                                                      |
|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `survivalDaysSinceTreatmentStart` | For entries with systemic treatment after metastatic diagnosis, this field is defined as ```survivalSincePrimaryDiagnosis - daysBetweenPrimaryDiagnosisAndTreatmentStart```                                      |
| `hadProgressionEvent`             | This indicates whether a patient experienced a progression event during a study. `false` here means that the patient was censored, or no progression measures were recorded even though a treatment was started. |

### Creation of further filtered views

For specific analyses, further filtering can be helpful. To facilitate common filtering, two views are available derived from
the `reference` table:

- `palliativeReference` containing all reference entries in a palliative setting, with the following conditions:
    - has not had systemic treatment prior to metastatic diagnosis
    - is metastatic prior to metastatic treatment (as opposed to "during")
    - either has TNM status of M1 (either clinical or pathological), or tumor stage IV (clinical or pathological)
    - has not had primary surgery during metastatic treatment
    - had not had gastroenterology surgery during metastatic treatment
    - has not had HIPEC during metastatic treatment
    - has not had primary radiotherapy during metastatic treatment
    - has not had metastatic surgery
    - has not had metastatic radiotherapy
- `knownPalliativeTreatedReference` containing all entries in palliative setting that received a known treatment, with following conditions:
    - first systemic treatment after metastatic diagnosis is known and not `OTHER`



 
