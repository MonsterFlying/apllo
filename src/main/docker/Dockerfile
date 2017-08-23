FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD app.jar app.jar
# RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
EXPOSE 8911
EXPOSE 5005
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]