FROM python:3.11

# copy the requirements.txt first
COPY src/main/python/requirements.txt /src/main/python/requirements.txt
RUN pip install --no-cache-dir -r /src/main/python/requirements.txt

ADD src /src

ENTRYPOINT [ "python3", "/src/main/python/run_prediction.py" ]
