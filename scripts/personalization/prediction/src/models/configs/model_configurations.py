from src.models.survival_models import *

os_configs = {
    'DeepSurv': (
        DeepSurv,
        {
            'num_nodes': [64, 32],
            'batch_norm': True,
            'dropout': 0.1,
            'weight_decay': 0.001,
            'lr': 0.01,
            'activation': 'elu',
            'optimizer': 'Adam',
            'batch_size': 32,
            'epochs': 50,
            'early_stopping_patience': 30
        }
    ),
    'LogisticHazardModel': (
        LogisticHazardModel,
        {
            'num_nodes': [64, 32],
            'lr': 0.0005,
            'dropout': 0.15,
            'batch_size': 64,
            'early_stopping_patience': 50,
            'optimizer': 'Adam',
            'epochs': 50
       
        }
    ),
    'DeepHitModel': (
        DeepHitModel,
        {
            'num_nodes': [128, 64, 32],
            'activation': 'swish',
            'alpha': 0.2,
            'sigma': 0.05,
            'weight_decay': 0.001,
            'optimizer': 'Adam',
            'dropout': 0.2,
            'batch_size': 32,
            'epochs': 200,
            'early_stopping_patience': 30
        }
    ),
    'PCHazardModel': (
        PCHazardModel,
        {
            'num_nodes': [64, 32],
            'num_durations': 120,
            'lr': 0.0005,
            'dropout': 0.15,
            'optimizer': 'RMSprop',
            'batch_size': 64,
            'epochs': 500,
            'early_stopping_patience': 50
        }
    ),
    'MTLRModel': (
        MTLRModel,
        {
           'num_nodes': [64, 32],
            'lr': 0.0005,
            'dropout': 0.2,
            'optimizer': 'Adam',
            'batch_size': 128,
            'epochs': 500,
            'early_stopping_patience': 20
        }
    ),
    'CoxPH': (
        CoxPHModel,
        {
            'alpha': 1.0,
            'ties': 'breslow',
            'n_iter': 500,
            'tol': 1e-05,
        }
    ),
    'RandomSurvivalForest': (
        RandomSurvivalForestModel,
        {
            'n_estimators': 200,
            'max_depth': 50,
            'min_samples_split': 50,
            'min_samples_leaf': 5,
            'max_features': None,
            'random_state': 42,
        }
    ),
    'GradientBoosting': (
        GradientBoostingSurvivalModel,
        {
            'learning_rate': 0.1,
            'n_estimators': 50,
            'max_depth': 3,
            'subsample': 1.0,
            'min_samples_leaf': 5,
            'min_samples_split': 20,
            'max_features': None,
            'random_state': 42,
        }
    ),
}

pfs_configs = {
    'DeepSurv': (
        DeepSurv,
        {
            'num_nodes': [64],
            'batch_norm': True,
            'dropout': 0.1,
            'weight_decay': 0.001,
            'lr': 0.01,
            'activation': 'elu',
            'optimizer': 'Adam',
            'batch_size': 32,
            'epochs': 50,
            'early_stopping_patience': 30
        }
    ),
    'LogisticHazard': (
        LogisticHazardModel,
        {
            'num_nodes': [64, 32],
            'lr': 0.0005,
            'dropout': 0.15,
            'batch_size': 64,
            'early_stopping_patience': 50,
            'optimizer': 'Adam',
            'epochs': 50
        }
    ),
    'DeepHit': (
        DeepHitModel,
        {
            'num_nodes': [128, 64, 32],
            'activation': 'relu',
            'alpha': 0.2,
            'sigma': 0.05,
            'weight_decay': 0.001,
            'optimizer': 'Adam',
            'dropout': 0.2,
            'batch_size': 128,
            'epochs': 50,
            'early_stopping_patience': 20
        }
    ),
    'PCHazard': (
        PCHazardModel,
        {
            'num_nodes': [64, 32],
            'num_durations': 100,
            'lr': 0.001,
            'dropout': 0.15,
            'optimizer': 'RMSprop',
            'batch_size': 64,
            'epochs': 500,
            'early_stopping_patience': 20
        }
    ),
    'MTLR': (
        MTLRModel,
        {
            'num_nodes': [128, 64, 32],
            'lr': 0.001,
            'dropout': 0.15,
            'optimizer': 'Adam',
            'batch_size': 64,
            'epochs': 100,
            'early_stopping_patience': 20
        }
    ),
    'CoxPH': (
        CoxPHModel,
        {
            'alpha': 1.0,
            'ties': 'breslow',
            'n_iter': 100,
            'tol': 1e-5,
        }
    ),
    'RandomSurvivalForest': (
        RandomSurvivalForestModel,
        {
            'n_estimators': 200,
            'max_depth': 20,
            'min_samples_split': 50,
            'min_samples_leaf': 10,
            'max_features': 'sqrt',
            'random_state': 42,
        }
    ),
    'GradientBoosting': (
        GradientBoostingSurvivalModel,
        {
            'learning_rate': 0.05,
            'n_estimators': 300,
            'max_depth': 5,
            'subsample': 1.0,
            'min_samples_leaf': 10,
            'min_samples_split': 50,
            'max_features': 'log2',
            'random_state': 42,
        }
    ),
}
