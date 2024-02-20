knn_cross_validation <- function(training_set, outcome_var, predictor_vars, vfold, strata) {
  
  training_set_processed <- training_set %>% subset(select=c(outcome_var,  predictor_vars))
  
  knn_recipe <- recipe(training_set_processed) %>%
    update_role(outcome_var, new_role = "outcome")
  
  for (i in 1:length(predictor_vars)) {
  knn_recipe <- knn_recipe %>% update_role(predictor_vars[i], new_role = "predictor")
  }

  knn_recipe <- knn_recipe %>% step_normalize(all_predictors()) 

  knn_spec <- nearest_neighbor(weight_func = "rectangular", neighbors = tune()) %>%
    set_engine("kknn") %>%
    set_mode("regression")
  
  knn_wkflw <- workflow() %>%
    add_recipe(knn_recipe) %>%
    add_model(knn_spec)
 
  if (missing(strata)) {
    v_fold <- vfold_cv(training_set_processed, vfold)
  } else {
    v_fold <- vfold_cv(training_set_processed, vfold, strata = strata)
  }
  
  gridvals <- tibble(neighbors = seq(from = 1, to = 300, by = 2))

  knn_cross_results <- knn_wkflw %>% 
      tune_grid(resamples = v_fold, grid = gridvals) %>%
      collect_metrics() %>%
      dplyr::filter(.metric == "rmse") %>%
      dplyr::arrange(mean)

  outcome_list <- list(knn_recipe, knn_cross_results)
  return(outcome_list)
}

knn_cross_validation_optimal_k <- function(knn_cross_results) {
  
  knn_cross_results_min <- knn_cross_results %>% dplyr::filter(mean == min(mean))
  knn_cross_results_min_k <- knn_cross_results_min %>% pull(neighbors)

  return(knn_cross_results_min_k)
}

knn_run_on_test_set <- function(training_set, test_set, k, recipe, truth_var) {
  knn_spec <- nearest_neighbor(weight_func ="rectangular", neighbors = k) %>%
    set_engine("kknn") %>%
    set_mode ("regression")
  
  knn_fit <- workflow() %>%
    add_recipe(recipe) %>%
    add_model(knn_spec) %>%
    fit(data = training_set)
  
  knn_summary <- knn_fit %>% 
    predict(test_set) %>%
    bind_cols(test_set) %>%
    metrics(truth=truth_var, estimate=.pred) %>%
    dplyr::filter(.metric=='rmse')
  
  outcome_list <- list(knn_fit, knn_summary)
  return(outcome_list)
}