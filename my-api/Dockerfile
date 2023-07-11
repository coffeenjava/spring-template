# builder stage
FROM amazoncorretto:17.0.7 AS builder

# environment
ARG PROFILE=${PROFILE}
ARG APP_NAME=${APP_NAME}
ARG SOURCE_PATH=/src

# create diretories
WORKDIR ${SOURCE_PATH}
COPY . ${SOURCE_PATH}
# gradle build
RUN ./gradlew ${APP_NAME}:clean ${APP_NAME}:build
# copy app.jar & run.sh
RUN mkdir -p /${APP_NAME}/bin && \
    cp ${APP_NAME}/build/libs/*.jar /${APP_NAME}/bin/app.jar && \
    cp ${APP_NAME}/scripts/docker/run.sh /${APP_NAME}/bin/run.sh && \
# pinpoint
#    yum install -y tar gzip && \
#    cp ${APP_NAME}/scripts/pinpoint/pinpoint-agent-${PINPOINT_VERSION}.tar.gz . && \
#    tar -xzvf pinpoint-agent-${PINPOINT_VERSION}.tar.gz && \
#    mv pinpoint-agent-${PINPOINT_VERSION}/* ${PINPOINT_AGENT_PATH} && \
#    rm -rf pinpoint-agent-${PINPOINT_VERSION} && \
#    rm pinpoint-agent-${PINPOINT_VERSION}.tar.gz && \
#    cp ${APP_NAME}/scripts/docker/pinpoint-${PROFILE}.config ${PINPOINT_AGENT_PATH}/pinpoint-root-${PROFILE}.config && \
    chmod +x /${APP_NAME}/bin/run.sh



# runner stage
FROM amazoncorretto:17.0.7-alpine3.17 AS runner

# environment
ARG PROFILE=${PROFILE}
ARG APP_NAME=${APP_NAME}
ARG PINPOINT_AGENT_PATH=/usr/local/pinpoint/agent
#ARG PINPOINT_VERSION=${PINPOINT_VERSION}
#ARG PINPOINT_ENABLE=${PINPOINT_ENABLE}

ENV PROFILE=${PROFILE}
ENV APP_NAME=${APP_NAME}
#ENV PINPOINT_ENABLE=${PINPOINT_ENABLE}
#ENV PINPOINT_VERSION=${PINPOINT_VERSION}

ENV TZ=Asia/Seoul

WORKDIR /${APP_NAME}/bin
RUN apk --no-cache add tzdata && \
    cp /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone
COPY --from=builder /${APP_NAME}/bin /${APP_NAME}/bin
#COPY --from=builder ${PINPOINT_AGENT_PATH} ${PINPOINT_AGENT_PATH}

EXPOSE 8080

ENTRYPOINT ["sh"]
CMD ["run.sh"]
