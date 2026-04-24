# Deployment Guide - App Inventory Management Backend

This guide covers the deployment of the Spring Boot backend application to AWS infrastructure (EC2 or ECS).

## Prerequisites

- AWS CLI configured with appropriate credentials
- Docker installed (for building container images)
- Maven 3.9+ (for building the application)
- Java 17+ (for local builds)
- Access to AWS Secrets Manager or Systems Manager Parameter Store
- RDS MySQL 8.0+ instance configured

## Table of Contents

1. [Database Configuration](#database-configuration)
2. [Building the Application](#building-the-application)
3. [Docker Deployment](#docker-deployment)
4. [ECS Deployment](#ecs-deployment)
5. [EC2 Deployment](#ec2-deployment)
6. [ALB Configuration](#alb-configuration)
7. [Health Checks](#health-checks)
8. [Troubleshooting](#troubleshooting)

---

## Database Configuration

### Store Database Credentials in AWS Secrets Manager

```bash
# Set your AWS region
export AWS_REGION=us-east-1

# Create secrets for database configuration
aws secretsmanager create-secret \
  --name app-inventory/db-host \
  --secret-string "your-rds-endpoint.rds.amazonaws.com" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name app-inventory/db-port \
  --secret-string "3306" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name app-inventory/db-name \
  --secret-string "matriz_usuarios" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name app-inventory/db-user \
  --secret-string "admin" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name app-inventory/db-password \
  --secret-string "your-secure-password" \
  --region ${AWS_REGION}
```

### Alternative: AWS Systems Manager Parameter Store

```bash
# Create parameters in Parameter Store (SecureString type)
aws ssm put-parameter \
  --name /app-inventory/db-host \
  --value "your-rds-endpoint.rds.amazonaws.com" \
  --type SecureString \
  --region ${AWS_REGION}

aws ssm put-parameter \
  --name /app-inventory/db-port \
  --value "3306" \
  --type String \
  --region ${AWS_REGION}

aws ssm put-parameter \
  --name /app-inventory/db-name \
  --value "matriz_usuarios" \
  --type String \
  --region ${AWS_REGION}

aws ssm put-parameter \
  --name /app-inventory/db-user \
  --value "admin" \
  --type SecureString \
  --region ${AWS_REGION}

aws ssm put-parameter \
  --name /app-inventory/db-password \
  --value "your-secure-password" \
  --type SecureString \
  --region ${AWS_REGION}
```

---

## Building the Application

### Local Build

```bash
# Build the JAR file
mvn clean package -DskipTests

# The JAR will be located at: target/app-inventory-management-1.0.0-SNAPSHOT.jar
```

### Docker Build

```bash
# Build the Docker image
docker build -t app-inventory-management:latest .

# Tag for ECR
docker tag app-inventory-management:latest \
  <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/app-inventory-management:latest

# Login to ECR
aws ecr get-login-password --region <REGION> | \
  docker login --username AWS --password-stdin \
  <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com

# Push to ECR
docker push <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/app-inventory-management:latest
```

---

## Docker Deployment

### Run Locally with Docker

```bash
# Run the container with environment variables
docker run -d \
  --name app-inventory-backend \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/matriz_usuarios?useSSL=false&serverTimezone=UTC" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="root" \
  -e SPRING_PROFILES_ACTIVE="production" \
  app-inventory-management:latest

# Check logs
docker logs -f app-inventory-backend

# Check health
curl http://localhost:8080/actuator/health
```

---

## ECS Deployment

### 1. Create ECR Repository

```bash
aws ecr create-repository \
  --repository-name app-inventory-management \
  --region <REGION>
```

### 2. Create ECS Cluster

```bash
aws ecs create-cluster \
  --cluster-name app-inventory-cluster \
  --region <REGION>
```

### 3. Create CloudWatch Log Group

```bash
aws logs create-log-group \
  --log-group-name /ecs/app-inventory-management \
  --region <REGION>
```

### 4. Register Task Definition

Update `ecs-task-definition.json` with your account ID and region, then:

```bash
aws ecs register-task-definition \
  --cli-input-json file://ecs-task-definition.json \
  --region <REGION>
```

### 5. Create ECS Service

```bash
aws ecs create-service \
  --cluster app-inventory-cluster \
  --service-name app-inventory-backend-service \
  --task-definition app-inventory-management \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxx],assignPublicIp=DISABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:<REGION>:<ACCOUNT_ID>:targetgroup/app-inventory-backend-tg/xxx,containerName=app-inventory-backend,containerPort=8080" \
  --region <REGION>
```

### 6. IAM Roles Required

**ECS Task Execution Role** (`ecsTaskExecutionRole`):
- `AmazonECSTaskExecutionRolePolicy` (managed policy)
- Custom policy for Secrets Manager access:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:<REGION>:<ACCOUNT_ID>:secret:app-inventory/*"
      ]
    }
  ]
}
```

**ECS Task Role** (`ecsTaskRole`):
- Custom policies for application-specific AWS service access (if needed)

---

## EC2 Deployment

### 1. Prepare EC2 Instance

```bash
# SSH into your EC2 instance
ssh -i your-key.pem ec2-user@<EC2_IP>

# Install Java 21
sudo yum install -y java-21-amazon-corretto

# Install AWS CLI (if not already installed)
sudo yum install -y aws-cli

# Create application user
sudo useradd -r -s /bin/false appuser

# Create application directory
sudo mkdir -p /opt/app-inventory-management
sudo chown appuser:appuser /opt/app-inventory-management
```

### 2. Deploy Application

```bash
# Copy JAR file to EC2
scp -i your-key.pem target/app-inventory-management-1.0.0-SNAPSHOT.jar \
  ec2-user@<EC2_IP>:/tmp/

# On EC2 instance, move JAR to application directory
sudo mv /tmp/app-inventory-management-1.0.0-SNAPSHOT.jar \
  /opt/app-inventory-management/app.jar
sudo chown appuser:appuser /opt/app-inventory-management/app.jar
```

### 3. Install Systemd Service

```bash
# Copy service file
sudo cp app-inventory-management.service /etc/systemd/system/

# Reload systemd
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable app-inventory-management
```

### 4. Deploy with Script

```bash
# Make deployment script executable
chmod +x deploy-ec2.sh

# Run deployment
./deploy-ec2.sh
```

### 5. Verify Deployment

```bash
# Check service status
sudo systemctl status app-inventory-management

# View logs
sudo journalctl -u app-inventory-management -f

# Test health endpoint
curl http://localhost:8080/actuator/health
```

---

## ALB Configuration

### 1. Create Target Group

Update `alb-target-group-config.json` with your VPC ID, then:

```bash
aws elbv2 create-target-group \
  --cli-input-json file://alb-target-group-config.json \
  --region <REGION>
```

### 2. Register Targets (for EC2)

```bash
aws elbv2 register-targets \
  --target-group-arn <TARGET_GROUP_ARN> \
  --targets Id=<EC2_INSTANCE_ID> \
  --region <REGION>
```

### 3. Create ALB Listener Rule

```bash
# Add a listener rule to forward traffic to the target group
aws elbv2 create-rule \
  --listener-arn <LISTENER_ARN> \
  --priority 10 \
  --conditions Field=path-pattern,Values='/api/*' \
  --actions Type=forward,TargetGroupArn=<TARGET_GROUP_ARN> \
  --region <REGION>
```

### Health Check Configuration

The ALB is configured to check:
- **Path**: `/actuator/health`
- **Port**: `8080`
- **Protocol**: `HTTP`
- **Interval**: 30 seconds
- **Timeout**: 5 seconds
- **Healthy threshold**: 2 consecutive successes
- **Unhealthy threshold**: 3 consecutive failures
- **Success codes**: `200`

---

## Health Checks

### Actuator Endpoints

The application exposes the following health check endpoints:

- **Health**: `GET /actuator/health`
  - Returns overall application health status
  - Includes database connectivity check
  
- **Liveness**: `GET /actuator/health/liveness`
  - Kubernetes-style liveness probe
  
- **Readiness**: `GET /actuator/health/readiness`
  - Kubernetes-style readiness probe

### Example Health Response

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

### Testing Health Checks

```bash
# Basic health check
curl http://localhost:8080/actuator/health

# Detailed health check (requires authorization)
curl http://localhost:8080/actuator/health?showDetails=true

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```

---

## Troubleshooting

### Application Won't Start

1. **Check logs**:
   ```bash
   # ECS
   aws logs tail /ecs/app-inventory-management --follow
   
   # EC2
   sudo journalctl -u app-inventory-management -n 100
   ```

2. **Verify database connectivity**:
   ```bash
   # Test MySQL connection from container/instance
   mysql -h <DB_HOST> -P <DB_PORT> -u <DB_USER> -p<DB_PASSWORD> <DB_NAME>
   ```

3. **Check environment variables**:
   ```bash
   # EC2
   sudo systemctl show app-inventory-management --property=Environment
   
   # ECS - check task definition
   aws ecs describe-task-definition --task-definition app-inventory-management
   ```

### Health Check Failing

1. **Verify actuator is enabled**:
   ```bash
   curl http://localhost:8080/actuator
   ```

2. **Check database connection**:
   - Ensure RDS security group allows inbound traffic from ECS/EC2 security group
   - Verify database credentials in Secrets Manager

3. **Review application logs** for database connection errors

### High Memory Usage

1. **Adjust JVM heap settings** in `app-inventory-management.service`:
   ```
   Environment="JAVA_OPTS=-Xmx512m -Xms256m"
   ```

2. **Update ECS task definition** CPU and memory limits

### Secrets Manager Access Denied

1. **Verify IAM role** has `secretsmanager:GetSecretValue` permission
2. **Check secret ARN** matches the pattern in IAM policy
3. **Ensure task execution role** is correctly attached to ECS task

---

## Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | JDBC connection URL | `jdbc:mysql://db.example.com:3306/matriz_usuarios` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `SecurePassword123!` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `production` |
| `SERVER_PORT` | Application port | `8080` |
| `JAVA_OPTS` | JVM options | `-Xmx768m -Xms512m` |

---

## Security Considerations

1. **Never commit secrets** to version control
2. **Use AWS Secrets Manager** or Parameter Store for sensitive data
3. **Enable SSL/TLS** for database connections in production
4. **Restrict security groups** to allow only necessary traffic
5. **Use IAM roles** instead of access keys for AWS service access
6. **Enable CloudWatch logging** for audit trails
7. **Regularly rotate** database credentials

---

## Monitoring

### CloudWatch Metrics

- ECS service CPU and memory utilization
- ALB target health status
- Request count and latency
- 4xx and 5xx error rates

### CloudWatch Alarms

Create alarms for:
- Unhealthy target count > 0
- CPU utilization > 80%
- Memory utilization > 80%
- 5xx error rate > 1%

### Application Metrics

Access via `/actuator/metrics` endpoint (requires configuration to expose)

---

## Rollback Procedure

### ECS

```bash
# List task definition revisions
aws ecs list-task-definitions --family-prefix app-inventory-management

# Update service to previous revision
aws ecs update-service \
  --cluster app-inventory-cluster \
  --service app-inventory-backend-service \
  --task-definition app-inventory-management:PREVIOUS_REVISION
```

### EC2

```bash
# Stop service
sudo systemctl stop app-inventory-management

# Replace JAR with previous version
sudo cp /opt/app-inventory-management/app.jar.backup \
  /opt/app-inventory-management/app.jar

# Start service
sudo systemctl start app-inventory-management
```

---

## Additional Resources

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS Secrets Manager Documentation](https://docs.aws.amazon.com/secretsmanager/)
- [Application Load Balancer Documentation](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/)
