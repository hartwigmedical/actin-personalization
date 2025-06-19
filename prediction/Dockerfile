FROM python:3.11

ADD src /src
RUN pip install --no-cache-dir -r src/main/python/requirements.txt

ENTRYPOINT [ "python3", "/src/main/python/run_prediction.py" ]
