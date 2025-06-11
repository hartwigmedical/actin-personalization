## ACTIN-Personalization - Prediction

This module focuses on **predicting survival outcomes** to support treatment decision-making for cancer patients.

It consists of two complementary components:

---

## 🔬 Analytical Insights (`resources/`)

This section contains a collection of Jupyter notebooks that explore:
- Clinical and methodological **biases** (e.g., in untreated patients or treatment groupings)
- **Survival curve assumptions** (e.g., proportional hazards)
- **Feature relationships** with Overall Survival (OS) and Progression-Free Survival (PFS)
- The **correlation** between OS and PFS

These analyses inform modeling decisions and ensure the robustness and fairness of the predictive algorithms.

---

## 🤖 Survival Prediction Pipeline (`python/`)

This part of the module contains the full codebase for:
- **Preprocessing** structured clinical and molecular data
- **Training** survival models (e.g., CoxPH, DeepSurv, DeepHit)
- **Evaluating** model performance using metrics like C-Index, IBS, CE, and AUC
- **Interpreting** model outputs with SHAP values
- **Saving and loading** models for downstream use in production or research

Model artifacts are stored locally or synced with Google Cloud Storage (`gs://actin-personalization-models-v1`), allowing seamless reuse.

The pipeline is built with flexibility in mind, allowing quick experimentation and integration of new model types or data sources.

