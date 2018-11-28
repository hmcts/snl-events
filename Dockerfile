FROM hmcts/cnp-java-base:openjdk-jre-8-alpine-1.2

# Mandatory!
ENV APP snl-events.jar
ENV APPLICATION_TOTAL_MEMORY 512M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 100

# Optional
ENV JAVA_OPTS ""

COPY build/libs/$APP /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8092/health

EXPOSE 8092

