# ====== Build ======
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# ====== Runtime (ligero) ======
FROM eclipse-temurin:21-jre
WORKDIR /app
# zona horaria opcional
ENV TZ=America/Santiago
COPY --from=build /app/target/ms-consumidor-kafka-0.0.1-SNAPSHOT.jar app.jar
# variables con defaults seguros
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
    KAFKA_GROUP_ID=ms-consumidor-grp \
    KAFKA_TOPIC=mi.topico \
    KAFKA_DLQ_TOPIC=mi.topico.DLQ
ENTRYPOINT ["java","-jar","/app/app.jar"]