import pandas as pd
import pymysql

from sklearn.impute import KNNImputer
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.preprocessing import MinMaxScaler, StandardScaler

from typing import List, Dict, Tuple, Any

from utils.settings import settings
from .lookups import lookup_manager

class DataSplitter:
    def __init__(self, test_size: float=0.1, random_state: int=42) -> None:
        self.test_size = test_size
        self.random_state = random_state

    def split(self, X: pd.DataFrame, y: pd.DataFrame, encoded_columns: Dict[str, List[str]] = None) -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame, pd.DataFrame]:
        """
        Split the data into training and test sets, stratified by treatment type and censoring status.
        """
        if settings.event_col:
            stratify_labels = y[settings.event_col].astype(str)
        else:
            stratify_labels = None
       
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=self.test_size, random_state=self.random_state, stratify=stratify_labels
        )
        
        settings.input_size = X_train.shape[1]
        
        return X_train, X_test, y_train, y_test

class DataPreprocessor:
    def __init__(self, db_config_path: str, db_name: str) -> None:
        self.db_config_path = db_config_path
        self.db_name = db_name
        self.data_dir = "data"
        self.encoded_columns = {}

    def preprocess_data(self, features = lookup_manager.features, df = None) -> Tuple[pd.DataFrame, List[str], Dict[str, List[str]]]:
        if df is None:
            df = self.load_data()
         
        df = df[features + [settings.duration_col, settings.event_col]]
        df = df[~df["firstSystemicTreatmentAfterMetastaticDiagnosis"].str.upper().str.contains("NIVOLUMAB", na=False)]

        df = df[~df[lookup_manager.features].isna().all(axis=1)].copy()
        
        if settings.experiment_type == 'treatment_vs_no':
            df = self.group_treatments(df)
        elif settings.experiment_type == 'treatment_drug':
            df = self.add_treatment_drugs(df)

        df = self.impute_knn(df, ['whoAssessmentAtMetastaticDiagnosis'], k=7)
        df = self.numerize(df, lookup_manager.lookup_dictionary)
        df = self.handle_missing_values(df)

        df = self.encode_categorical(df)

        updated_features = [col for col in df.columns if col not in [settings.duration_col, settings.event_col]]
        
        # df = self.normalize(df, updated_features)
        df = self.standardize(df, updated_features)
        
        return df, updated_features, self.encoded_columns

    
    def load_data(self) -> pd.DataFrame:
        db_connection = pymysql.connect(
            read_default_file=self.db_config_path,
            read_default_group='RAnalysis',
            db=self.db_name
        )
    
        df = pd.read_sql(f"SELECT * FROM {settings.view_name}", db_connection)
    
        db_connection.close()
        if settings.event_col == "isAlive":
            df[settings.event_col] = 1 - df["isAlive"]
        
        return df.dropna(subset=[settings.duration_col, settings.event_col]).copy()

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
        """
        Handle missing values in the DataFrame.
        - For numerical columns, fill NaN with median and add an indicator column.
        """
        numerical_cols = df.select_dtypes(include=['float64', 'int64']).columns.tolist()
        numerical_cols = [col for col in numerical_cols if col not in [settings.event_col, settings.duration_col]]

        for col in numerical_cols:
            df[col] = df[col].fillna(df[col].median())

        if settings.duration_col in df.columns:
            df = df[df[settings.duration_col] > 0].copy() 

        return df
    
    def group_treatments(self, df: pd.DataFrame, treatment_col: str = 'systemicTreatmentPlan') -> pd.DataFrame:
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

        if "bevacizumab" in t or t.endswith("_b"):
            components["systemicTreatmentPlan_bevacizumab"] = 1

        if "panitumumab" in t or t.endswith("_p"):
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

    def encode_categorical(self, df: pd.DataFrame) -> pd.DataFrame:
        categorical_cols = df.select_dtypes(include=['object', 'category']).columns.tolist()
        categorical_cols = [col for col in categorical_cols if col not in [settings.event_col, settings.duration_col]]

        for col in categorical_cols:
            if df[col].nunique() == 2:
                le = LabelEncoder()
                df[col] = le.fit_transform(df[col].astype(str))
                self.encoded_columns[col] = le.classes_
            else:
                dummies = pd.get_dummies(df[col], prefix=col, dummy_na=False)
                df = pd.concat([df.drop(columns=[col]), dummies], axis=1)
                self.encoded_columns[col] = list(dummies.columns)
                
        return df

    def normalize(self, df: pd.DataFrame, features: List[str]) -> pd.DataFrame:
        """
        Normalize numerical features to a range of [0, 1].
        """
        scaler = MinMaxScaler()
        cols_to_normalize = [
            col for col in features
            if pd.api.types.is_numeric_dtype(df[col]) and col not in [settings.event_col, settings.duration_col]
        ]
        df[cols_to_normalize] = scaler.fit_transform(df[cols_to_normalize])
        return df

    def standardize(self, df: pd.DataFrame, features: List[str]) -> pd.DataFrame:
        """
        Standardize continuous numerical features to have a mean of 0 and standard deviation of 1.
        Binary or nearly binary columns (<=2 unique values) are not standardized.
        """
        scaler = StandardScaler()
        cols_to_standardize = [
            col for col in features
            if col != 'ncrId'
            if pd.api.types.is_numeric_dtype(df[col])
            and col not in [settings.event_col, settings.duration_col]
            and df[col].nunique() > 2  # exclude binary columns
        ]
        df[cols_to_standardize] = scaler.fit_transform(df[cols_to_standardize])
        
        return df