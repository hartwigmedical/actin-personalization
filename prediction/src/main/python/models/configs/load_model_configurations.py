import importlib
import json
import os
from models.models import *
from utils.settings import config_settings

class ExperimentConfig:
    def __init__(self, config_file, settings=config_settings):
        self.config_file = config_file
        self.configs = self._load_configs()
        
        self.settings = settings

    def _load_configs(self):
        with open(self.config_file, 'r') as f:
            configs = json.load(f)
        
        if not isinstance(configs, dict):
            raise TypeError(f"Expected the configuration file to contain a dictionary, but got {type(configs).__name__}.")
        
        for key, value in configs.items():
            if not isinstance(value, dict):
                raise TypeError(f"Expected each configuration entry to be a dictionary, but got {type(value).__name__} for key '{key}'.")
        
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
            kwargs = dict(setting.get("kwargs", {}))

            if model_name.startswith("MultiTaskNN_"):
                backbone_spec = kwargs.pop("model_class", None)
                if isinstance(backbone_spec, str):
                    bb_mod, bb_cls = backbone_spec.rsplit('.', 1)
                    kwargs["model_class"] = getattr(importlib.import_module(bb_mod), bb_cls)
                elif isinstance(backbone_spec, dict):
                    nested_class_path = backbone_spec.get("class")
                    if nested_class_path:
                        bb_mod, bb_cls = nested_class_path.rsplit('.', 1)
                        kwargs["model_class"] = getattr(importlib.import_module(bb_mod), bb_cls)
                elif backbone_spec is not None:
                    kwargs.pop("model_class", None)

            if issubclass(model_class, NNSurvivalModel) and 'input_size' not in kwargs:
                kwargs['input_size'] = self.settings.input_size

            model_configs[model_name] = (model_class, kwargs)

        return model_configs

    
    @staticmethod
    def update_model_hyperparams(best_models: dict) -> None:
        path = config_settings.json_config_file
        config = {}
        if os.path.exists(path):
            try:
                with open(path, "r") as f:
                    config = json.load(f)
            except json.JSONDecodeError:
                print("Warning: JSON file is corrupted. Starting with an empty configuration.")
                config = {}

        key = f"{config_settings.experiment_type}_{config_settings.outcome}"
        config.setdefault(key, {})

        for model_name, (model_instance, best_params) in best_models.items():
            if not best_params:
                continue

            model_type = model_instance if isinstance(model_instance, type) else type(model_instance)
            params = dict(best_params)

            if issubclass(model_type, NNSurvivalModel):
                params["input_size"] = config_settings.input_size

            if "model_class" in params:
                model_class = params.pop("model_class")
                if isinstance(model_class, type):
                    params["model_class"] = f"{model_class.__module__}.{model_class.__name__}"
                elif isinstance(model_class, str) and model_class.startswith("<class '"):
                    params["model_class"] = model_class.split("'")[1]
                elif isinstance(model_class, str):
                    params["model_class"] = model_class
                else:
                    params["model_class"] = f"{type(model_class).__module__}.{type(model_class).__name__}"
            params.pop("model_kwargs", None)

            # json-ify numpy types
            serializable = {}
            for k, v in params.items():
                if hasattr(v, "item"):
                    v = v.item()
                elif hasattr(v, "tolist"):
                    v = v.tolist()
                serializable[k] = v

            module_path = model_type.__module__
            if module_path.startswith("src."):
                module_path = module_path[len("src."):]

            config[key][model_name] = {
                "class": f"{module_path}.{model_type.__name__}",
                "kwargs": serializable,
            }

        try:
            with open(path, "w") as f:
                json.dump(config, f, indent=4)
            print(f"Updated model hyperparameters saved to '{path}' under key '{key}'.")
        except Exception as e:
            print(f"Error saving model hyperparameters: {e}")
