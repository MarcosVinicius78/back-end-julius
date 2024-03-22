# Use a imagem base do OpenJDK
FROM openjdk:latest

# Copie o JAR da aplicação Spring para o diretório de trabalho
COPY ./target/julius-0.0.1-SNAPSHOT.jar /app/julius-0.0.1-SNAPSHOT.jar

# Comando para executar a aplicação Spring ao iniciar o contêiner
CMD ["java", "-jar", "/app/julius-0.0.1-SNAPSHOT.jar"]

