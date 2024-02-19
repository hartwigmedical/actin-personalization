knn_cross_validation <- function(training_set, outcome_var, predictor_var, vfold) {
  
 knn_recipe <- recipe(training_set) %>%
    update_role(outcome_var, new_role = "outcome") %>%
    update_role(predictor_var, new_role = "predictor") %>%
    step_normalize(all_predictors()) 
  
 knn_spec <- nearest_neighbor(weight_func = "rectangular", neighbors = tune()) %>%
    set_engine("kknn") %>%
    set_mode("regression")
  
 knn_wkflw <- workflow() %>%
    add_recipe(knn_recipe) %>%
    add_model(knn_spec)
 
 v_fold <- vfold_cv(training_set, vfold)
 gridvals <- tibble(neighbors = seq(from = 1, to = 100, by = 2))

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
  
  return(knn_summary)
}

