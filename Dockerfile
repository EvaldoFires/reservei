# Usa a imagem do Maven para construir o projeto
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia os arquivos do projeto para o contêiner
COPY . .

# Compila o projeto e gera o JAR
RUN mvn clean package -DskipTests

# Usa uma imagem mais leve para rodar a aplicação
FROM amazoncorretto:21

# Define o diretório de trabalho para a aplicação final
WORKDIR /app

# Copia o JAR gerado na etapa anterior para o contêiner final
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta do Spring Boot
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

