# --- Estágio 1: build do Quarkus (fast-jar) ---
# Imagem com Maven completo, sem depender do mvnw local.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# pom.xml primeiro para cachear as dependências entre builds.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Estágio 2: runtime só com o JRE ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /build/target/quarkus-app/lib/ ./lib/
COPY --from=build /build/target/quarkus-app/*.jar ./
COPY --from=build /build/target/quarkus-app/app/ ./app/
COPY --from=build /build/target/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8091
# Escuta em todas as interfaces (necessário dentro do container).
ENTRYPOINT ["java", "-Dquarkus.http.host=0.0.0.0", "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", "-jar", "quarkus-run.jar"]
