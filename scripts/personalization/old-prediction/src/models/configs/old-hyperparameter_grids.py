curve_param_grids = {
    'DeepSurv': [
        {
            'num_nodes': [[256, 128, 64], [128, 64, 32], [128, 64], [64, 32], [64], [32]],
            'batch_norm': [True, False],
            'dropout': [0.1, 0.2, 0.3],
            'weight_decay': [1e-3, 5e-4, 1e-4],
            'lr': [0.001, 0.0005, 0.005, 0.01],
            'activation': ['elu', 'relu'],
            'optimizer': ['Adam', 'RMSprop'],
            'batch_size': [32, 64, 128],
            'epochs': [50, 100, 200, 500],
            'early_stopping_patience': [20, 30, 50]
        }
    ],
    'LogisticHazardModel': [
        {
            'num_nodes': [[256, 128, 64], [128, 64, 32],[128, 64], [64, 32], [64], [32]],
            'lr': [0.001, 0.0005, 0.01],
            'dropout': [0.1, 0.15, 0.2],
            'batch_size': [32, 64, 128],
            'early_stopping_patience': [20, 30, 50],
            'optimizer': ['Adam', 'RMSprop'], 
            'epochs' : [50, 100, 200, 500]
        }
    ],
    'DeepHitModel': [
        {
            'num_nodes': [[256, 128, 64], [128, 64, 32],[128, 64], [64, 32], [64], [32]],
            'activation': ['swish', 'elu', 'relu'],
            'alpha': [0.2, 0.3, 0.4],
            'sigma': [0.05, 0.1, 0.2],
            'weight_decay': [1e-3, 5e-4, 1e-4],
            'optimizer': ['Adam', 'RMSprop'],
            'dropout': [0.1, 0.15, 0.2],  
            'batch_size': [32, 64, 128],
            'epochs': [50, 100, 200, 500],
            'early_stopping_patience': [20, 30, 50]
        }
    ],
    'PCHazardModel': [
        {
            'num_nodes': [[256, 128, 64],[128, 64, 32], [128, 64], [64, 32], [64], [32]],
            'num_durations': [60, 80, 100, 120],
            'lr': [0.0005, 0.001, 0.01],
            'dropout': [0.1, 0.15, 0.2],
            'optimizer': ['Adam', 'RMSprop'],
            'batch_size': [32, 64, 128],
            'epochs': [50, 100, 200, 500],
            'early_stopping_patience': [20, 30, 50]
        }
    ],
    'MTLRModel': [
        {
            'num_nodes': [[256, 128, 64],[128, 64, 32], [128, 64], [64, 32], [64], [32]],
            'lr': [0.0005, 0.001, 0.01],
            'dropout': [0.1, 0.15, 0.2],
            'optimizer': ['Adam', 'RMSprop'],
            'batch_size': [32, 64, 128],
            'epochs': [50, 100, 200, 500],
            'early_stopping_patience': [20, 30, 50]
            
        }
    ],
    'AalenAdditive': [
        {
            'fit_intercept': [True],
            'alpha': [0.01, 0.05, 0.1],
            'coef_penalizer': [0.5, 1.0, 2.0, 5.0, 10.0],
            'smoothing_penalizer': [0.0, 0.5, 1.0],
        }
    ],
    'CoxPH': [
        {
            'alpha': [0, 0.05, 0.1, 0.2, 0.5, 1.0],
            'ties': ['breslow', 'efron'],
            'n_iter': [100, 200, 500],
            'tol': [1e-5, 1e-7, 1e-9],
        }
    ],
    'RandomSurvivalForest': [
        {
            'n_estimators': [50, 100, 200, 500],
            'max_depth': [5, 10, 20, 50],
            'min_samples_split': [10, 20, 50, 100],
            'min_samples_leaf': [5, 10, 15, 20, 30],
            'max_features': ['sqrt', 'log2', None]
        }
    ],
    'GradientBoosting': [
        {
            'learning_rate': [0.01, 0.05, 0.1],
            'n_estimators': [50, 100, 200, 300, 500],
            'max_depth': [3, 5, 10],
            'subsample': [0.8, 1.0],
            'min_samples_leaf': [5, 10, 20],
            'min_samples_split': [10, 20, 50],
            'max_features': ['sqrt', 'log2', None]
        }
    ],
}

days_param_grids = {
    'Ridge': [
        {
            'alpha': [0.1, 1.0, 10.0, 100.0],
            'solver': ['auto', 'svd', 'cholesky', 'lsqr']
        }
    ],
    'Lasso': [
        {
            'alpha': [0.001, 0.01, 0.1, 1.0],
            'max_iter': [1000, 5000]
        }
    ],
    'RandomForest': [
        {
            'n_estimators': [100, 200, 300, 500],
            'max_depth': [None, 10, 20, 30],
            'min_samples_split': [2, 5, 10],
            'min_samples_leaf': [1, 2, 4],
            'max_features': ['auto', 'sqrt', 'log2']
        }
    ],
    'GradientBoosting': [
        {
            'n_estimators': [50, 100, 200, 300, 500],
            'learning_rate': [0.01, 0.05, 0.1, 0.2],
            'max_depth': [3, 5, 10],
            'min_samples_split': [2, 5, 10, 20],
            'min_samples_leaf': [1, 2, 5, 10, 20],
            'max_features': ['auto', 'sqrt', 'log2']
        }
    ],
    'MLPRegressor': [
        {
            'hidden_layer_sizes': [(64, 32), (128, 64, 32), (32,), (128, 64, 64, 32), (32, 32)],
            'activation': ['relu', 'tanh'],
            'solver': ['adam', 'lbfgs'],
            'alpha': [1e-4, 1e-3, 1e-2],
            'learning_rate_init': [0.001, 0.01, 0.05]
        }
    ],
    'SVR_RBF': [
        {
            'C': [0.1, 1.0, 10.0],
            'gamma': ['scale', 'auto']
        }
    ],
    'XGBRegressor': [
        {
            'n_estimators': [50, 100, 200, 300, 500],
            'max_depth': [3, 5, 7, 10],
            'learning_rate': [0.01, 0.05, 0.1, 0.2],
            'subsample': [0.6, 0.8, 1.0]
        }
    ],
    'KNN': [
        {
            'n_neighbors': [3, 5, 7, 9, 11],
            'weights': ['uniform', 'distance'],
            'p': [1, 2]  # Manhattan vs. Euclidean
        }
    ]
}