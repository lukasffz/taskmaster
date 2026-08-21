# ─────────────────────────────────────────────────────────────
# Stage 1 — Build
#   Eclipse Temurin 21 JDK (Alpine) + Maven Wrapper
#   Compila o JAR sem executar os testes (já validados localmente)
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Copia os arquivos de dependência primeiro para aproveitar o cache de camadas
COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml pom.xml

# Garante permissão de execução no Maven Wrapper (necessário no Linux)
RUN chmod +x mvnw

# Baixa todas as dependências sem compilar o código fonte
# A camada de cache é reutilizada enquanto o pom.xml não mudar
RUN ./mvnw dependency:go-offline -B --no-transfer-progress

# Copia o código fonte
COPY src/ src/

# Compila e empacota, pulando testes (validados localmente antes do deploy)
RUN ./mvnw clean package -DskipTests -B --no-transfer-progress

# ─────────────────────────────────────────────────────────────
# Stage 2 — Runtime
#   Eclipse Temurin 21 JRE slim (Alpine)
#   Imagem final enxuta: sem Maven, sem código fonte, sem JDK completo
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Metadados da imagem
LABEL org.opencontainers.image.title="TaskMaster API"
LABEL org.opencontainers.image.description="TaskMaster backend — Spring Boot 3.3 / Java 21"
LABEL org.opencontainers.image.source="https://github.com/lukasffz/taskmaster"

WORKDIR /app

# Usuário não-root para princípio do menor privilégio
RUN addgroup -S taskmaster && adduser -S taskmaster -G taskmaster
USER taskmaster

# Copia apenas o JAR gerado no stage anterior
COPY --from=build --chown=taskmaster:taskmaster \
     /workspace/target/backend-taskmaster-1.0.0.jar app.jar

# Porta exposta (mapeada via variável SERVER_PORT no Render)
EXPOSE 10000

# JVM tunada para container:
#   -XX:+UseContainerSupport          — respeita os limites de CPU/memória do container
#   -XX:MaxRAMPercentage=75.0         — usa até 75% da RAM disponível para o heap
#   -Djava.security.egd=...           — acelera a geração de números aleatórios (SecureRandom)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
