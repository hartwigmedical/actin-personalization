# ACTIN Personalization

ACTIN-personalization is a system for ingesting and standardizing reference real world patients, along with the logic to derive
personalization models from a reference database.

More details on the following modules are available from the links below:

| Module                   | Description                                                                                                  |
|--------------------------|--------------------------------------------------------------------------------------------------------------|
| [Datamodel](datamodel)   | The definition of the common reference patient datamodel used by personalization                             | 
| [Database](database)     | Writing the common datamodel to MySQL including creating a flattened view for analysis                       |
| [NCR](ncr)               | Logic to inspect and convert NCR-specific data to the common personalization datamodel                       | 
| [Prediction](prediction) | Module containing the predictive algorithms                                                                  |
| [Similarity](similarity) | A module providing basic patient-like-me functionality, to compare a new patient with the reference database |
