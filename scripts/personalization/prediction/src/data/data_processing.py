import os
import pandas as pd
import pymysql

from typing import List, Dict, Tuple, Union, Any, Optional

from sklearn.impute import KNNImputer
from sklearn.preprocessing import LabelEncoder, OneHotEncoder
from sklearn.preprocessing import MinMaxScaler, StandardScaler
from sklearn.model_selection import train_test_split

from .lookups import LookupManager

class DataSplitter:
    def __init__(self, test_size: float=0.1, random_state: int=42) -> None:
        self.test_size = test_size
        self.random_state = random_state

    def split(self, X: pd.DataFrame, y: pd.DataFrame, event_col: str, encoded_columns: Dict[str, List[str]]) -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame, pd.DataFrame]:
        """
        Split the data into training and test sets, stratified by treatment type and censoring status.
        """
        stratify_labels = y[event_col].astype(str)
       
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=self.test_size, random_state=self.random_state, stratify=stratify_labels
        )
        return X_train, X_test, y_train, y_test

class DataPreprocessor:
    def __init__(self, db_config_path: str, db_name: str) -> None:
        self.db_config_path = db_config_path
        self.db_name = db_name
        self.data_dir = "data"
        self.encoded_columns = {}
        self.event_col = None
        self.duration_col = None

    def preprocess_data(self, query: str, duration_col: str, event_col: str, features: List[str], group_treatments: bool = False) -> Tuple[pd.DataFrame, List[str], Dict[str, List[str]]]:
        self.duration_col = duration_col
        self.event_col = event_col

        df = self.load_data(query)

        df = df[features + [self.duration_col, self.event_col]]
        df = df[~df[features].isna().all(axis=1)].copy()

        df = self.impute_knn(df, ['whoStatusPreTreatmentStart'], k=7)
        lookup = LookupManager()
        df = self.numerize(df, lookup.lookup_dictionary)
        df = self.handle_missing_values(df)
        
        df = self.expand_column_groups(df, column_name = 'metastasisLocationGroupsPriorToSystemicTreatment')
        if group_treatments:
            df = self.group_treatments(df) 
        df = self.encode_categorical(df)
        
        updated_features = [col for col in df.columns if col not in [self.duration_col, self.event_col]]
        
        # df = self.normalize(df, updated_features)
        df = self.standardize(df, updated_features)
    
        return df, updated_features, self.encoded_columns

    
    def load_data(self, query: str) -> pd.DataFrame:
        db_connection = pymysql.connect(
            read_default_file=self.db_config_path,
            read_default_group='RAnalysis',
            db=self.db_name
        )
        df = pd.read_sql(query, db_connection)
        db_connection.close()
        return df.dropna(subset=[self.duration_col, self.event_col]).copy()

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
        - For numerical columns, fill NaN with -1.
        - For categorical columns, fill NaN with 'Missing'.
        """
       
        numerical_cols = df.select_dtypes(include=['float64', 'int64']).columns.tolist()
        numerical_cols = [col for col in numerical_cols if col not in [self.event_col, self.duration_col]]
        for col in numerical_cols:
            df[col] = df[col].fillna(-1)

        categorical_cols = df.select_dtypes(include=['object', 'category']).columns.tolist()
        categorical_cols = [col for col in categorical_cols if col not in [self.event_col, self.duration_col]]
        for col in categorical_cols:
            df[col] = df[col].fillna('Missing')
            
        if self.duration_col in df.columns:
            df = df[df[self.duration_col] > 0].copy() 
        
        return df
    
    def expand_column_groups(self, df: pd.DataFrame, column_name: str) -> pd.DataFrame:
        """
        Expand a multi-label column into separate binary columns for each unique label.
        """
     
        metastasis_sets = df[column_name].dropna().apply(lambda x: x.split(',')).tolist()
        unique_metastases_categories = sorted(set(item for sublist in metastasis_sets for item in sublist))

        for metastasis in unique_metastases_categories:
            df[f'{column_name}_{metastasis}'] = df[column_name].apply(
                lambda x: 1 if isinstance(x, str) and metastasis in x.split(',') else 0
            )

        df = df.drop(columns=[column_name], errors='ignore')

        return df
    
    def group_treatments(self, df: pd.DataFrame, treatment_col: str = 'systemicTreatmentPlan') -> pd.DataFrame:
        df['treatment'] = df[treatment_col].apply(
            lambda x: 1 if pd.notnull(x) and str(x).strip() != '' else 0
        )
        return df

    def encode_categorical(self, df: pd.DataFrame) -> pd.DataFrame:
        categorical_cols = df.select_dtypes(include=['object', 'category']).columns.tolist()
        categorical_cols = [col for col in categorical_cols if col not in [self.event_col, self.duration_col]]

        for col in categorical_cols:
            if df[col].nunique() == 2:
                le = LabelEncoder()
                df[col] = le.fit_transform(df[col].astype(str))
                self.encoded_columns[col] = le.classes_
            else:
                ohe = OneHotEncoder(sparse=False, drop='first')
                encoded = ohe.fit_transform(df[[col]])
                encoded_df = pd.DataFrame(encoded, columns=ohe.get_feature_names_out([col]), index=df.index)
                df = pd.concat([df.drop(columns=[col]), encoded_df], axis=1)
                self.encoded_columns[col] = ohe.get_feature_names_out([col])
        return df


    def normalize(self, df: pd.DataFrame, features: List[str]) -> pd.DataFrame:
        """
        Normalize numerical features to a range of [0, 1].
        """
        scaler = MinMaxScaler()
        cols_to_normalize = [
            col for col in features
            if pd.api.types.is_numeric_dtype(df[col]) and col not in [self.event_col, self.duration_col]
        ]
        df[cols_to_normalize] = scaler.fit_transform(df[cols_to_normalize])
        return df

    def standardize(self, df: pd.DataFrame, features: List[str]) -> pd.DataFrame:
        """
        Standardize numerical features to have a mean of 0 and std dev of 1.
        """
        scaler = StandardScaler()
        cols_to_standardize = [
            col for col in features
            if pd.api.types.is_numeric_dtype(df[col]) and col not in [self.event_col, self.duration_col]
        ]
        df[cols_to_standardize] = scaler.fit_transform(df[cols_to_standardize])
        return df
    
