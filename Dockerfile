ARG DOCKER_BASE_IMG
ARG DOCKER_CBOS_VERSION
FROM $DOCKER_BASE_IMG:$DOCKER_CBOS_VERSION

LABEL author="Challengers"

HEALTHCHECK NONE

RUN mkdir -p /hcagent/bin/ && \
    mkdir -p /hcagent/libs/ && \
    mkdir -p /hcagent/classes/

COPY maven-common-resources/entrypoint.sh /hcagent/entrypoint.sh
COPY target/libs /hcagent/libs
COPY target/classes /hcagent/classes

RUN echo "120260:x:120260:120260:An Identify for non-rootroot-cont:/nonexistent:/bin/false" >>/etc/passwd && \
    echo "120260:!::0:::::" >>/etc/shadow && \
    chown -R 120260:0 /hcagent && chmod -R g=u /hcagent
RUN mkdir /vertx && chown -R 120260:0 /vertx && chmod -R g=u /vertx
USER 120260

WORKDIR /hcagent

ENTRYPOINT ["/bin/bash", "-c", "/hcagent/entrypoint.sh java -cp /hcagent/libs/*:/hcagent/classes ${JVM_PARAMS} -Dlogback.configurationFile=/hcagent/classes/${LOGBACK_FILENAME} com.ericsson.sc.hcagent.HealthCheckAgent"]
