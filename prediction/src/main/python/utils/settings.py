from dataclasses import dataclass
from typing import List, Optional

@dataclass
class Settings:
    #--------------------------------------------------------------------------------------------
    # User-configurable settings:
    experiment_type: str = 'treatment_drug'  # Options: treatment_specific, treatment_vs_no, treatment_drug
    outcome: str = 'OS'                          # "OS" or "PFS"
    
    cross_val_n_splits: int = 5
    hyperparam_tuning_optimization_metric: str = 'auc'  # c_index, IBS, CE, auc
    hyperparam_tuning_number_combinations: int = 10
    save_models: bool = True
    json_config_file: str = 'src/main/python/models/configs/model_hyperparams.json'
    db_name: str = 'actin_personalization_v2'
    view_name: str = 'knownPalliativeTreatedReference'
    db_config_path: str = '/home/jupyter/.my.cnf'
    
    standardize: bool = True
    #--------------------------------------------------------------------------------------------
    # Derived or computed settings:
    event_col: Optional[str] = None
    duration_col: Optional[str] = None

    save_path: Optional[str] = None
    time_points: Optional[List[int]] = None
    max_time: int = 1825
    input_size: int = 101  # This is updated in data_processing.py once it has X_train.shape[1].
    use_gate: bool = True
    n_jobs: int = 4 # depends on how many CPU's you have available
        
    def __post_init__(self):
        self.configure_data_settings()
        self.configure_model_settings()
    
    def configure_data_settings(self) -> None:
        self.group_treatment = False 
        if self.outcome.upper() == 'OS':
            self.event_col = 'hadSurvivalEvent'
            self.duration_col = 'survivalDaysSinceMetastaticDiagnosis'
            self.view_name = 'palliativeReference'
        else:
            self.event_col = 'hadProgressionEvent'
            self.duration_col = 'daysBetweenTreatmentStartAndProgression'
            self.view_name = 'knownPalliativeTreatedReference'
    
    def configure_model_settings(self) -> None:
        self.save_models = True
        if self.save_path is None:
            self.save_path = f'/data/patient_like_me/prediction/trained_models/{self.experiment_type}'
        if self.outcome.upper() == 'OS':
            self.time_points = [int(round(i * 365 / 4)) for i in range(1, 21)]
        else:
            self.time_points = [int(round(i * 365 / 4)) for i in range(1, 13)]
            self.max_time = 1095

settings = Settings()
