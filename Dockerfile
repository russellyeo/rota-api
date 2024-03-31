FROM eclipse-temurin:11-alpine

COPY ./dist/app.jar /app/app.jar
WORKDIR /app

EXPOSE 9000

ENTRYPOINT [ "java", "-jar", "app.jar" ]