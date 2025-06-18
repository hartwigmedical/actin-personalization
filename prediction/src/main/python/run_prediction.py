import json
import argparse

from utils.settings import config_settings
from models.predictor import *

import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)


def apply_settings_from_args(args):
    config_settings.outcome = args.outcome or 'OS'
    config_settings.save_path = args.trained_path or config_settings.save_path

    config_settings.experiment_type = 'treatment_drug'
    config_settings.standardize = True
    config_settings.normalize = False
    config_settings.use_gate = True
    config_settings.save_models = False

    config_settings.configure_data_settings()
    config_settings.configure_model_settings()
    
    logger.info(f"Configured settings with outcome={config_settings.outcome}, trained_path={config_settings.save_path}")
    
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("input_path", help="Path to input JSON file")
    parser.add_argument("output_path", help="Path to output JSON file")
    parser.add_argument("--trained_path", help= "Path to folder that contains trained model + preprocessors")
    parser.add_argument("--outcome", choices=["OS", "PFS"], help="Prediction outcome")
    parser.add_argument("--treatment_config", help="Path to treatment combination JSON")
    args = parser.parse_args()
    
    logger.info("Starting treatment prediction script")
    apply_settings_from_args(args)

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
        valid_treatment_combinations=treatment_config
    )
    
    logger.info(f"Saving results to {args.output_path}")
    with open(args.output_path, 'w') as f:
        json.dump(result, f)
        
    logger.info("Prediction script completed successfully")

if __name__ == "__main__":
    main()
