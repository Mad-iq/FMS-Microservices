FROM eclipse-temurin:17-jdk

# IMPORTANT: Install ca-certificates so Java can trust GitHub SSL
RUN apt-get update && \
    apt-get install -y git curl ca-certificates && \
    update-ca-certificates && \
    apt-get clean

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
