# Predictive Survival Analysis Repository

This repository implements predictive algorithms for survival analysis, specifically for **Overall Survival (OS)** and **Progression-Free
Survival (PFS)**. It includes data preprocessing, model training, hyperparameter optimization, feature selection, and model interpretation.

---

## 📁 **Directory Overview**

Below is the organized structure of the repository, detailing the role of each folder and file.

### **Root Directory**

| File/Folders                                      | Description                                                                                  |
|---------------------------------------------------|----------------------------------------------------------------------------------------------|
| `ACTIN-1393_Predictive_algorithms_pipeline.ipynb` | Main Jupyter Notebook for building, evaluating, and interpreting predictive survival models. |
| `__init__.py`                                     | Makes the root directory importable as a package.                                            |
| `requirements.txt`                                | Contains a list of all Python dependencies required to run the project.                      |

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

| File/Folder                                                  | Description                                                                             |
|--------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| `hyperparameter_optimization.py`                             | Handles hyperparameter tuning using random search.                                      |
| `model_trainer.py`                                           | Manages model training, cross-validation, and evaluation.                               |
| `survival_models.py`                                         | Implements survival models, including CoxPH, Aalen Additive, and neural network models. |
| `configs/`                                                   | Stores model and hyperparameter configurations.                                         |
| ├── `hyperparameter_grids.py`                                | Defines the grid of hyperparameters for optimization.                                   |
| ├── `model_configurations.py`                                | Contains the best model configurations.                                                 |
| to be made by the code/copied by the user: `trained_models/` | Directory for storing trained model artifacts and evaluation results.                   |
| ├── `OS_*` & `PFS_*` files                                   | Trained models for OS and PFS tasks (CoxPH, Aalen Additive, DeepHit, etc.).             |
| ├── `OS_model_outcomes.csv`                                  | Performance metrics for OS models.                                                      |
| ├── `PFS_model_outcomes.csv`                                 | Performance metrics for PFS models.                                                     |

---

### 📁 **utils/**

Utility functions for evaluation metrics and other supporting operations.

| File          | Description                                                                                      |
|---------------|--------------------------------------------------------------------------------------------------|
| `metrics.py`  | Defines metrics like C-Index, Integrated Brier Score, Calibration Error, and Time-Dependent AUC. |
| `utils.py`    | Contains helper functions                                                                        |
| `__init__.py` | Initializes the `utils` module.                                                                  |

---

## 🚀 **Workflow**

1. **Data Preparation**:  
   Use the `data_processing.py` script to preprocess and split survival data.

2. **Model Training**:  
   Train models using the `train_and_evaluate` method in the `ModelTrainer` class.

3. **Feature Selection**:  
   Perform feature selection for CoxPH and Aalen Additive models.

4. **Evaluation and Visualization**:
    - Evaluate models using metrics like C-Index, IBS, CE, and AUC.
    - Visualize survival curves and compare model performance.

5. **Model Interpretation**:  
   Use SHAP to interpret the trained models.

---

## 📥 **Loading Pretrained Models**

You need to load the pretrained models into your `trained_models` directory, they can be found gs:
//actin-personalization-models-v1/trained_models/

## 🛠 Python Environment Setup

To ensure that the code runs as intended, set up your Python environment with the required dependencies. This repository uses a requirements.txt file to manage dependencies. Run the following command in the root directory of the repository:
`pip install -r requirements.txt`

This will install all the packages required to run the project.





