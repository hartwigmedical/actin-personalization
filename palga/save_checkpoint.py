from unsloth import FastLanguageModel

model, tokenizer = FastLanguageModel.from_pretrained("/home/jbartlett/out/checkpoint-102/")
model.save_pretrained("lora_model")
tokenizer.save_pretrained("lora_model")
