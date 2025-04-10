import json
import importlib

from src.utils.settings import settings
from src.models import *

class ExperimentConfig:
    def __init__(self, config_file):
        self.config_file = config_file
        self.configs = self._load_configs()
        
    @staticmethod 
    def save_model_configs(configs):
        serializable_configs = {}
        for model_name, (model_class, model_kwargs) in configs.items():
            if issubclass(model_class, NNSurvivalModel):
                model_kwargs['input_size'] = settings.input_size
       
            class_path = f"{model_class.__module__}.{model_class.__name__}"
            serializable_configs[model_name] = {
                "class": class_path,
                "kwargs": model_kwargs
            }

        with open(f"{settings.save_path}/{settings.outcome}_model_configs", "w") as f:
            json.dump(serializable_configs, f, indent=4)

        print(f"Model configurations saved to model_configs{settings.outcome}")  

    def _load_configs(self):
        with open(self.config_file, 'r') as f:
            configs = json.load(f)
        return configs
    
    def get_config(self):
        key = f"{settings.experiment_type}_{settings.outcome}"
        if key not in self.configs:
            raise ValueError(
                f"No configuration found for experiment type '{settings.experiment_type}' and outcome '{settings.outcome}'."
            )
        return self.configs[key]

    def load_model_configs(self):
        config_dict = self.get_config()
        model_configs = {}
        print(config_dict)
        for model_name, setting in config_dict.items():
            print(model_name)
            class_path = setting.get("class")
            module_name, class_name = class_path.rsplit('.', 1)
            module = importlib.import_module(module_name)
            model_class = getattr(module, class_name)
            kwargs = setting.get("kwargs", {})
            if issubclass(model_class, NNSurvivalModel):
                if 'input_size' not in kwargs:
                    kwargs['input_size'] = settings.input_size
            model_configs[model_name] = (model_class, kwargs)
       
        return model_configs
