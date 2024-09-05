## Similarity

This module supports estimating patient Progression-Free Survival (PFS) based on similar reference patients.

### Estimating PFS of a Population

#### Naive Approach - Complete Data Analysis

Initially, we considered excluding all censored patients and taking the median PFS of all patients that experienced
progression or death.
So-called "complete data analysis" may be justified in cases where censoring is independent of the estimated quantity
(e.g. where some participants randomly quit the study), but individuals with longer PFS are less likely to experience
progression during the study and thus more likely to be censored.

#### Kaplan-Meier Estimator

This is a non-parametric approach, which means that no knowledge of the shape of the true underlying survival function
is required.
The estimation at event time `t` is the product of `1 - numEventsE / (numEventsUpToE + numSurvivedUpToE)` for all
events `e` up to `t`.
This is calculated recursively for all events and the output can be used to generate survival plots.