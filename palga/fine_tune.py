import argparse
import json

from datasets import Dataset
from transformers import AutoTokenizer
from trl import SFTConfig, SFTTrainer, apply_chat_template
from unsloth import FastLanguageModel

ALPHA_SCALING_FACTOR = 16

RANK = 16

MAX_TOKENS = 8192
MODEL_NAME = "unsloth/Mistral-Nemo-Instruct-2407-bnb-4bit"  # "neuralmagic/Mistral-Nemo-Instruct-2407-FP8"

JSON_SCHEMA = {
    "$defs": {
        "nullableBoolean": {
            "type": "object",
            "properties": {
                "value": {"type": ["boolean", "null"], "comment": "The inferred boolean value of this attribute, or null if this cannot be confidently extracted from the relevant text identified above"},
            },
            "required": ["value"]
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
        }
    },
    "required": []
}

PROMPT = (
        "You are a helpful medical coding assistant. Extract the necessary fields into a valid JSON object that adheres to the following schema:" +
                   "```json\n" + str(JSON_SCHEMA) + "\n```\n"
)

def fine_tune(hf_token, training_data_json, output_dir):
    # Load model
    model, tokenizer = FastLanguageModel.from_pretrained(
        model_name=MODEL_NAME,
        max_seq_length=MAX_TOKENS,
        dtype=None,  # None for auto detection. Float16 for Tesla T4, V100, Bfloat16 for Ampere+
        load_in_4bit=True,  # Use 4bit quantization to reduce memory usage. Can be False
        token = hf_token # use one if using gated models like meta-llama/Llama-2-7b-hf
    )

    # Do model patching and add fast LoRA weights
    model = FastLanguageModel.get_peft_model(
        model,
        r=RANK,
        target_modules=[
            "q_proj",
            "k_proj",
            "v_proj",
            "o_proj",
            "gate_proj",
            "up_proj",
            "down_proj",
        ],
        lora_alpha=ALPHA_SCALING_FACTOR,
        lora_dropout=0,  # Dropout = 0 is currently optimized
        bias="none",  # Bias = "none" is currently optimized
        use_gradient_checkpointing="unsloth",
        random_state=3407
    )

    training_args = SFTConfig(
        output_dir=output_dir,
        max_seq_length=MAX_TOKENS,
        fp16=False,
        bf16=True,
        per_device_train_batch_size=2
    )

    dataset = Dataset.from_generator(generate_data, gen_kwargs={"training_data_json": training_data_json})

    trainer = SFTTrainer(
        model=model,
        args=training_args,
        train_dataset=dataset
    )
    trainer.train()

def generate_data(training_data_json):
    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
    with open(training_data_json, "r") as f:
        json_content = json.load(f)

    for input_text, data in json_content.items():
        yield apply_chat_template(format_input_and_expected(input_text, data["expectedValues"]), tokenizer)


def format_input_and_expected(input_text, expected):
    messages =  [
        {"role": "system", "content": PROMPT},
        {
            "role": "user",
            "content": f"Please summarize the following report delimited by triple-quotes: \"\"\"\n{input_text}\n\"\"\""
        },
        {
            "role": "assistant",
            "content": str({k: {"value": v} for k, v in expected.items()})
        }
    ]

    return {"messages": messages}

def main():
    parser = argparse.ArgumentParser()

    parser.add_argument(
        '--hf_token', required=True, help='Hugging Face API token'
    )
    parser.add_argument(
        '--training_data_json', required=True, help='Path to the training data file'
    )
    parser.add_argument(
        '--output_dir', required=True, help='Directory where output will be written'
    )
    args = parser.parse_args()

    fine_tune(args.hf_token, args.training_data_json, args.output_dir)


if __name__ == "__main__":
    main()

