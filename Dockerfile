# --------------------------------------------------------------------------------------------------
# ETAPA 1: BUILDER - Compila a aplicação usando a imagem completa do Java Development Kit (JDK) e Maven.
# O resultado dessa etapa é o arquivo JAR compilado.
# --------------------------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia os arquivos de configuração do Maven para otimizar o cache da layer
COPY pom.xml .

# Copia todo o código-fonte da aplicação
COPY src src

# Empacota a aplicação, gerando o arquivo JAR
# O parâmetro -DskipTests pula os testes durante o build para agilizar o processo
RUN mvn clean package -DskipTests

# --------------------------------------------------------------------------------------------------
# ETAPA 2: RUNNER - Cria a imagem final, minimalista e segura, apenas com o JRE.
# Isso reduz o tamanho da imagem e a superfície de ataque.
# ----------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

# Define o argumento 'JAR_FILE' que será usado para saber o nome do JAR gerado.
# O Spring Boot Maven Plugin tipicamente nomeia o JAR como "app.jar".
ARG JAR_FILE=target/*.jar

# Copia o JAR do estágio 'builder' para a imagem final
COPY --from=builder /app/${JAR_FILE} app.jar

# Define o ponto de entrada. O comando 'java -jar app.jar' inicia sua aplicação.
# O comando `-Djava.security.egd=file:/dev/./urandom` é uma boa prática para melhorar a performance.
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

# Expõe a porta que o Spring Boot usa (8080)
EXPOSE 8080