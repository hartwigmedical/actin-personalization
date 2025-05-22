## Database

This module writes a personalization reference database to a MySQL instance. In addition to writing the datamodel itself, the database
application creates a "reference" table which is a table of flattened reference entries. This table is meant to serve as the starting point
for learning algorithms.

This application requires Java 17+ and can be run as follows:

```
java -cp actin-personalizaton.jar com.hartwig.actin.personalization.database.PersonalizationLoaderApplicationKt \
   -reference_entry_json /path/to/reference_entry_json.json \
   -db_user ${sql_user} -db_pass ${sql_pass} -db_url ${sql_url}
```

The `reference_entry_json` can be created from input sources, see for example [NCR](../ncr) module.

### Creation of the flattened reference table

A number of key properties are derived from a `ReferenceEntry` as follows

| Property                                       | Description                                                                                                                                                                          |
|------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `daysBetweenPrimaryAndMetastaticDiagnosis`     | For synchronous entries this field is set to 0. For metachronous entries, this field is set to the `daysSinceDiagnosis` of the first metastasis defined in the `metastaticDiagnosis` |
| `metastaticTreatmentEpisode`                   | This is defined as the first treatment episode where metastases were present `AT_START`.                                                                                             |
| `daysBetweenPrimaryDiagnosisAndTreatmentStart` | This is defined as the smallest `daysBetweenDiagnosisAndStart` of all systemic treatments that are given as part of the `metastaticTreatmentEpisode`                                 |

Reference entries are filtered in the following cases:

- `daysBetweenPrimaryAndMetastaticDiagnosis` is not defined: Since we care about metastatic reference entries only, every entry to learn
  from should have a clearly defined moment of becoming metastatic.
- `metastaticTreatmentEpisode` is not defined: To learn from an entry, it must have a recorded treatment episode that started after
  metastatic diagnosis. Note that this can still contain entries without treatment, in this case the reason to refrain from treatment must
  have been recorded after metastatic diagnosis

All other fields of the `reference` table are either trivially derived from the underlying datamodel, or via a simple algo in the following
cases:

| Property                          | Description                                                                                                                                                                          |
|-----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `survivalDaysSinceTreatmentStart` | For patients who have been systemically treated after metastatic diagnosis, field is defined as `survivalSincePrimaryDiagnosis` minus `daysBetweenPrimaryDiagnosisAndTreatmentStart` |
| etc                               | etc                                                                                                                                                                                  | 




