# ACTIN Personalization

ACTIN-personalization is a system for ingesting and standardizing reference data with the purpose to create models for treatment decision
personalization for the next patient. In addition to the core datamodel and source mappings to the core datamodel, this repo contains the logic that creates the prediction models
along with various analysis notebooks.

More details on the following modules are available from the links below:

| Module                   | Description                                                                                                  |
|--------------------------|--------------------------------------------------------------------------------------------------------------|
| [Datamodel](datamodel)   | The definition of the common reference patient datamodel used by personalization                             | 
| [Database](database)     | Writing the common datamodel to MySQL including creating a flattened view for analysis                       |
| [NCR](ncr)               | Logic to inspect and convert NCR-specific data to the common personalization datamodel                       | 
| [Prediction](prediction) | Module containing the predictive algorithms                                                                  |
| [Similarity](similarity) | A module providing basic patient-like-me functionality, to compare a new patient with the reference database |
