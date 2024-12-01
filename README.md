# LLM Conversational Service Implementation

### Author: Ashish Bhushan
### Email: abhus@uic.edu
### UIN: 654108403

## Introduction

Homework Assignment 3 for CS441 focuses on creating a cloud-deployed LLM-based generative system. This project implements a microservice architecture that enables clients to interact with an LLM through HTTP requests, with responses generated using AWS Bedrock or a custom-trained LLM. The implementation includes both RESTful and gRPC interfaces, AWS Lambda integration, and for graduate students, an automated conversational client using Ollama.

### Environment
```
OS: Windows 11

IDE: IntelliJ IDEA 2024.2.3 (Ultimate Edition)

SCALA Version: 2.13.12

SBT Version: 1.9.7

Docker Version: 24.0.7

Java Version: 11.0.24
```

## Architecture Overview

The system consists of several key components:

1. **LLM Service**: A microservice that handles client requests through both REST and gRPC interfaces
2. **AWS Lambda Integration**: Processes LLM queries using AWS Bedrock
3. **Docker Containerization**: Deployment using containers for both service and Ollama
4. **Conversational Client**: Automated client using Ollama for follow-up question generation

## Prerequisites

Before running the project, ensure you have:

1. **Docker**: Installed and configured
2. **AWS Account**: With access to Lambda and Bedrock services
3. **Ollama**: Runs in Docker, local and AWS EC2. Model used- tinyllama
4. **curl/Postman**: For testing the REST API
5. **AWS CLI**: Configured with appropriate credentials


## Docker Setup and Deployment

The project uses two containers:
1. Ollama container for local model hosting
2. LLM server container for our service

### 1. Docker Compose Setup

Create a `docker-compose.yml`:
```yaml
version: '3.8'

services:
  ollama:
    image: ollama/ollama
    volumes:
      - ollama:/root/.ollama
    ports:
      - "11434:11434"
    networks:
      - llm-network

  llm-server:
    build: .
    ports:
      - "8081:8081"
      - "9091:9091"
    environment:
      - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
      - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
      - AWS_REGION=us-east-1
      - OLLAMA_HOST=http://ollama:11434
    depends_on:
      - ollama
    networks:
      - llm-network

volumes:
  ollama:

networks:
  llm-network:
    driver: bridge
```

### 2. Dockerfile for LLM Server:
```dockerfile
# Build stage
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.9_9_1.9.7_2.13.12 AS builder
WORKDIR /app
COPY . /app/
RUN sbt clean assembly

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/scala-2.13/homework3-assembly.jar /app/homework3-assembly.jar
COPY --from=builder /app/src/main/resources/application.conf /app/application.conf
RUN mkdir -p /app/conversations

CMD ["java", "-jar", "homework3-assembly.jar"]
```

### 3. Deployment Steps

1. **Start the Containers:**
```bash
# Build and start both containers
docker-compose up --build

# Start in detached mode
docker-compose up -d --build
```

2. **Pull Llama2 Model in Ollama:**
```bash
# Execute into Ollama container
docker exec -it homework3-ollama-1 ollama pull llama2
```

3. **Verify Containers:**
```bash
# Check running containers
docker ps

# Check container logs
docker logs homework3-ollama-1
docker logs homework3-llm-server-1
```

4. **Stop the Containers:**
```bash
docker-compose down
```

### 4. Container Management

Common Docker commands for maintenance:
```bash
# View container logs
docker logs -f homework3-ollama-1    # Follow Ollama logs
docker logs -f homework3-llm-server-1 # Follow server logs

# Restart containers
docker-compose restart

# Stop and remove containers
docker-compose down

# Stop and remove containers + volumes
docker-compose down -v
```

### 5. Network Configuration

The containers communicate through the `llm-network` bridge network:
- Ollama is accessible at `http://ollama:11434` within the network
- LLM server can reach Ollama using this internal DNS name
- Both containers share the same network namespace

### 6. Volume Management

Ollama models are stored in a named volume:
```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect homework3_ollama

# Clean up volume
docker volume rm homework3_ollama
```

[Rest of the README remains the same]

## Troubleshooting Docker Deployment

Common issues and solutions:

1. **Port Conflicts:**
   ```bash
   # Check ports in use
   netstat -ano | findstr "8081"
   netstat -ano | findstr "9091"
   netstat -ano | findstr "11434"
   ```

2. **Container Communication:**
   ```bash
   # Test network connectivity
   docker exec homework3-llm-server-1 ping ollama
   ```

3. **Memory Issues:**
   - Increase Docker desktop resources
   - Monitor container resources:
   ```bash
   docker stats
   ```

4. **Volume Permissions:**
   ```bash
   # Reset volume permissions
   docker-compose down -v
   docker-compose up --build
   ```



## Running the Project

1) **Clone this repository**
```bash
git clone https://github.com/ashishbhushan/ConversationalAgent.git
```

2) **Local Development Setup**
```bash
cd CS441HW3
sbt clean compile
sbt run
```

3)  kk


4) **Docker Deployment**

First, set up Ollama:
```bash
# Pull and run Ollama
docker pull ollama/ollama
docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama
docker exec -it ollama ollama pull tinyllama
```

Then, deploy the service:
```bash
# Build and run the service
docker build -t llm-conversation-service .
docker run -d \
  --name llm-conversation-service \
  -p 8081:8081 \
  -p 9091:9091 \
  -e AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} \
  -e AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} \
  -e AWS_REGION=us-east-1 \
  llm-conversation-service
```

5) **Testing the Service**

Using curl:
```bash
curl -X POST http://localhost:8081/chat \
  -H "Content-Type: application/json" \
  -d '{"prompt": "How do cats express love?"}'
```

## Project Structure

Key components of the implementation:

- `MainApp.scala`: Entry point and HTTP server setup
- `LLMServer.scala`: gRPC server implementation
- `ConversationalClient.scala`: Automated client implementation (grad students)
- `OllamaClient.scala`: Ollama integration for follow-up questions
- `LambdaGrpcClient.scala`: AWS Lambda integration
- `application.conf`: Configuration settings

## Configuration

The application uses Typesafe Config for configuration management. Key configurations in `application.conf`:

```hocon
server {
    host = "localhost"
    port = 8081
}

grpc {
    port = 9091
}

aws {
    region = "us-east-1"
    lambda-function = "LLMQueryFunction"
}

ollama {
    host = "http://localhost:11434"
    model = "tinyllama"
}
```

## AWS Deployment

For AWS deployment:

1. Configure AWS credentials
2. Build Docker image and push to ECR
3. Deploy using provided Docker commands
4. Configure AWS Lambda and API Gateway
5. Update configuration with appropriate endpoints

## Output

The service generates conversation logs in the `conversations` directory with the format:
```
conversation_YYYYMMDD_HHMMSS.txt
```

Each log contains:
- Timestamp
- Initial query
- Conversation turns (Bedrock and Ollama responses)
- Metrics and statistics

## Video Demonstration

[Link to deployment and demo video]

## Testing

The project includes unit tests and integration tests. Run tests using:
```bash
sbt test
```

## Conclusion

This implementation demonstrates a scalable, cloud-based LLM service with automated conversation capabilities. The project showcases integration of multiple technologies including Scala, Akka HTTP, gRPC, AWS services, and Docker containerization.
