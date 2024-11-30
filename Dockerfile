# Build stage
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.9_9_1.9.7_2.13.12 AS builder

WORKDIR /app
COPY . /app/

# Build the application
RUN sbt clean assembly

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the assembled jar and config using your project's paths
COPY --from=builder /app/target/scala-2.13/homework3-assembly.jar /app/homework3-assembly.jar
COPY --from=builder /app/src/main/resources/application.conf /app/application.conf

# Create conversations directory
RUN mkdir -p /app/conversations

# Expose ports for HTTP and gRPC
ENV SERVER_HOST=0.0.0.0
EXPOSE 8081 9091

# Set the config file location
ENV CONFIG_FILE=/app/application.conf

# Command to run the application
CMD ["java", "-jar", "homework3-assembly.jar"]