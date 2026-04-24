#!/bin/bash

# Deployment script for EC2 instance
# This script pulls environment variables from AWS Secrets Manager or Parameter Store
# and runs the Spring Boot application

set -e

echo "Starting deployment of app-inventory-management backend..."

# Configuration
APP_NAME="app-inventory-management"
JAR_FILE="/opt/${APP_NAME}/app.jar"
SERVICE_USER="appuser"
AWS_REGION="${AWS_REGION:-us-east-1}"

# Fetch database configuration from AWS Secrets Manager
echo "Fetching database configuration from AWS Secrets Manager..."
export DB_HOST=$(aws secretsmanager get-secret-value \
  --secret-id app-inventory/db-host \
  --region ${AWS_REGION} \
  --query SecretString \
  --output text)

export DB_PORT=$(aws secretsmanager get-secret-value \
  --secret-id app-inventory/db-port \
  --region ${AWS_REGION} \
  --query SecretString \
  --output text)

export DB_NAME=$(aws secretsmanager get-secret-value \
  --secret-id app-inventory/db-name \
  --region ${AWS_REGION} \
  --query SecretString \
  --output text)

export DB_USER=$(aws secretsmanager get-secret-value \
  --secret-id app-inventory/db-user \
  --region ${AWS_REGION} \
  --query SecretString \
  --output text)

export DB_PASSWORD=$(aws secretsmanager get-secret-value \
  --secret-id app-inventory/db-password \
  --region ${AWS_REGION} \
  --query SecretString \
  --output text)

# Alternative: Fetch from AWS Systems Manager Parameter Store
# Uncomment the following lines if using Parameter Store instead of Secrets Manager
# export DB_HOST=$(aws ssm get-parameter --name /app-inventory/db-host --with-decryption --region ${AWS_REGION} --query Parameter.Value --output text)
# export DB_PORT=$(aws ssm get-parameter --name /app-inventory/db-port --with-decryption --region ${AWS_REGION} --query Parameter.Value --output text)
# export DB_NAME=$(aws ssm get-parameter --name /app-inventory/db-name --with-decryption --region ${AWS_REGION} --query Parameter.Value --output text)
# export DB_USER=$(aws ssm get-parameter --name /app-inventory/db-user --with-decryption --region ${AWS_REGION} --query Parameter.Value --output text)
# export DB_PASSWORD=$(aws ssm get-parameter --name /app-inventory/db-password --with-decryption --region ${AWS_REGION} --query Parameter.Value --output text)

# Build database URL
export SPRING_DATASOURCE_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true&requireSSL=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="${DB_USER}"
export SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD}"

# Set Spring profile
export SPRING_PROFILES_ACTIVE="production"

# Stop existing application if running
echo "Stopping existing application..."
if systemctl is-active --quiet ${APP_NAME}; then
  sudo systemctl stop ${APP_NAME}
fi

# Start the application
echo "Starting application..."
sudo systemctl start ${APP_NAME}

# Check status
sleep 5
if systemctl is-active --quiet ${APP_NAME}; then
  echo "Application started successfully!"
  echo "Health check endpoint: http://localhost:8080/actuator/health"
else
  echo "Failed to start application. Check logs with: journalctl -u ${APP_NAME} -n 50"
  exit 1
fi
