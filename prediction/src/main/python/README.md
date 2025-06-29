# Predictive Survival Analysis Repository

This repository implements predictive algorithms for survival analysis. It includes data preprocessing, model training, hyperparameter optimization, feature selection, and model interpretation.

---

## 📁 **Directory Overview**

Below is the organized structure of the repository, detailing the role of each folder and file.

### **Root Directory**

| File/Folders                             | Description                                                                 |
|------------------------------------------|-----------------------------------------------------------------------------|
| `ACTIN-1659_predict_days.ipynb`          | Notebook for evaluating model performance in predicting survival days.     |
| `predictive_algorithms_training.ipynb`   | End-to-end pipeline for training survival models.                         |
| `predictive_algorithms_interpretation.ipynb` |  Interpretation of model predictions.                        |
| `requirements.txt`                       | Lists Python dependencies required to run the repository.                  |
| `__init__.py`                            | Marks the root directory as a Python package.                              |

---

### 📁 **data/**

Handles data preprocessing, splitting, and feature management.

| File                 | Description                                                                                      |
|----------------------|--------------------------------------------------------------------------------------------------|
| `data_processing.py` | Preprocesses raw data for survival analysis. Handles encoding, scaling, and feature engineering. |
| `lookups.py`         | Contains lookups for feature selection and other reference mappings.                             |
| `__init__.py`        | Initializes the `data` module.                                                                   |

---

### 📁 **models/**

Contains implementations of survival models, hyperparameter optimization, and trained model artifacts.

| File/Folder                          | Description                                                                 |
|--------------------------------------|-----------------------------------------------------------------------------|
| `hyperparameter_optimization.py`     | Performs hyperparameter search using random sampling.                       |
| `model_trainer.py`                   | Main interface for model training, evaluation, and cross-validation.        |
| `survival_models.py`                 | Defines model classes (CoxPH, Aalen Additive, DeepSurv, DeepHit, etc.).     |
| `configs/`                           | Configuration files for model setup and tuning.                             |
| └── `model_configurations.py`        | Loads and updates the best model configurations.                            |
| └── `hyperparameter_grids.py`        | Defines hyperparameter grids used in optimization.                          |
| `trained_models/`                    | Stores trained model files and performance reports (see below).             |
| `__init__.py`                        | Initializes the models module.

---

### 📁 **utils/**

Utility functions for evaluation metrics and other supporting operations.

| File          | Description                                                                 |
|---------------|-----------------------------------------------------------------------------|
| `metrics.py`  | Implements evaluation metrics (C-Index, IBS, CE, AUC, etc.).               |
| `settings.py` | Central settings used across the repository.                              |
| `__init__.py` | Initializes the utils module.  

---

## 📥 **Loading Pretrained Models**

You need to load the pretrained models into your `trained_models` directory, they can be found gs:
//actin-personalization-models-v1/trained_models/

## 🛠 Python Environment Setup

To ensure that the code runs as intended, set up your Python environment with the required dependencies. This repository uses a requirements.txt file to manage dependencies. Run the following command in the root directory of the repository:
`pip install -r requirements.txt`

This will install all the packages required to run the project.


## 🏃 Running Inference with `run_prediction.py`

To generate survival predictions for a single patient using a pretrained model, use the script `run_prediction.py`.

### 🧾 Input Arguments

| Argument             | Description                                                                                          |
|----------------------|------------------------------------------------------------------------------------------------------|
| `input_path`         | **(Required)** Path to a JSON file with patient data (single record).                                |
| `output_path`        | **(Required)** Path to save the prediction result as a JSON.                                         |
| `--trained_path`     | **(Required)** Path to the folder containing trained model + preprocessor files. The folder must contain:<br>• `model_config.json`<br>• `model.pt`<br>• `preprocessing_config.json`<br>• `standard_scaler.pkl` |
| `--treatment_config` | **(Required)** Path to a JSON file specifying valid treatment combinations to evaluate.              |

### 🧪 Example Usage

```bash
python run_prediction.py \
    /path/to/patient_input.json \
    /path/to/output_predictions.json \
    --trained_path /path/to/model_artifacts \
    --treatment_config /path/to/treatment_combinations.json
```

What this script does:
1. Loads patient data from `input_path`
2. Loads the model and preprocessing pipeline from `trained_path`
3. Loads treatment combinations from `--treatment_config`
4. Generates survival predictions for each treatment scenario
5. Saves predictions to `output_path`
   





