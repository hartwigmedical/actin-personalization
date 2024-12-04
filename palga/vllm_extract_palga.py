import argparse
import json
import logging
import signal

from huggingface_hub import login
from pandas import read_csv
from transformers import AutoTokenizer
from vllm import LLM
from vllm.sampling_params import SamplingParams

PROMPT_TEXT = 'Parse "{0}" into JSON.'
MAX_TOKENS = 750
MODEL_TEMPERATURE = 0
MODEL_TOP_K = 1
MODEL_TOP_P = 1


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__file__)


def generate_suggestions(model_path, template_dir, input_series, output_dir, timeout_seconds):
    login(token=HF_TOKEN)

    model_name = "neuralmagic/Mistral-Nemo-Instruct-2407-FP8"
    tokenizer = AutoTokenizer.from_pretrained(model_name)

    model = LLM(
        model=model_name,
        # tokenizer_mode="mistral",
        # trust_remote_code=True,
        # load_format="mistral",
        #config_format="mistral",
        # quantization="fp8"
        #hf_overrides={"token": HF_TOKEN}
    )

    output = {}
    try:
        logger.info(f'Generating suggestions')
        for palga_input in input_series:
            output[palga_input] = _suggestion_for_input(model, tokenizer, palga_input)

    except TimeoutException:
        logger.warning(f'Failed to generate suggestions within limit of {timeout_seconds} seconds')
    finally:
        _write_output_to_file(output_dir, output)


def _write_output_to_file(output_dir, output):
    output_file = f'{output_dir}/extractions.json'
    logger.info(f'Writing suggestions for {len(output)} reports to {output_file}')
    with open(output_file, 'w') as out:
        json.dump(output, out)


def _suggestion_for_input(model, tokenizer, palga_input):
    messages = [
        {
            "role": "system",
            "content": "You are a helpful medical coding assistant that produces a valid JSON object " +
            "summarizing the contents of each report",
        },
        {"role": "user", "content": f"Please extract a report summary from the following: {palga_input}"},
    ],
    prompts = tokenizer.apply_chat_template(messages, tokenize=False)

    sampling_params = SamplingParams(temperature=MODEL_TEMPERATURE, top_p=MODEL_TOP_P, max_tokens=8192)
    response = model.chat(
        messages=prompts,
        response_format={
            "type": "json_object",
            "schema": {
                "type": "object",
                "properties": {
                    "mutations": {"type": "array", "items": {"type": "object", "properties": {
                        "gene": {"type": "string"},
                        "codon": {"type": "integer"},
                    }}},
                    "wildTypeGenes": {"type": "array", "items": {"type": "string"}},
                    "isMSI": {"type": "boolean"}
                }
            },
        },
        sampling_params=sampling_params
    )
    return response[0].outputs[0].text


class TimeoutException(Exception):
    pass


def timeout_handler(signum, frame):
    raise TimeoutException


def main():
    parser = argparse.ArgumentParser()

    parser.add_argument(
        '--model_path', required=True, help='Path to the directory which contains the LLM'
    )
    parser.add_argument(
        '--palga_report_csv', required=True, help='Path to the palga report file'
    )
    parser.add_argument(
        '--template_dir', required=True, help='Path to the prompt template directory'
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
            args.model_path,
            args.template_dir,
            df['report'][args.start_index:args.stop_index],
            args.output_dir,
            args.timeout_seconds
        )
    except TimeoutException:
        logger.warning(f'Failed to complete suggestion process within limit of {args.timeout_seconds} seconds')


if __name__ == "__main__":
    main()

