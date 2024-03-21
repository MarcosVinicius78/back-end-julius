# Use a imagem base do OpenJDK
FROM openjdk:latest

# Copie o JAR da aplicação Spring para o diretório de trabalho
#COPY ./target/my-spring-app.jar /app/my-spring-app.jar

# Comando para executar a aplicação Spring ao iniciar o contêiner
CMD ["java", "-jar", "./target/julius-0.0.1-SNAPSHOT.jar"]
