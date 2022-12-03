FROM python:latest as builder
WORKDIR /usr/src/app
COPY script.py ./script.py
COPY .env ./.env
COPY pylint.cfg ./pylint.cfg
WORKDIR /wheels
COPY requirements.txt ./requirements.txt
RUN pip wheel -r ./requirements.txt

FROM eeacms/pylint:latest as linting
WORKDIR /code
COPY --from=builder /usr/src/app/pylint.cfg /etc/pylint.cfg
COPY --from=builder /usr/src/app/script.py ./script.py
RUN ["/docker-entrypoint.sh", "pylint"]

FROM python:3.7-slim as serve
ENV PYTHONUNBUFFERED=1
RUN mkdir root/.aws
COPY config root/.aws/
COPY credentials  root/.aws/
WORKDIR /usr/src/app
COPY --from=builder /wheels /wheels
RUN     pip install -r /wheels/requirements.txt \
                      -f /wheels \
       && rm -rf /wheels \
       && rm -rf /root/.cache/pip/*
COPY --from=builder /usr/src/app/script.py ./script.py
COPY --from=builder /usr/src/app/.env ./.env
CMD ["python3", "script.py"]
