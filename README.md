# LLM Conversational Service Implementation

### Author: Ashish Bhushan
### Email: abhus@uic.edu
### UIN: 654108403

## Introduction

Homework Assignment 3 for CS441 focuses on creating a cloud-deployed LLM-based generative system. This project implements a microservice architecture that enables clients to interact with an LLM through HTTP requests, with responses generated using AWS Bedrock or a custom-trained LLM. The implementation includes both RESTful and gRPC interfaces, AWS Lambda integration, and for graduate students, an automated conversational client using Ollama.

## Video Demonstration - https://youtu.be/7QcjBjiFk0Q

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

**I have also attached the commands I used in the video for running it on all instances as a text file in the repository.**

## Installing Ollama

### Local Installation (Windows)

Using Windows Subsystem for Linux (WSL):

```bash
# Install WSL if not already installed
wsl --install

# Update package list
sudo apt update

# Install Ollama
curl https://ollama.ai/install.sh | sh

# Start Ollama service
ollama serve

# Pull the tinyllama model
ollama pull tinyllama
```

## Environment Setup
### AWS and Environment Credentials

I have removed all my personal credentials from the project for security reasons. Before running the project, create a .env file in the root directory with your own credentials:

```bash
# .env file
AWS_ACCESS_KEY_ID=your_access_key_here
AWS_SECRET_ACCESS_KEY=your_secret_key_here
AWS_REGION=your_region_here
```

Make sure to:

1. Never commit your .env file to version control
2. Keep your AWS credentials secure
3. Use appropriate IAM roles and permissions
4. Configure your AWS credentials either through:
   1..env file (for local development)
   2. AWS EC2 Instance Profile (for EC2 deployment)
   3. Environment variables in your Docker configuration



Required AWS permissions:

Lambda full access
Bedrock full access
API Gateway access
ECR access (for Docker deployment)

## Docker Setup and Deployment

The project uses two containers:
1. Ollama container for local model hosting
2. LLM server container for our service

### 1. Docker Compose Setup

Create a `docker-compose.yml`:

Attached in the repository

### 2. Dockerfile for LLM Server:

Attached in the repository

### 3. Deployment Steps

1. **Start the Containers:**
```bash
# Build and start both containers
docker-compose up --build

# Start in detached mode
docker-compose up -d --build
```

2. **Pull tinyllama Model in Ollama:**
```bash
# Execute into Ollama container
docker exec -it homework3-ollama-1 ollama pull tinyllama
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
// this will remove downloaded ollama models as well.
docker-compose down -v
```

### 5. Network Configuration

The containers communicate through the `llm-network` bridge network:
- Ollama is accessible at `http://ollama:11434` within the network
- LLM server can reach Ollama using this internal DNS name
- Both containers share the same network namespace
- Make sure you update resource.conf (in src/main/resources) to keep host as ollama while running on docker (otherwise it should be localhost) (port will remain same in both the cases)

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

3) **Using curl**

Use curl to send an initial request to bedrock for processing, something like below command can be used.

```bash
curl -X POST http://localhost:8081/chat -H "Content-Type: application/json" -d "{\"prompt\": \"what is the solar system?\"}"
```

If you're running to AWS EC2 and want to run it via local terminal on your machine, you can also you below command

```bash
curl -X POST http://<Public IPv4 address in EC2 Instance>:8081/chat -H "Content-Type: application/json" -d "{\"prompt\": \"how do cats express love?\"}"
```

5) **Docker Deployment**

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

Attached in the repository.

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

## Testing

The project includes unit tests and integration tests. Run tests using:
```bash
sbt test
```

## Conclusion

This implementation demonstrates a scalable, cloud-based LLM service with automated conversation capabilities. The project showcases integration of multiple technologies including Scala, Akka HTTP, gRPC, AWS services, and Docker containerization.
