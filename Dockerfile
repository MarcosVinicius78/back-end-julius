# Etapa 1: Build da aplicação usando Maven
FROM maven:latest AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean install -DskipTests

# Etapa 2: Imagem final usando OpenJDK
FROM openjdk:latest

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    fontconfig \
    fonts-dejavu-core \
    fonts-dejavu-extra && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copie o JAR da aplicação da etapa anterior
COPY --from=build /app/target/julius-0.0.1-SNAPSHOT.jar /app/julius-0.0.1-SNAPSHOT.jar

# Comando para executar a aplicação Spring ao iniciar o contêiner
ENTRYPOINT ["java", "-jar", "/app/julius-0.0.1-SNAPSHOT.jar"]

# Expor a porta 8080
EXPOSE 8080
