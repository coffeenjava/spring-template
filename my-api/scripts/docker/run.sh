#!/bin/bash

# app setting
JVM_OPTIONS="-XX:+UseG1GC"
if [ "prod" = "${PROFILE}" ]
then
  JVM_OPTIONS="${JVM_OPTIONS} -Xmx2048m"
else
  JVM_OPTIONS="${JVM_OPTIONS}"
fi

#if [ "${PINPOINT_ENABLE}" = true ]
#then
#  # pinpoint 용
#  POSTFIX=`echo ${HOST_IP:-0.0.0.0} | awk -F'.' '{print $3"." $4}'`
#
#  java -Djava.security.egd=file:/dev/./urandom \
#       -Dspring.profiles.active=${PROFILE} \
#       ${JVM_OPTIONS} \
#       -javaagent:/usr/local/pinpoint/agent/pinpoint-bootstrap-${PINPOINT_VERSION}.jar \
#       -Dpinpoint.agentId=${APP_NAME}-${POSTFIX} \
#       -Dpinpoint.applicationName=${APP_NAME} \
#       -Dpinpoint.config=/usr/local/pinpoint/agent/pinpoint-root-${PROFILE}.config \
#       -jar /${APP_NAME}/bin/app.jar ${JOB_NAME} version=${JOB_VERSION}
#else
java -Djava.security.egd=file:/dev/./urandom \
     -Dspring.profiles.active=${PROFILE} \
     ${JVM_OPTIONS} \
     -jar /${APP_NAME}/bin/app.jar
#fi

