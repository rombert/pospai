FROM docker.io/apache/sling:snapshot AS starter
FROM docker.io/eclipse-temurin:21@sha256:96e31e0d459d05f4c9facaffb3b25a0b2c460e0c68614113c27d28dcc77d6ca6

EXPOSE 8080

RUN groupadd --system sling && \
    useradd --no-log-init --system --gid sling sling && \
    mkdir /opt/sling && \
    mkdir /opt/sling/bin && \
    mkdir /opt/sling/org.apache.sling.feature.launcher && \
    mkdir /opt/sling/launcher && \
    mkdir /opt/sling/artifacts && \
    mkdir /opt/sling/agents && \
    chown -R sling:sling /opt/sling/launcher

VOLUME /opt/sling/launcher

COPY --from=starter /opt/sling/bin /opt/sling/bin
COPY target/dependency/org.apache.sling.feature.launcher /opt/sling/org.apache.sling.feature.launcher
COPY target/artifacts/ /opt/sling/artifacts/
COPY target/agents/ /opt/sling/agents/

# ensure all files are readable by the sling user
# for some reason some jar files are 0600 while most are 0644
RUN find /opt/sling/artifacts -type f -perm 0600 | xargs --no-run-if-empty chmod 0644

USER sling:sling

ENV OTEL_SERVICE_NAME=pospai

WORKDIR /opt/sling
ENTRYPOINT [ "/opt/sling/bin/launch.sh" ]
CMD ["app"]
