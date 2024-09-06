## Similarity

This module supports estimating patient Progression-Free Survival (PFS) based on similar reference patients.

### Estimating PFS of a Population

#### Naive Approach - Complete Data Analysis

Initially, we considered excluding all censored patients and taking the median PFS of all patients that experienced
progression or death.
So-called "complete data analysis" may be justified in cases where censoring is independent of the estimated quantity
(e.g. where some participants randomly drop out), but in our case, individuals with longer PFS are more likely to
reach the final follow-up before experiencing a progression event and thus be censored.

#### Kaplan-Meier Estimator

The Kaplan-Meier or product limit estimator is a non-parametric approach, which means that it doesn't make any
assumptions about the shape of the true underlying survival function.
It makes use of all available information at every event time to predict survival, including reference patients who are
censored at later times.

The probability `p` of surviving any interval `i` is the fraction of patients that are at risk during that interval that
do not experience a progression event during that interval.

`p = (numSurvivedUpToI - numEventsI) / numSurvivedUpToI`

`p = 1 - (numEventsI / numSurvivedUpToI)`

If we handle each event independently, then only one patient does not survive each interval:

`p = 1 - (1 / numSurvivedUpToI)`

The estimation at event time `t` is the product of the survival probabilities `p` for all event intervals `i` up to `t`.
This is calculated recursively for all events and the output can be used to generate survival plots.