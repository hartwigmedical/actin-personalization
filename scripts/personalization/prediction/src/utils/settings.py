from dataclasses import dataclass, field
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
    add_risk_scores: bool = False
    save_models: bool = True
    json_config_file: str = 'src/models/configs/model_hyperparams.json'
    db_name: str = 'actin_personalization'
    view_name: str = 'knownPalliativeTreatments'
    db_config_path: str = '/home/jupyter/.my.cnf'
  
    #--------------------------------------------------------------------------------------------
    # Derived or computed settings:
    event_col: Optional[str] = None
    duration_col: Optional[str] = None

    save_path: Optional[str] = None
    time_points: Optional[List[int]] = None
    max_time: int = 1825
    input_size: int = 83  # This is updated in data_processing.py once it has X_train.shape[1].
    use_gate: bool = True
    n_jobs: int = 4 # depends on how many CPU's you have available
        
    def __post_init__(self):
        self.configure_data_settings()
        self.configure_model_settings()
    
    def configure_data_settings(self) -> None:
        self.group_treatment = False 
        if self.outcome.upper() == 'OS':
            self.event_col = 'isAlive'
            if self.experiment_type == 'treatment_vs_no' or self.experiment_type == 'treatment_drug':
                self.duration_col = 'observedOsFromMetastasisDetectionDays'
                self.view_name = 'palliativeIntents'
            else:
                self.duration_col = 'observedOsFromTreatmentStartDays'
        else:
            self.event_col = 'hadProgressionEvent'
            self.duration_col = 'observedPfsDays'
            if self.experiment_type == 'treatment_vs_no' or self.experiment_type == 'treatment_drug':
                warnings.warn(f"experiment type: {self.experiment_type}, but outcome is {self.outcome}", UserWarning)
    
    def configure_model_settings(self) -> None:
        self.save_models = True
        self.save_path = f'src/models/trained_models/{self.experiment_type}'
        if self.outcome.upper() == 'OS':
            self.time_points = [int(round(i * 365 / 4)) for i in range(1, 21)]
        else:
            self.time_points = [int(round(i * 365 / 4)) for i in range(1, 13)]
            self.max_time = 1095

settings = Settings()
