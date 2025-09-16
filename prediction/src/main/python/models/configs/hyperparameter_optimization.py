import random
from itertools import product
from utils.settings import config_settings
from typing import List, Dict, Any

from .load_model_configurations import *
from models.model_trainer import *
from models.models import *


def random_parameter_search(param_dict: Dict[str, List[Any]], settings=config_settings) -> List[Dict[str, Any]]:
    """
    Randomly sample `n_samples` parameter combinations from the given param_dict.
    param_dict should be a dict of lists, e.g.:
      {
        'lr': [0.001, 0.0005, 0.01],
        'dropout': [0.1, 0.2],
      }
    """
    keys = list(param_dict.keys())
    values = [param_dict[k] for k in keys]

    all_combos = list(product(*values))
    if len(all_combos) <= settings.hyperparam_tuning_number_combinations:
        return [dict(zip(keys, combo)) for combo in all_combos]

    sampled_combos = random.sample(all_combos, settings.hyperparam_tuning_number_combinations)
    return [dict(zip(keys, combo)) for combo in sampled_combos]

def hyperparameter_search(X_train: pd.DataFrame, y_train: pd.DataFrame, X_test: pd.DataFrame, y_test: pd.DataFrame,
    encoded_columns: Dict[str, List[str]], 
    base_models: Dict[str, BaseSurvivalModel], 
    param_grids: Dict[str, List[Dict[str, Any]]], 
    settings = config_settings,
    random_state: int = 42
):
    random.seed(random_state)
    best_models = {}
    all_results = {}
    trainer = ModelTrainer(models={}, settings=settings)

    for base in list(param_grids):
        param_grids[base + '_attention'] = param_grids[base]

    for model_name, model_instance in base_models.items():
        model_class = type(model_instance)
        use_attention = getattr(model_instance, 'kwargs', {}).get('use_attention', False)

        grid_key = 'MultiTaskNN' if model_name.startswith('MultiTaskNN_') else model_name

        if grid_key not in param_grids:
            print(f"No hyperparameter grid found for {model_name}, skipping optimization...")
            best_models[model_name] = (model_instance, None)
            continue

        best_score = -np.inf
        best_params = None
        best_model_trained = None
        all_results[model_name] = []

        for param_dict in param_grids[grid_key]:
            sampled_params = random_parameter_search(param_dict=param_dict, settings=settings)

            for params in sampled_params:
                if isinstance(model_instance, MultiTaskNNSurvivalModel) or model_name.startswith('MultiTaskNN_'):
                    base_kwargs = getattr(model_instance, 'kwargs', {})
                    new_model = model_class(model_class=getattr(model_class, 'model_class', base_kwargs.get('model_class')), input_size=X_train.shape[1], **params)
                    ModelTrainer._set_attention_indices(new_model, list(X_train.columns))
                elif issubclass(model_class, NNSurvivalModel):
                    new_model = model_class(input_size=X_train.shape[1], use_attention=use_attention, **params)
                    ModelTrainer._set_attention_indices(new_model, list(X_train.columns))
                else:
                    new_model = model_class(**params)

                print(f"Training {model_name} with parameters: {params}, use_attention: {use_attention}")

                trainer.models = {model_name: new_model}
                results, trained_models = trainer.train_and_evaluate(
                    X_train, y_train, X_test, y_test,
                    encoded_columns=encoded_columns,
                )
                
                current_score = float(results.loc[results.index[0], settings.hyperparam_tuning_optimization_metric ])
                all_results[model_name].append((params, results.loc[results.index[0]]))

                if current_score > best_score:
                    best_score = current_score
                    best_params = params
                    best_model_trained = trained_models[model_name]

        if best_params is not None:
            best_params['use_attention'] = use_attention
        best_models[model_name] = (best_model_trained, best_params)

        print(f"Best params for {model_name}: {best_params} with auc={best_score}")

        ExperimentConfig.update_model_hyperparams({model_name: (best_model_trained, best_params)})

    return best_models, all_results