# Etapa 1: Build da aplicação usando Maven
FROM maven:latest AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean install -DskipTests

# Etapa 2: Imagem final usando OpenJDK
FROM openjdk:latest

# Instale as dependências necessárias para adicionar fontes
RUN apt-get update && apt-get install -y \
    fontconfig \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Adicione a fonte desejada
RUN mkdir -p /usr/share/fonts/truetype/custom && \
    wget -O /usr/share/fonts/truetype/custom/CustomFont.ttf https://example.com/path/to/CustomFont.ttf

# Atualize o cache de fontes
RUN fc-cache -f -v

WORKDIR /app

# Copie o JAR da aplicação da etapa anterior
COPY --from=build /app/target/julius-0.0.1-SNAPSHOT.jar /app/julius-0.0.1-SNAPSHOT.jar

# Comando para executar a aplicação Spring ao iniciar o contêiner
ENTRYPOINT ["java", "-jar", "/app/julius-0.0.1-SNAPSHOT.jar"]

# Expor a porta 8080
EXPOSE 8080
