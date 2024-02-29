## VARIOUS FUNCTIONS FOR ACTIN CPCT DATA ANALYSIS---------------------------------------------

# K-NEAREST NEIGHBOURS------------------------------------------------------------------
knn_cross_validation <- function(training_set, outcome_var, predictor_vars, vfold, strata, kmax) {
  
  training_set_processed <- training_set %>% subset(select=c(outcome_var,  predictor_vars))
  
  knn_recipe <- recipe(training_set_processed) %>%
    update_role(outcome_var, new_role = "outcome")
  
  for (i in 1:length(predictor_vars)) {
  knn_recipe <- knn_recipe %>% update_role(predictor_vars[i], new_role = "predictor")
  }

  knn_recipe <- knn_recipe %>% step_normalize(all_numeric_predictors()) 

  knn_spec <- nearest_neighbor(weight_func = "rectangular", neighbors = tune()) %>%
    set_engine("kknn") %>%
    set_mode("regression")
  
  knn_wkflw <- workflow() %>%
    add_recipe(knn_recipe) %>%
    add_model(knn_spec)
 
  if (missing(strata)) {
    v_fold <- vfold_cv(data=training_set_processed, v=vfold)
  } else {
    v_fold <- vfold_cv(data=training_set_processed, v=vfold, strata=strata)
  }
  
  if (missing(kmax)) {
    gridvals <- tibble(neighbors = seq(from = 1, to = 200, by = 2))
  } else {
    gridvals <- tibble(neighbors = seq(from = 1, to = kmax, by = 2))
  }
  
  knn_cross_results <- knn_wkflw %>% 
      tune_grid(resamples = v_fold, grid = gridvals) %>%
      collect_metrics() %>%
      dplyr::filter(.metric == "rmse") %>%
      dplyr::arrange(mean)

  outcome_list <- list(knn_recipe, knn_cross_results)
  names(outcome_list) <- c("knn cv recipe", "knn cv results")
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
  names(outcome_list) <- c("knn fit", "knn fit summary")
  return(outcome_list)
}

# LINEAR REGRESSION MODEL ------------------------------------------------------------------
linear_regression_model <- function(training_set, test_set, outcome_var, predictor_vars, truth_var) {
  
  training_set_processed <- training_set %>% subset(select=c(outcome_var,  predictor_vars))
  
  lm_recipe <- recipe(training_set_processed) %>%
    update_role(outcome_var, new_role = "outcome")
  
    for (i in 1:length(predictor_vars)) {
      lm_recipe <- lm_recipe %>% update_role(predictor_vars[i], new_role = "predictor")
    }
  
  lm_spec <- linear_reg() %>%
    set_engine("lm") %>%
    set_mode("regression")
  
  lm_fit <- workflow() %>%
    add_recipe(lm_recipe) %>%
    add_model(lm_spec) %>%
    fit(data = training_set)
  
  lm_test_results <- lm_fit %>%
    predict(test_set) %>%
    bind_cols(test_set) %>%
    metrics(truth = truth_var, estimate = .pred)
  
  outcome_list <- list(lm_fit, lm_test_results)
  names(outcome_list) <- c("lm fit", "lm test results")
  return(outcome_list)
}

# SURVIVAL PLOTS ------------------------------------------------------------------
generate_survival_plot <- function(data_set, survival_var, censor_status_var, split_var, type, event_at_time) {
  
  x_lab_surv <- "Time (days)"
  y_lab_surv <- "Proportion"

  if(missing(type)) {
    type = "?"
  }
  
  if (missing(split_var)) {
    surv_fit_results <- survfit(Surv(as.numeric(survival_var), as.numeric(censor_status_var)) ~ 1, data = data_set)
    
    surv_plot <- survfit2(Surv(survival_var, censor_status_var) ~ 1, data = data_set) %>% 
      ggsurvfit() + labs(
        x = x_lab_surv,
        y = y_lab_surv
      ) + add_confidence_interval() + add_risktable() + ggtitle(paste0("Survival plot (",type,"), p=",round(surv_fit_sig$pvalue,3)))
    
    if(!missing(event_at_time)) {
      surv_fit_event_at_time <- summary(survfit(Surv(survival_var, censor_status_var) ~ 1, data = data_set), times = event_at_time)
      outcome_list <- list(surv_fit_results, surv_plot, surv_fit_event_at_time)
      names(outcome_list) <- c("surv_fit_results", "surv_plot", "surv_fit_event_at_time")
      return(outcome_list)
    }
    
    outcome_list <- list(surv_fit_results, surv_plot)
    names(outcome_list) <- c("surv_fit_results", "surv_plot")
    return(outcome_list)
    
  } else {
    surv_fit_results <- survfit(Surv(as.numeric(survival_var), as.numeric(censor_status_var)) ~ split_var, data = data_set)
    surv_fit_sig <- survdiff(Surv(survival_var, censor_status_var) ~ split_var, data = data_set)
    
    surv_plot <- survfit2(Surv(survival_var, censor_status_var) ~ split_var, data = data_set) %>% 
      ggsurvfit() + labs(
        x = x_lab_surv,
        y = y_lab_surv
      ) + add_confidence_interval() + add_risktable() + ggtitle(paste0("Survival plot (",type,"), p=",round(surv_fit_sig$pvalue,3)))
    
    if(!missing(event_at_time)) {
      surv_fit_event_at_time <- summary(survfit(Surv(survival_var, censor_status_var) ~ split_var, data = data_set), times = event_at_time)
      
      outcome_list <- list(surv_fit_results, surv_fit_sig, surv_plot, surv_fit_event_at_time)
      names(outcome_list) <- c("surv_fit_results", "surv_fit_sig", "surv_plot", "surv_fit_event_at_time")
      return(outcome_list)
      }
    
    outcome_list <- list(surv_fit_results, surv_fit_sig, surv_plot)
    names(outcome_list) <- c("surv_fit_results", "surv_fit_sig", "surv_plot")
    return(outcome_list)
  }
}
