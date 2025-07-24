import argparse

from predictor import *

import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)


def apply_settings_from_args(args):
    settings = Settings()
    
    settings.save_path = args.trained_path
    settings.experiment_type = 'treatment_drug'
    settings.standardize = True
    settings.normalize = False
    settings.use_gate = True
    settings.save_models = False

    settings.configure_data_settings()
    settings.configure_model_settings()

    logger.info(f"Configured settings with outcome={settings.outcome}, trained_path={settings.save_path}")
    return settings
    
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("input_path", help="Path to input JSON file")
    parser.add_argument("output_path", help="Path to output JSON file")
    parser.add_argument("--trained_path", help= "Path to folder that contains trained model + preprocessors")
    parser.add_argument("--treatment_config", help="Path to treatment combination JSON")
    args = parser.parse_args()
    
    logger.info("Starting treatment prediction script")
    settings = apply_settings_from_args(args)

    try:
        logger.info(f"Loading patient data from {args.input_path}")
        with open(args.input_path, 'r') as f:
            patient_data = json.load(f)
    except Exception as e:
        logger.error(f"Failed to load input data: {e}")
        raise

    try:
        logger.info(f"Loading treatment configuration from {args.treatment_config}")
        with open(args.treatment_config, 'r') as f:
            treatment_config = json.load(f)
    except Exception as e:
        logger.error(f"Failed to load treatment config: {e}")
        raise
    
    logger.info("Running predictions...")
    result = predict_treatment_scenarios(
        patient_data=patient_data,
        trained_path=args.trained_path,
        valid_treatment_combinations=treatment_config, 
        settings=settings
    )
    
    logger.info(f"Saving results to {args.output_path}")
    with open(args.output_path, 'w') as f:
        json.dump(result, f)
        
    logger.info("Prediction script completed successfully")

if __name__ == "__main__":
    main()
