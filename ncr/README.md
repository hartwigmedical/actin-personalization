## NCR

This module contains the datamodel mapping for data that was shared via the [NCR](https://iknl.nl/en/ncr) (Netherlands Cancer Registry)

The NCR ingestion application requires Java 17+ and can be run as follows:

```
java -cp actin.jar com.hartwig.actin.personalization.ncr.NcrIngestionApplication \
   -ncr_file /path/to/ncr_file.csv \
   -output_fle /path/to/output_reference_patients.json
```


