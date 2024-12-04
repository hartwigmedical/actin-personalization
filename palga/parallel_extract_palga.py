import argparse
import json
import logging
import signal

from huggingface_hub import login
from joblib import Parallel, delayed
from openai import OpenAI
from pandas import read_csv

PROMPT_TEXT = 'Parse "{0}" into JSON.'
MODEL_NAME = "neuralmagic/Mistral-Nemo-Instruct-2407-FP8"
MAX_TOKENS = 750
MODEL_TEMPERATURE = 0
MODEL_TOP_K = 1
MODEL_TOP_P = 1

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__file__)


def generate_suggestions(input_series, output_dir, timeout_seconds, hf_token):
    login(token=hf_token)

    output = {}
    try:
        logger.info(f'Generating suggestions')
        results = Parallel(n_jobs=4)(delayed(_suggestion_for_input)(palga_input) for palga_input in input_series)
        output = {input_text: extracted for (input_text, extracted) in results}

    except TimeoutException:
        logger.warning(f'Failed to generate suggestions within limit of {timeout_seconds} seconds')
    finally:
        _write_output_to_file(output_dir, output)


def _write_output_to_file(output_dir, output):
    output_file = f'{output_dir}/extractions.json'
    logger.info(f'Writing suggestions for {len(output)} reports to {output_file}')
    with open(output_file, 'w') as out:
        json.dump(output, out)


def _suggestion_for_input(palga_input):
    json_schema = {
        "$defs": {
            "relevantText": {"type": "string", "comment": "The input text used to determine the appropriate value. This should always be provided when the value is not null"},
            "textInEnglish": {"type": "string", "comment": "The relevant input text translated to English"},
            "explanation": {"type": "string", "comment": "Describe how the value is specified by the input, when applicable"},
            "selfEvaluation": {"type": "string", "comment": "Evaluation of the explanation given for how the value was derived from the relevant text"},
            "confidence": {"type": "number", "comment": "Confidence level in extracted value as a percentage"},
            "nullableBoolean": {
                "type": "object",
                "properties": {
                    "relevantText": {"$ref": "#/$defs/relevantText"},
                    "textInEnglish": {"$ref": "#/$defs/textInEnglish"},
                    "value": {"type": ["boolean", "null"], "comment": "The inferred boolean value of this attribute, or null if this cannot be confidently extracted from the relevant text identified above"},
                    "explanation": {"$ref": "#/$defs/explanation"},
                    "selfEvaluation": {"$ref": "#/$defs/selfEvaluation"},
                    "confidence": {"$ref": "#/$defs/confidence"}
                },
                "required": ["value", "explanation"]
            }
        },
        "type": "object",
        "properties": {
            "hasBrafV600EMutation": {"$ref": "#/$defs/nullableBoolean", "comment": "Indicates the presence of a V-to-E substitution in codon 600 of gene BRAF"},
            "hasBrafMutation": {"$ref": "#/$defs/nullableBoolean", "comment": "Indicates the presence of any mutation in gene BRAF"},
            "hasKrasG12CMutation": {"$ref": "#/$defs/nullableBoolean", "comment": "Indicates the presence of a G-to-C substitution in codon 12 of gene KRAS"},
            "hasRasMutation": {"$ref": "#/$defs/nullableBoolean", "comment": "Indicates the presence of any mutation in the RAS family of genes"},
            "microsatelliteInstability": {"$ref": "#/$defs/nullableBoolean", "comment": "Indicates whether the tumor is microsatellite-unstable"},
            "tumorMutationalBurden": {
                "type": "object",
                "properties": {
                    "relevantText": {"$ref": "#/$defs/relevantText"},
                    "textInEnglish": {"$ref": "#/$defs/textInEnglish"},
                    "value": {"type": ["number", "null"], "comment": "The inferred numerical value of this attribute, or null if this cannot be confidently extracted from the relevant text identified above"},
                    "explanation": {"$ref": "#/$defs/explanation"},
                    "selfEvaluation": {"$ref": "#/$defs/selfEvaluation"},
                    "confidence": {"$ref": "#/$defs/confidence"}
                },
                "required": ["value", "explanation"]
            },
        },
        "required": []
    }
    messages = [
        {
            "role": "system",
            "content": "You are a helpful medical coding assistant. Extract the necessary fields into a valid JSON object that adheres to the following schema:" +
                "```json\n" + str(json_schema) + "\n```\n" +
                "Use the following stepwise approach to extract each field:\n" +
                "1. If possible, identify the portion of the provided report text that specifies the value of the field and store this as \"relevantText\" in the response. " +
                "The \"comment\" provided for each field in the JSON schema describes what the field is meant to capture.\n" +
                "2. If relevant text was extracted, translate it to English and store in \"textInEnglish\".\n" +
                "3. Explain how the value can be unambiguously determined from the input in \"explanation\".\n" +
                "4. Set \"value\" if it was determined sucessfully, or set to null otherwise.\n" +
                "5. If a value was determined, reflect on how it was extracted from the relevant input based on the provided explanation and evaluate its correctness. Store the evaluation in \"selfEvaluation\".\n" +
                "6. Express your confidence in the extracted value as a percentage and store in \"confidence\"."
        },
        {
            "role": "user",
            "content": "Please summarize the following report delimited by triple-quotes: \"\"\"" + palga_input + "\"\"\""
        }
    ]
    client = OpenAI(base_url="http://localhost:8000/v1", api_key="key")

    response = client.chat.completions.create(
        model=MODEL_NAME,
        messages=messages,
        max_tokens=8192,
        temperature=MODEL_TEMPERATURE,
        top_p=MODEL_TOP_P,
        extra_body=dict(guided_json=json_schema)
    )
    return palga_input, response.choices[0].message.content


class TimeoutException(Exception):
    pass


def timeout_handler(signum, frame):
    raise TimeoutException


def main():
    parser = argparse.ArgumentParser()

    parser.add_argument(
        '--palga_report_csv', required=True, help='Path to the palga report file'
    )
    parser.add_argument(
        '--output_dir', required=True, help='Directory where output will be written'
    )
    parser.add_argument(
        '--start_index', type=int, required=True, help='First line to parse'
    )
    parser.add_argument(
        '--stop_index', type=int, required=True, help='Last line to parse'
    )
    parser.add_argument(
        '--hf_token', required=True, help='Hugging Face API token'
    )
    parser.add_argument(
        '--timeout_seconds', type=int, required=False, help='Seconds after which generation will be gracefully interrupted'
    )

    args = parser.parse_args()

    match args.timeout_seconds:
        case 0:
            logger.info('Timeout specified as 0 seconds; exiting')
            exit(0)
        case None:
            pass
        case _:
            signal.signal(signal.SIGALRM, timeout_handler)
            signal.alarm(args.timeout_seconds)

    try:
        df = read_csv(args.palga_report_csv, sep=";")
        logger.info(f'Read {len(df)} row(s) from {args.palga_report_csv}')
        df['report'] = df['Microscopie'] + "\n" + df['Conclusie']

        generate_suggestions(
            df['report'][args.start_index:args.stop_index],
            args.output_dir,
            args.timeout_seconds,
            args.hf_token
        )
    except TimeoutException:
        logger.warning(f'Failed to complete suggestion process within limit of {args.timeout_seconds} seconds')


if __name__ == "__main__":
    main()

