import pandas as pd
import numpy as np
import pymysql
import json

from sklearn.impute import KNNImputer
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler

from typing import List, Dict, Tuple, Any
import joblib
import warnings

from utils.settings import config_settings
from .lookups import lookup_manager

class DataSplitter:
    def __init__(self, settings=config_settings, test_size: float=0.1, random_state: int=42) -> None:
        self.settings = settings
        self.test_size = test_size
        self.random_state = random_state

    def split(self, X: pd.DataFrame, y: pd.DataFrame, encoded_columns: Dict[str, List[str]] = None) -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame, pd.DataFrame]:
        """
        Split the data into training and test sets, stratified by treatment type and censoring status.
        """
        if isinstance(y, np.ndarray) and self.settings.event_col in y.dtype.names:
            stratify_labels = y[self.settings.event_col].astype(str)
        else:
            stratify_labels = None
       
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=self.test_size, random_state=self.random_state, stratify=stratify_labels
        )
        
        self.settings.input_size = X_train.shape[1]
        
        return X_train, X_test, y_train, y_test

class DataPreprocessor:
    def __init__(self, settings=config_settings, fit: bool = True, preprocessor_path=True) -> None:
        self.settings = settings
        self.db_config_path = self.settings.db_config_path
        self.db_name = self.settings.db_name
        self.data_dir = "data"
        self.fit = fit
        
        if preprocessor_path:
            self.preprocessor_path = os.path.join(self.settings.save_path, f"{self.settings.outcome}_preprocessor")
        else:
            self.preprocessor_path = self.settings.save_path

        if not self.fit:
            try:
                with open(f"{self.preprocessor_path}/preprocessing_config.json", "r") as f:
                    config = json.load(f)
                    self.medians = config.get("medians", {})
                    self.encoded_columns = config.get("encoded_columns", {})
            except Exception as e:
                warnings.warn(f"Failed to load preprocessing_config.json: {e}")
                self.medians = {}
                self.encoded_columns = {}

            try:
                self.scaler = joblib.load(f"{self.preprocessor_path}/standard_scaler.pkl", "r")
            except Exception as e:
                warnings.warn(f"Failed to load StandardScaler: {e}")
                self.scaler = None
                
        else:
            self.medians = {}
            self.encoded_columns = {}
            self.scaler = None 


    def preprocess_data(self, features = lookup_manager.features, df = None) -> Tuple[pd.DataFrame, List[str], Dict[str, List[str]]]:
        if df is None:
            df = self.load_data()
                            
        df = df[features + [self.settings.duration_col, self.settings.event_col]]

        df = df[~df["firstSystemicTreatmentAfterMetastaticDiagnosis"].str.upper().str.contains("NIVOLUMAB", na=False)]
        
        df = df[~df[lookup_manager.features].isna().all(axis=1)].copy()

        if self.settings.experiment_type == 'treatment_vs_no':
            df = self.group_treatments(df)
        elif self.settings.experiment_type == 'treatment_drug':
            df = self.add_treatment_drugs(df)

        df = self.impute_knn(df, ['whoAssessmentAtMetastaticDiagnosis'], k=7)
        
        df = self.numerize(df, lookup_manager.lookup_dictionary)
        
        df = self.auto_cast_object_columns(df)
        df = self.handle_missing_values(df)
        
        df = self.encode_categorical(df) 

        updated_features = [col for col in df.columns if col not in [self.settings.duration_col, self.settings.event_col]]
        
        if self.settings.standardize:
            df = self.standardize(df, updated_features)
       
        return df, updated_features, self.encoded_columns

    
    def load_data(self) -> pd.DataFrame:
        db_connection = pymysql.connect(
            read_default_file=self.db_config_path,
            read_default_group='RAnalysis',
            db=self.db_name
        )
    
        df = pd.read_sql(f"SELECT * FROM {self.settings.view_name}", db_connection)
    
        db_connection.close()
        
        return df.dropna(subset=[self.settings.duration_col, self.settings.event_col]).copy()

    def impute_knn(self, df: pd.DataFrame, columns: List[str], k: int) -> pd.DataFrame:
        """
        :param df: DataFrame to process.
        :param k: Number of neighbors for KNN imputation.
        :return: Updated DataFrame with imputed values.
        """
        imputer = KNNImputer(n_neighbors=k)
        df[columns] = imputer.fit_transform(df[columns])
    
        return df

    def numerize(self, df: pd.DataFrame, lookup_dictionary: Dict[str, Dict[Any, Any]]) -> pd.DataFrame:
        """
        :param df: DataFrame to process.
        :param lookup_dictionary: Dictionary mapping column names to their lookup values.
        :return: Updated DataFrame with numerized columns.
        """
        for column, lookup in lookup_dictionary.items():
            if column in df.columns:
                df[column] = df[column].map(lookup)
                
        return df
    
    def handle_missing_values(self, df: pd.DataFrame) -> pd.DataFrame:
      
        numerical_cols = df.select_dtypes(include=['float64', 'int64', 'bool']).columns.tolist()
        numerical_cols = [col for col in numerical_cols if col not in [self.settings.event_col, self.settings.duration_col]]

        for col in numerical_cols:
            if self.fit:
                median_value = df[col].median()
                self.medians[col] = median_value
               
            else:
                median_value = self.medians.get(col, 0)  
            df[col] = df[col].fillna(median_value)
        # Check for duplicate column names
        duplicate_cols = df.columns[df.columns.duplicated()].tolist()
        if duplicate_cols:
            raise ValueError(f"Duplicate columns found in DataFrame: {duplicate_cols}")

        if self.settings.duration_col in df.columns:
            df = df[df[self.settings.duration_col] > 0].copy()

        return df
    
    def group_treatments(self, df: pd.DataFrame, treatment_col: str = 'firstSystemicTreatmentAfterMetastaticDiagnosis') -> pd.DataFrame:
        df['treatment'] = df[treatment_col].apply(
            lambda x: 1 if pd.notnull(x) and str(x).strip() != '' else 0
        )
        df = df.drop(columns = [treatment_col])
        
        return df
    
    def parse_treatment(self, treatment: str) -> Dict[str, int]:

        components = {"systemicTreatmentPlan_5-FU": 0, "systemicTreatmentPlan_oxaliplatin": 0, "systemicTreatmentPlan_irinotecan": 0, "systemicTreatmentPlan_bevacizumab": 0, "systemicTreatmentPlan_panitumumab": 0, "systemicTreatmentPlan_pembrolizumab": 0, "systemicTreatmentPlan_nivolumab": 0}

        if pd.isna(treatment) or treatment.strip() == "":
            return components    
        
        t = treatment.lower()

        if any(x in t for x in ["fluorouracil", "fol", "cap"]):
            components["systemicTreatmentPlan_5-FU"] = 1

        if "ox" in t:
            components["systemicTreatmentPlan_oxaliplatin"] = 1

        if "iri" in t:
            components["systemicTreatmentPlan_irinotecan"] = 1

        if "bevacizumab" in t or t.endswith("-b"):
            components["systemicTreatmentPlan_bevacizumab"] = 1

        if "panitumumab" in t or t.endswith("-p"):
            components["systemicTreatmentPlan_panitumumab"] = 1

        if "pembrolizumab" in t: 
            components["systemicTreatmentPlan_pembrolizumab"] = 1
        
        if "nivolumab" in t: 
            components["systemicTreatmentPlan_nivolumab"] = 1
            
        return components


    def add_treatment_drugs(self, df: pd.DataFrame, treatment_col: str = "firstSystemicTreatmentAfterMetastaticDiagnosis") -> pd.DataFrame:
        treatment_components = df[treatment_col].apply(self.parse_treatment)

        components_df = pd.DataFrame(treatment_components.tolist(), index=df.index)         
        components_df["hasTreatment"] = components_df.sum(axis=1).clip(upper=1)

        df = df.join(components_df)
        
        df = df.drop(columns = [treatment_col], axis = 1)

        return df
    
    def auto_cast_object_columns(self, df: pd.DataFrame) -> pd.DataFrame:
        for col in df.select_dtypes(include='object').columns:
            try:
                df[col] = pd.to_numeric(df[col], errors='raise')
            except Exception:
                unique_vals = df[col].dropna().unique()
                if set(unique_vals).issubset({'0', '1', 0, 1, True, False}):
                    df[col] = df[col].astype(float)
                else:
                    continue  

        return df

    def encode_categorical(self, df: pd.DataFrame) -> pd.DataFrame:

        for col, encoding_info in self.encoded_columns.items():
            if encoding_info["type"] == "onehot" and col in df.columns:
                df[col] = df[col].astype('object')
                
        categorical_cols = df.select_dtypes(include=['object', 'category']).columns.tolist()
        categorical_cols = [col for col in categorical_cols if col not in [self.settings.event_col, self.settings.duration_col]]

          
        for col in categorical_cols:
            if self.encoded_columns and col in self.encoded_columns:
                encoding_info = self.encoded_columns[col]
                if encoding_info["type"] == "label":
                    classes = encoding_info["classes"]
                    mapping = {label: idx for idx, label in enumerate(classes)}
                    df[col] = df[col].astype(str).map(mapping).fillna(0).astype(int)
                    

                elif encoding_info["type"] == "onehot":
                    expected_dummies = encoding_info["columns"]
                    if df[col].isnull().all():
                        dummies = pd.DataFrame({dummy_col: [0] * len(df) for dummy_col in expected_dummies}, index=df.index)
                    else:
                        dummies = pd.get_dummies(df[col], prefix=col, dummy_na=False)
                        for dummy_col in expected_dummies:
                            if dummy_col not in dummies.columns:
                                dummies[dummy_col] = 0
                        dummies = dummies[expected_dummies]

                    df = pd.concat([df.drop(columns=[col]), dummies], axis=1)
             
            else:
                if df[col].nunique(dropna=False) == 2:
                    le = LabelEncoder()
                    df[col] = le.fit_transform(df[col].astype(str))
                    self.encoded_columns[col] = {
                        "type": "label",
                        "classes": le.classes_.tolist()
                    }
                else:
                    dummies = pd.get_dummies(df[col], prefix=col, dummy_na=False)
                    df = pd.concat([df.drop(columns=[col]), dummies], axis=1)
                    self.encoded_columns[col] = {
                        "type": "onehot",
                        "columns": list(dummies.columns)
                    } 
              
        if self.fit and self.settings.save_models:
            with open(f"{self.settings.save_path}/{self.settings.outcome}_preprocessor/preprocessing_config.json", "w") as f:
                json.dump({
                    "medians": self.medians,
                    "encoded_columns": self.encoded_columns
                }, f)

        return df
    
    def standardize(self, df: pd.DataFrame, features: List[str]) -> pd.DataFrame:
        if self.fit:
            cols_to_standardize = [
                col for col in features
                if pd.api.types.is_numeric_dtype(df[col])
                and col not in [self.settings.event_col, self.settings.duration_col]
                and df[col].nunique() > 2
            ]

            self.scaler = StandardScaler()
            df[cols_to_standardize] = self.scaler.fit_transform(df[cols_to_standardize])

            if self.settings.save_models:
                joblib.dump(self.scaler, f"{self.settings.save_path}/{self.settings.outcome}_preprocessor/standard_scaler.pkl")
            return df

        else:
            if self.scaler is None:
                raise RuntimeError(f"No pre‐fitted StandardScaler found at {self.settings.save_path}/{self.settings.outcome}_preprocessor/standard_scaler.pkl")
                
            trained_cols = list(self.scaler.feature_names_in_)
            df = df.assign(**{c: 0.0 for c in trained_cols if c not in df.columns})
            
            df.loc[:, trained_cols] = self.scaler.transform(df[trained_cols])

            return df