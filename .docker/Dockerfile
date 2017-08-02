FROM openjdk:8u131-jre-alpine

VOLUME /tmp
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar", "--api.host=bank-api.beedemo.net", "--api.proto=http", "--api.port=8080", "--server.port=8080"]

ADD @project.build.finalName@.jar app.jar
RUN ash -c 'touch /app.jar'

ARG COMMIT_SHA
LABEL beedemo.commit.sha=$COMMIT_SHA
