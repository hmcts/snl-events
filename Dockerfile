FROM openjdk:8-jre

COPY build/install/snl-events /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8092/health

EXPOSE 8092

ENTRYPOINT ["/opt/app/bin/snl-events"]
