# Etapa 1: Build da aplicação usando Maven
FROM maven:latest AS build

# Instalação de dependências necessárias para baixar as fontes
RUN apt-get update && apt-get install -y wget cabextract

# Diretório temporário para baixar e extrair as fontes
WORKDIR /tmp/fonts

# Baixe o pacote de fontes do SourceForge
RUN wget https://downloads.sourceforge.net/corefonts/andale32.exe \
    https://downloads.sourceforge.net/corefonts/arial32.exe \
    https://downloads.sourceforge.net/corefonts/arialb32.exe \
    https://downloads.sourceforge.net/corefonts/comic32.exe \
    https://downloads.sourceforge.net/corefonts/courie32.exe \
    https://downloads.sourceforge.net/corefonts/georgi32.exe \
    https://downloads.sourceforge.net/corefonts/impact32.exe \
    https://downloads.sourceforge.net/corefonts/times32.exe \
    https://downloads.sourceforge.net/corefonts/trebuc32.exe \
    https://downloads.sourceforge.net/corefonts/verdan32.exe \
    https://downloads.sourceforge.net/corefonts/webdin32.exe

# Extraia as fontes usando cabextract
RUN cabextract *.exe && \
    mkdir -p /usr/share/fonts/truetype/msttcorefonts && \
    mv *.ttf /usr/share/fonts/truetype/msttcorefonts && \
    fc-cache -f -v

RUN fc-cache -f -v

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean install -DskipTests

# Etapa 2: Imagem final usando OpenJDK
FROM openjdk:latest

WORKDIR /app

# Copie o JAR da aplicação da etapa anterior
COPY --from=build /app/target/julius-0.0.1-SNAPSHOT.jar /app/julius-0.0.1-SNAPSHOT.jar

# Comando para executar a aplicação Spring ao iniciar o contêiner
ENTRYPOINT ["java", "-jar", "/app/julius-0.0.1-SNAPSHOT.jar"]

# Expor a porta 8080
EXPOSE 8080
