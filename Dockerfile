
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# Use a imagem base do OpenJDK
FROM openjdk:latest

# Copie o JAR da aplicação Spring para o diretório de trabalho
COPY /target/julius-0.0.1-SNAPSHOT.jar /app/julius-0.0.1-SNAPSHOT.jar

# Comando para executar a aplicação Spring ao iniciar o contêiner
CMD ["java", "-jar", "/app/julius-0.0.1-SNAPSHOT.jar"]

