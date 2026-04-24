# Task 14.2 - Backend Deployment Configuration Summary

## Overview

This document summarizes the deployment configuration created for the App Inventory Management backend application on AWS infrastructure (EC2/ECS).

## Files Created

### 1. **Dockerfile**
Multi-stage Docker build configuration:
- **Build stage**: Uses `maven:3.9-eclipse-temurin-17` to compile the application
- **Production stage**: Uses `eclipse-temurin:21-jre-alpine` as the base image
- **JAR**: Copies the compiled JAR from the build stage
- **Port**: Exposes port 8080
- **Health check**: Configured to check `/actuator/health` endpoint every 30 seconds
- **Entry point**: `java -jar app.jar`

### 2. **ecs-task-definition.json**
ECS Fargate task definition with:
- **Container**: `app-inventory-backend` running on port 8080
- **Resources**: 512 CPU units, 1024 MB memory
- **Environment variables**: 
  - `SPRING_PROFILES_ACTIVE=production`
  - `SERVER_PORT=8080`
- **Secrets from AWS Secrets Manager**:
  - `DB_HOST` - Database endpoint
  - `DB_PORT` - Database port (3306)
  - `DB_NAME` - Database name (matriz_usuarios)
  - `DB_USER` - Database username
  - `DB_PASSWORD` - Database password
- **Health check**: Container-level health check using `/actuator/health`
- **Logging**: CloudWatch Logs integration (`/ecs/app-inventory-management`)

### 3. **deploy-ec2.sh**
Deployment script for EC2 instances:
- Fetches database credentials from AWS Secrets Manager
- Constructs JDBC connection URL with environment variables
- Manages systemd service lifecycle (stop/start)
- Includes health check verification
- Alternative commands for AWS Systems Manager Parameter Store

### 4. **app-inventory-management.service**
Systemd service unit file for EC2:
- Runs as dedicated `appuser` user
- JVM options: `-Xmx768m -Xms512m -XX:+UseG1GC`
- Automatic restart on failure
- Journal logging integration
- Security hardening: `NoNewPrivileges`, `PrivateTmp`

### 5. **alb-target-group-config.json**
Application Load Balancer target group configuration:
- **Protocol**: HTTP
- **Port**: 8080
- **Health check path**: `/actuator/health`
- **Health check interval**: 30 seconds
- **Health check timeout**: 5 seconds
- **Healthy threshold**: 2 consecutive successes
- **Unhealthy threshold**: 3 consecutive failures
- **Success matcher**: HTTP 200
- **Target type**: IP (for ECS Fargate)

### 6. **DEPLOYMENT.md**
Comprehensive deployment guide covering:
- Database configuration with Secrets Manager/Parameter Store
- Building the application (Maven + Docker)
- Docker local deployment
- ECS deployment (cluster, service, task definition)
- EC2 deployment (systemd service)
- ALB configuration and health checks
- Troubleshooting procedures
- Security considerations
- Monitoring and rollback procedures

### 7. **aws-setup-commands.sh**
Complete AWS infrastructure setup script:
- Creates Secrets Manager secrets for database credentials
- Creates ECR repository for Docker images
- Creates CloudWatch log group
- Creates IAM roles (task execution and task roles)
- Creates security groups
- Creates ALB target group with health checks
- Creates ECS cluster
- Builds and pushes Docker image
- Registers ECS task definition
- Creates ECS service with load balancer integration
- Creates CloudWatch alarms for monitoring

### 8. **.dockerignore**
Docker build optimization file to exclude:
- Maven build artifacts (except JAR)
- IDE files
- Git files
- Documentation
- Test files
- Kiro configuration

### 9. **src/main/resources/application-production.yml**
Production-specific Spring Boot configuration:
- Database connection pool settings (HikariCP)
- JPA optimizations (batch inserts, query optimization)
- Actuator endpoints configuration
- Health check probes (liveness, readiness)
- Prometheus metrics export
- Server compression and thread pool tuning
- Production logging configuration

### 10. **Updated pom.xml**
Added Spring Boot Actuator dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 11. **Updated src/main/resources/application.yml**
- Added environment variable support for database configuration:
  - `SPRING_DATASOURCE_URL` (default: localhost)
  - `SPRING_DATASOURCE_USERNAME` (default: root)
  - `SPRING_DATASOURCE_PASSWORD` (default: root)
- Added Actuator configuration:
  - Health endpoint: `/actuator/health`
  - Liveness probe: `/actuator/health/liveness`
  - Readiness probe: `/actuator/health/readiness`
  - Exposed endpoints: health, info, metrics

## Key Features

### 1. **Environment Variable Configuration**
The application supports configuration via environment variables, allowing the same Docker image to be deployed across different environments (dev, staging, production) without rebuilding.

### 2. **AWS Secrets Manager Integration**
Database credentials are securely stored in AWS Secrets Manager and injected at runtime:
- ECS: Via task definition `secrets` section
- EC2: Via deployment script fetching secrets

### 3. **Health Checks at Multiple Levels**
- **Docker**: Container health check using wget
- **ECS**: Task-level health check
- **ALB**: Target group health check
- **Spring Boot**: Actuator health endpoint with database connectivity check

### 4. **Production-Ready Configuration**
- Connection pooling with HikariCP
- JVM tuning for optimal performance
- Compression enabled for API responses
- Structured logging with rotation
- Metrics export for monitoring

### 5. **Security Best Practices**
- No hardcoded credentials
- Secrets stored in AWS Secrets Manager
- IAM roles for service authentication
- Security group restrictions
- Systemd security hardening
- SSL/TLS support for database connections

## Health Check Configuration

### ALB Health Check
- **Endpoint**: `GET /actuator/health`
- **Expected response**: HTTP 200 with JSON body
- **Interval**: 30 seconds
- **Timeout**: 5 seconds
- **Healthy threshold**: 2 consecutive successes
- **Unhealthy threshold**: 3 consecutive failures

### Health Response Example
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

## Deployment Options

### Option 1: ECS Fargate (Recommended)
- **Pros**: Fully managed, auto-scaling, no server management
- **Cons**: Slightly higher cost than EC2
- **Use case**: Production workloads requiring high availability

### Option 2: EC2
- **Pros**: More control, potentially lower cost
- **Cons**: Requires server management, manual scaling
- **Use case**: Development/staging environments, cost-sensitive deployments

## Environment Variables Reference

| Variable | Description | Source |
|----------|-------------|--------|
| `DB_HOST` | RDS endpoint | Secrets Manager |
| `DB_PORT` | Database port | Secrets Manager |
| `DB_NAME` | Database name | Secrets Manager |
| `DB_USER` | Database username | Secrets Manager |
| `DB_PASSWORD` | Database password | Secrets Manager |
| `SPRING_PROFILES_ACTIVE` | Active profile | Environment |
| `SERVER_PORT` | Application port | Environment |

## Quick Start

### For ECS Deployment:
```bash
# 1. Configure variables in aws-setup-commands.sh
# 2. Run the setup script
chmod +x aws-setup-commands.sh
./aws-setup-commands.sh

# 3. Monitor deployment
aws ecs describe-services \
  --cluster app-inventory-cluster \
  --services app-inventory-backend-service
```

### For EC2 Deployment:
```bash
# 1. Set up EC2 instance with Java 21
# 2. Install systemd service
sudo cp app-inventory-management.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable app-inventory-management

# 3. Run deployment script
chmod +x deploy-ec2.sh
./deploy-ec2.sh
```

## Validation

After deployment, verify:

1. **Health endpoint responds**:
   ```bash
   curl http://<ALB_DNS>/actuator/health
   ```

2. **Database connectivity**:
   Check health response includes `"db": {"status": "UP"}`

3. **ALB target health**:
   ```bash
   aws elbv2 describe-target-health \
     --target-group-arn <TARGET_GROUP_ARN>
   ```

4. **Application logs**:
   ```bash
   # ECS
   aws logs tail /ecs/app-inventory-management --follow
   
   # EC2
   sudo journalctl -u app-inventory-management -f
   ```

## Requirements Validation

This implementation satisfies the task requirements:

✅ **Dockerfile created** with:
- Base image: `eclipse-temurin:21-jre-alpine`
- JAR copied from build stage
- Port 8080 exposed
- ENTRYPOINT defined

✅ **ECS task definition created** with:
- Environment variables for database configuration
- Secrets injected from AWS Secrets Manager
- Container health check configured

✅ **Deployment script created** for EC2 with:
- Secrets Manager integration
- Environment variable injection
- Service management

✅ **ALB health check configured**:
- Endpoint: `/actuator/health`
- Spring Boot Actuator enabled
- Health check parameters optimized

## Related Requirements

This task implements:
- **Requirement 1.2**: Backend SHALL implement a API REST with Spring Boot 3 or superior
- **Requirement 1.4**: System SHALL deploy on AWS infrastructure (EC2, RDS or managed equivalents)

## Next Steps

1. Configure ALB listener rules to route traffic to the target group
2. Set up CloudWatch dashboards for monitoring
3. Configure auto-scaling policies for ECS service
4. Set up CI/CD pipeline for automated deployments
5. Configure SSL/TLS certificates for HTTPS
6. Set up database backups and disaster recovery
