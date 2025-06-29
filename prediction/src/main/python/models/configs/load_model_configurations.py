import importlib
import json
import os
from models.survival_models import *
from utils.settings import config_settings

class ExperimentConfig:
    def __init__(self, config_file, settings=config_settings):
        self.config_file = config_file
        self.configs = self._load_configs()
        
        self.settings = settings

    def _load_configs(self):
        with open(self.config_file, 'r') as f:
            configs = json.load(f)
        return configs
    
    def get_config(self):
        key = f"{self.settings.experiment_type}_{self.settings.outcome}"
        if key not in self.configs:
            raise ValueError(
                f"No configuration found for experiment type '{self.settings.experiment_type}' and outcome '{self.settings.outcome}'."
            )
        return self.configs[key]

    def load_model_configs(self):
        config_dict = self.get_config()
        model_configs = {}
        for model_name, setting in config_dict.items():
            class_path = setting.get("class")
            module_name, class_name = class_path.rsplit('.', 1)
            module = importlib.import_module(module_name)
            model_class = getattr(module, class_name)
            kwargs = setting.get("kwargs", {})
            if issubclass(model_class, NNSurvivalModel):
                if 'input_size' not in kwargs:
                    kwargs['input_size'] = self.settings.input_size
            model_configs[model_name] = (model_class, kwargs)
       
        return model_configs
    
    @staticmethod
    def update_model_hyperparams(best_models: dict) -> None:
        
        if os.path.exists(settings.json_config_file):
            with open(settings.json_config_file, "r") as f:
                config = json.load(f)
        else:
            config = {}

        key = f"{settings.experiment_type}_{settings.outcome}"
        if key not in config:
            config[key] = {}

        for model_name, (model_instance, best_params) in best_models.items():
            if best_params is None:
                continue
            model_class = model_instance if isinstance(model_instance, type) else type(model_instance)
            if issubclass(model_class, NNSurvivalModel):
                best_params['input_size'] = settings.input_size
                
            module_path = model_class.__module__
            if module_path.startswith("src."):
                module_path = module_path[len("src."):]

            config[key][model_name] = {
                "class": f"{module_path}.{model_class.__name__}",
                "kwargs": best_params
            }

        
        with open(settings.json_config_file, "w") as f:
            json.dump(config, f, indent=4)

        print(f"Updated model hyperparameters saved to '{settings.json_config_file}' under key '{key}'.")
