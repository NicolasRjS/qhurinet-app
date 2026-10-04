FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiar archivos de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Dar permisos y compilar (sin pruebas: el build no tiene base de datos)
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Imagen final: solo el JRE y el JAR
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar backend.jar

EXPOSE 8088
ENTRYPOINT ["java", "-Xmx512M", "-jar", "backend.jar"]
