FROM openjdk:jdk-alpine
RUN mkdir /app
WORKDIR /app
COPY ./build/libs/chat-0.0.1-SNAPSHOT.jar /app/
ENTRYPOINT [ "java", "-jar", "/app/chat-0.0.1-SNAPSHOT.jar"]
