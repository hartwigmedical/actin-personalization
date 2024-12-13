from src.models.survival_models import *

os_configs = {
    'DeepSurv': (
        DeepSurv, 
        { 
            'num_nodes': [256, 128, 64],
            'batch_norm': False,
            'dropout': 0.1,
            'weight_decay': 0.0001,
            'lr': 0.001,
            'activation': 'relu',
            'optimizer': 'RMSprop',
            'batch_size': 32,
            'epochs': 500,
        }
    ),
    'LogisticHazardModel': (
        LogisticHazardModel, 
        {
            'num_nodes': [256, 128, 64],
            'lr': 0.0005,
            'dropout': 0.15,
            'batch_size': 128,
            'epochs': 500,
            'early_stopping_patience': 50,
            'optimizer': 'RMSprop',
        }
    ),
    'DeepHitModel': (
        DeepHitModel, 
        {
            'num_nodes': [64, 32],
            'activation': 'elu',
            'alpha': 0.2,
            'sigma': 0.1,
            'weight_decay': 0.0001,
            'optimizer': 'Adam',
            'dropout': 0.1,
            'lr': 1e-3,
        }
    ),
    'PCHazardModel': (
        PCHazardModel, 
        {  
            'num_nodes': [256, 128, 64],
            'batch_norm': True,
            'dropout': 0.15,
            'lr': 0.01,
            'batch_size': 32,
            'epochs': 500,
            'optimizer': 'Adam',
        }
    ),
    'MTLRModel': (
        MTLRModel, 
        {
            'num_nodes': [64, 32],
            'lr': 0.01,
            'dropout': 0.2,
            'optimizer': 'RMSprop',
        }
    ),

    'AalenAdditive': (
        AalenAdditiveModel, 
        {
            'fit_intercept': True,
            'alpha': 0.05,
            'coef_penalizer': 10.0,
            'smoothing_penalizer': 0.0,
        }
    ),
    'CoxPH': (
        CoxPHModel, 
        {
            'alpha': 1.0,
            'ties': 'breslow',
            'n_iter': 100,
            'tol': 1e-7,
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
            'n_estimators': 500,
            'max_depth': 3,
            'subsample': 1.0,
            'min_samples_leaf': 5,
            'min_samples_split': 20,
            'max_features': 'sqrt',
        }
    ),
}

pfs_configs = {
    'DeepSurv': (
        DeepSurv, 
        {
            'num_nodes': [256, 128, 64],
            'batch_norm': True,
            'dropout': 0.2,
            'weight_decay': 0.001,
            'lr': 0.01,
            'activation': 'elu',
            'optimizer': 'RMSprop',
        }
    ),
    'LogisticHazardModel': (
        LogisticHazardModel, 
        {
            'num_nodes': [64],
            'lr': 0.0005,
            'dropout': 0.2,
            'batch_size': 128,
            'early_stopping_patience': 50,
            'optimizer': 'Adam',
        }
    ),
    'DeepHitModel': (
        DeepHitModel, 
        {
            'num_nodes': [64],
            'activation': 'swish',
            'alpha': 0.2,
            'sigma': 0.1,
            'weight_decay': 0.001,
            'optimizer': 'Adam',
            'dropout': 0.2,  
        }
    ),
    'PCHazardModel': (
        PCHazardModel, 
        {
            'num_nodes': [128, 64],
            'dropout': 0.2,
            'lr': 0.01,
            'optimizer': 'Adam',
        }
    ),
    'MTLRModel': (
        MTLRModel, 
        {
            'num_nodes': [64, 32],
            'lr': 0.001,
            'dropout': 0.2,
            'optimizer': 'Adam',
        }
    ),
    'CoxPH': (
        CoxPHModel, 
        {
            'alpha': 1.0,
            'ties': 'breslow',
            'n_iter': 200,
            'tol': 1e-7,
        }
    ),
    'AalenAdditive': (
        AalenAdditiveModel, 
        {
            'fit_intercept': True,
            'alpha': 0.05,
            'coef_penalizer': 10.0,
            'smoothing_penalizer': 0.0,
        }
    ),
    'RandomSurvivalForest': (
        RandomSurvivalForestModel, 
        {
            'n_estimators': 500,
            'max_depth': 20,
            'min_samples_split': 100,
            'min_samples_leaf': 10,
            'max_features': None,
        }
    ),
    'GradientBoosting': (
        GradientBoostingSurvivalModel, 
        {
            'learning_rate': 0.05,
            'n_estimators': 500,
            'max_depth': 3,
            'subsample': 0.8,
            'min_samples_leaf': 20,
            'min_samples_split': 20,
            'max_features': None,
        }
    ),
}
