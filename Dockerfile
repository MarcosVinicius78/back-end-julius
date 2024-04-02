# Use a imagem base do OpenJDK
FROM openjdk:latest

# Copie o JAR da aplicação Spring para o diretório de trabalho
COPY ./target/julius-0.0.1-SNAPSHOT.jar /app/julius-0.0.1-SNAPSHOT.jar

RUN apt-get update && apt-get install -y wget unzip && \
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && apt-get install -y google-chrome-stable && \
    wget -N https://chromedriver.storage.googleapis.com/92.0.4515.107/chromedriver_linux64.zip -P /tmp && \
    unzip /tmp/chromedriver_linux64.zip -d /usr/bin && \
    chmod +x /usr/bin/chromedriver

# Comando para executar a aplicação Spring ao iniciar o contêiner
CMD ["java", "-jar", "/app/julius-0.0.1-SNAPSHOT.jar"]

