# Multi-stage build for optimized image size
# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set the working directory for build
WORKDIR /app

# Copy the Maven wrapper and pom.xml first to leverage Docker layer caching
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Make the Maven wrapper executable and ensure line endings are correct
RUN chmod +x mvnw && \
    sed -i 's/\r$//' mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy the source code and configuration files needed for build
COPY src ./src
COPY checkstyle-logic-only.xml ./

# Build the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Set the working directory inside the container
WORKDIR /app

# Create a directory for the H2 database data
RUN mkdir -p /app/data && chown spring:spring /app/data

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/stammdatenverwaltung-0.0.1-SNAPSHOT.jar app.jar

# Change ownership of the app directory to the spring user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose the port that Spring Boot runs on (default 8080)
EXPOSE 8080

# Create a volume for the H2 database persistence
VOLUME ["/app/data"]

# Set environment variables with defaults
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
CMD ["java", "-jar", "app.jar"]