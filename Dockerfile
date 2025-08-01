FROM --platform=linux/amd64 gcr.io/distroless/java17-debian11

WORKDIR /app

COPY build/libs/cherrydan-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]