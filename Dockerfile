# Use OpenJDK 17 as the base image
FROM openjdk:17-slim

# Set working directory
WORKDIR /app

# Copy the Maven project file
COPY pom.xml .
COPY src ./src

# Install Maven and build the application
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests && \
    cp target/*.jar app.jar && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Expose the port the app runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]