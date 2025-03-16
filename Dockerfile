# Usa a imagem base do Amazon Corretto 21
FROM amazoncorretto:21

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o JAR para dentro do contêiner
COPY target/*.jar app.jar

# Expõe a porta usada pelo Spring Boot
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
