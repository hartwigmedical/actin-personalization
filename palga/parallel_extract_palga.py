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
MAX_TOKENS = 8192
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
        results = Parallel(n_jobs=2)(delayed(_suggestion_for_input)(palga_input) for palga_input in input_series)
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
            "selfEvaluation": {"type": "string", "comment": "Evaluation of the explanation given for how the value was determined from the relevant text, and whether any assumptions were made"},
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
            "hasBrafV600EMutation": {
                "$ref": "#/$defs/nullableBoolean",
                "comment": "True if the report indicates a V-to-E substitution in codon 600 of gene BRAF. " +
                           "False if the report indicates another mutation at this position, no mutation at this " +
                           "position, no mutations in BRAF exon 15, or no BRAF mutations. In all other cases, this " +
                           "field should be null."
            },
            "hasBrafMutation": {
                "$ref": "#/$defs/nullableBoolean",
                "comment": "True if the report indicates any mutation in gene BRAF. " +
                           "False if the report indicates no mutations in this gene. " +
                           "In all other cases, this field should be null."
            },
            "hasKrasG12CMutation": {
                "$ref": "#/$defs/nullableBoolean",
                "comment": "True if the report indicates a G-to-C substitution in codon 12 of gene KRAS. " +
                           "False if the report indicates another mutation at this position, no mutation at this " +
                           "position, no mutation in KRAS exon 2, or no KRAS mutations. In all other cases, this " +
                           "field should be null."
            },
            "hasRasMutation": {
                "$ref": "#/$defs/nullableBoolean",
                "comment": "True if the report indicates any mutation in the RAS family of genes. " +
                           "False if the report indicates that there are no mutations in any RAS genes. " +
                           "In all other cases, this field should be null."
            },
            "microsatelliteInstability": {
                "$ref": "#/$defs/nullableBoolean",
                "comment": "True if the report indicates that the tumor is microsatellite-unstable or MMR-deficient. " +
                           "False if the report indicates that the tumor is microsatellite-stable, or if MMR " +
                           "proteins MLH1, PMS2, MSH2, and MSH6 are detected." +
                           "In all other cases, this field should be null."
            },
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
                "4. Set \"value\" if it was determined successfully, or set to null otherwise.\n" +
                "5. If a value was determined, reflect on how it was extracted from the relevant input based on the provided explanation and evaluate its correctness. Store the evaluation in \"selfEvaluation\".\n" +
                "6. Express your confidence in the extracted value as a percentage and store in \"confidence\"."
        },
        {
            "role": "user",
            "content": "Please summarize the following report delimited by triple-quotes: \"\"\"\n" +
                       "Geen mutaties gedetecteerd in B-Raf codon 600. " +
                       "Door middel MMR eiwitaankleuring werd geen deficientie in mismatch repair genen aangetoond." +
                       "\n\"\"\""
        },
        {
            "role": "assistant",
            "content": json.dumps({
                "hasBrafV600EMutation": {
                    'relevantText': 'Geen mutaties gedetecteerd in B-Raf codon 600.',
                    'textInEnglish': 'No mutations detected in B-Raf codon 600.',
                    'value': False,
                    'explanation': 'The report explicitly states that no mutations were detected in B-Raf codon 600.',
                    'selfEvaluation': 'The explanation is clear and accurate based on the provided text.',
                    'confidence': 100
                },
                "hasBrafMutation": {
                    'relevantText': 'Geen mutaties gedetecteerd in B-Raf codon 600.',
                    'textInEnglish': 'No mutations detected in B-Raf codon 600.',
                    'value': False,
                    'explanation': 'The report indicates that no mutations were detected in the B-Raf gene.',
                    'selfEvaluation': 'The explanation is accurate and reflects the information given in the report.',
                    'confidence': 100
                },
                "hasKrasG12CMutation": {
                    'value': None,
                    'explanation': 'The report does not provide information about KRAS.',
                    'selfEvaluation': 'The explanation is accurate.',
                },
                "hasRasMutation": {
                    'value': None,
                    'explanation': 'The report does not provide information about RAS genes',
                    'selfEvaluation': 'The explanation accurately reflects the information given in the report.',
                },
                "microsatelliteInstability": {
                    'relevantText': 'Door middel MMR eiwitaankleuring werd geen deficientie in mismatch repair genen aangetoond.',
                    'textInEnglish': 'MMR protein staining did not demonstrate a deficiency in mismatch repair genes.',
                    'value': False,
                    'explanation': 'The report states that no mismatch repair gene deficiency was observed, ' +
                                   'which implies microsatellite stability',
                    'selfEvaluation': 'This determination required inferring microsatellite stability from mismatch repair gene observations.',
                    'confidence': 85
                },
                "tumorMutationalBurden": {
                    'value': None,
                    'explanation': 'The report does not provide information about TMB',
                    'selfEvaluation': 'The explanation accurately reflects the information given in the report.',
                }
            })
        },
        {
            "role": "user",
            "content": "Please summarize the following report delimited by triple-quotes: \"\"\"\n" +
                       str(palga_input) + "\n\"\"\""
        }
    ]
    client = OpenAI(base_url="http://localhost:8000/v1", api_key="key")

    response = client.chat.completions.create(
        model=MODEL_NAME,
        messages=messages,
        max_tokens=MAX_TOKENS,
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
        s = df['Microscopie'].fillna('') + "\n" + df['Conclusie'].fillna('')

        generate_suggestions(
            s[s != "\n"][args.start_index:args.stop_index],
            args.output_dir,
            args.timeout_seconds,
            args.hf_token
        )
    except TimeoutException:
        logger.warning(f'Failed to complete suggestion process within limit of {args.timeout_seconds} seconds')


if __name__ == "__main__":
    main()

