## Datamodel

This module contains the datamodel that is exposed from this project to downstream users

### High-level Data Structure

A `ReferencePatient` contains basic information and a list of tumor entries, each of which has a `Diagnosis` and a list
of `Episode`s.
Each episode includes an assessment of the tumor at that time and a treatment plan, captured in the
`systemicTreatmentPlan` field.

### Treatment Extraction

A `Treatment` is determined for treatment plan where possible by:
1. Identifying the set of drugs used in the initial treatment scheme.
2. Identifying the set of follow-up drugs used in all subsequent schemes.
3. If the set of follow-up drugs includes any new drugs that aren't allowed substitutions, assign `Treatment.OTHER`.
   Otherwise, identify the treatment represented by the initial drug set.

This allows some drugs to be dropped after the initial cycle while still using the initial drug set for treatment assignment.

### Progression-Free Survival (PFS)

A patient's PFS is the time between the start of their treatment and disease progression or death.
Some reference patients may not experience an event before the time of the last follow-up, in which case they are marked
"censored" and we do not know their true PFS.
Each `Episode` has a list of PFS measures with dates, each of which can represent progression, death, or censorship.
The episode's PFS measures are summarized in two fields of the systemic treatment plan:
* `hadProgressionEvent`: This indicates whether a patient experienced a progression event during a study.
  `false` here means the patient was censored.
* `observedPfsDays`: This is the number of days for which a patient is known to have been progression-free.
    * For patients that had a progression event, this is the time between treatment start and the first event.
    * For patients with no event, this is time between treatment start and the last time they were marked as censored.