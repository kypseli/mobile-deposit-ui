FROM java:8
VOLUME /tmp
ADD ${project.build.finalName}.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar", "--api.host=52.40.148.113", "--api.proto=http", "--api.port=8080", "--server.port=8080"]