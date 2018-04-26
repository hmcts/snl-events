FROM alpine:latest

RUN mkdir /app
COPY dockerize-linux-amd64-v0.2.0.tar.gz /app
WORKDIR /app
RUN tar -C /usr/local/bin -xzvf dockerize-linux-amd64-v0.2.0.tar.gz
