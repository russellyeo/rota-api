FROM openjdk:11-jre-slim

COPY ./dist/app.jar /app/app.jar
WORKDIR /app

EXPOSE 9000

ENTRYPOINT [ "java", "-jar", "app.jar" ]