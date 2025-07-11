# ACTIN-Personalization - Prediction

This module focuses on **predicting survival outcomes** to support treatment decision-making for cancer patients.

It consists of two complementary components:

---

## 🔍 Analytical Insights (`resources/`)

This section contains a collection of Jupyter notebooks that explore:

- Clinical and methodological **biases** (e.g., in untreated patients or treatment groupings)
- **Survival curve assumptions** (e.g., proportional hazards)
- **Feature relationships** with Overall Survival (OS) and Progression-Free Survival (PFS)
- The **correlation** between OS and PFS

These analyses inform modeling decisions and ensure the robustness and fairness of the predictive algorithms.

---

## ⚙️ Survival Prediction Pipeline (`prediction/src/main/python/`)

This part of the module contains the full codebase for:

- **Preprocessing** structured clinical and molecular data
- **Training** survival models (e.g., CoxPH, DeepSurv, DeepHit)
- **Evaluating** model performance using metrics like C-Index, IBS, CE, and AUC
- **Interpreting** model outputs with SHAP values
- **Saving and loading** models for downstream use in production or research

Model artifacts can be stored locally or synced with Google cloud storage (`gs://actin-personalization-models-v1`) for reuse and scalability.

The pipeline is designed for flexibility, enabling rapid experimentation and integration of new model types or data sources.

---

## 🐍 Python 3.11 Environment Setup

The pipeline is designed to run on **Python 3.11**. To get started:

### 1. Install Python 3.11 (if needed)

If Python 3.11 is not yet available on your machine (e.g., in a VM), install it using `pyenv` and add to your path:

```bash
version="3.11.13"

if [[ ! -d "${HOME}/.pyenv" ]]; then 
    echo "PyEnv not yet present, installing"
    curl -L https://raw.githubusercontent.com/yyuu/pyenv-installer/master/bin/pyenv-installer | bash
fi

echo "Initialising pyenv..."
${HOME}/.pyenv/bin/pyenv install ${version}
${HOME}/.pyenv/versions/${version}/bin/pip install --upgrade pip

echo "Switching to new ${version} version"
export PATH="${HOME}/.pyenv/versions/${version}/bin:$PATH"
```

### 2. Create and activate a virtual environment
```bash
python3 -m venv /path/to/your/env/prediction_3_11_venv
source /path/to/your/env/prediction_3_11_venv/bin/activate
```

### 3. Install required packages
Install all required dependencies using the requirements.txt in the main code folder:
```bash
pip install -r /path/to/actin-personalization-repo/prediction/src/main/python/requirements.txt
```

### 4. (Optional) Add the environment to JupyterLab
If you're working in Jupyter and want the new environment to show up:
```bash
pip install ipykernel
python3 -m ipykernel install --user --name=prediction_3_11_venv --display-name "Python 3.11 (prediction)"
```
_Note: You may need to restart JupyterLab for the new kernel to appear via_
```bash
sudo systemctl {start/stop/status} jupyterlab.service
```

---

## ✅ Running Tests
Tests for the python pipeline are located in:
`prediction/src/test/python`

We use `pytest` to run all tests. Here's an example command to test all available tests:

```bash
cd path/to/actin-personalization
PYTHONPATH=prediction/src/main/python python3.11 -m pytest prediction/src/test/python/utils/test_metrics.py
```

Make sure your virtual environment is activated before running tests. (```source /path/to/your/env/prediction_3_11/bin/activate ```)
