## ACTIN-Personalization - Datamodel

This module contains the datamodel that is exposed from this project to downstream users

### High-level Data Structure

A `ReferenceEntry` is the highest level object and represents a single diagnosis, treatment and followup of a tumor. 

Note that in theory multiple reference entries can belong to the same patient (in case one patient has had multiple independent tumors)

### Treatment Extraction

The `Treatment` property of a systemic treatment is determined as follows:
1. The set of drugs used in the initial treatment scheme is captured
2. The set of follow-up drugs used in all subsequent schemes is captured.
3. If the set of follow-up drugs includes any new drugs that aren't allowed substitutions, assign `Treatment.OTHER`.
   Otherwise, identify the treatment represented by the initial drug set.

This allows some drugs to be dropped after the initial cycle while still using the initial drug set for treatment assignment.

(Note: for NCR this is currently implemented as part of the [NCR Ingestion](../ncr))
