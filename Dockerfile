# Use OpenJDK 21 as the base image (matches the Java version in pom.xml)
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Create a directory for the H2 database data
RUN mkdir -p /app/data

# Copy the Maven wrapper and pom.xml first to leverage Docker layer caching
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Make the Maven wrapper executable and ensure line endings are correct
RUN chmod +x mvnw && \
    sed -i 's/\r$//' mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port that Spring Boot runs on (default 8080)
EXPOSE 8080

# Create a volume for the H2 database persistence
VOLUME ["/app/data"]

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
CMD ["java", "-jar", "target/stammdatenverwaltung-0.0.1-SNAPSHOT.jar"]