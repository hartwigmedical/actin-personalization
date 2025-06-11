import sys
import json
import argparse
import pandas as pd
from utils.settings import settings
from models.predictor import *

def apply_settings_from_args(args):
    settings.outcome = args.outcome or 'OS'
    settings.save_path = args.trained_path or settings.save_path

    settings.experiment_type = 'treatment_drug'
    settings.standardize = True
    settings.normalize = False
    settings.use_gate = True
    settings.save_models = False

    settings.configure_data_settings()
    settings.configure_model_settings()
    
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("input_path", help="Path to input JSON file")
    parser.add_argument("output_path", help="Path to output JSON file")
    parser.add_argument("--trained_path", help= "Path to folder that contains trained model + preprocessors")
    parser.add_argument("--outcome", choices=["OS", "PFS"], help="Prediction outcome")
    parser.add_argument("--treatment_config", help="Path to treatment combination JSON")
    args = parser.parse_args()
    
    apply_settings_from_args(args)

    try:
        with open(args.input_path, 'r') as f:
            patient_data = json.load(f)
    except Exception as e:
        raise RuntimeError(f"Failed to load input data: {e}")

    try:
        with open(args.treatment_config, 'r') as f:
            treatment_config = json.load(f)
    except Exception as e:
        raise RuntimeError(f"Failed to load treatment config: {e}")

    result = predict_treatment_scenarios(
        patient_data=patient_data,
        trained_path=args.trained_path,
        valid_treatment_combinations=treatment_config
    )

    with open(args.output_path, 'w') as f:
        json.dump(result, f)

if __name__ == "__main__":
    main()
