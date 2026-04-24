#!/bin/bash

# AWS Setup Commands for App Inventory Management Backend Deployment
# This script provides all the AWS CLI commands needed to set up the infrastructure
# Review and customize the variables before running

set -e

# ============================================================================
# CONFIGURATION VARIABLES - UPDATE THESE VALUES
# ============================================================================

export AWS_REGION="us-east-1"
export AWS_ACCOUNT_ID="123456789012"  # Replace with your AWS account ID
export VPC_ID="vpc-xxxxxxxxx"         # Replace with your VPC ID
export SUBNET_1="subnet-xxxxxxxxx"    # Replace with your first subnet ID
export SUBNET_2="subnet-xxxxxxxxx"    # Replace with your second subnet ID
export RDS_ENDPOINT="your-rds-endpoint.rds.amazonaws.com"
export DB_NAME="matriz_usuarios"
export DB_USER="admin"
export DB_PASSWORD="YourSecurePassword123!"  # Use a strong password

# ============================================================================
# 1. CREATE SECRETS IN AWS SECRETS MANAGER
# ============================================================================

echo "Creating secrets in AWS Secrets Manager..."

aws secretsmanager create-secret \
  --name app-inventory/db-host \
  --secret-string "${RDS_ENDPOINT}" \
  --description "Database host for App Inventory Management" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name app-inventory/db-port \
  --secret-string "3306" \
  --description "Database port for App Inventory Management" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name app-inventory/db-name \
  --secret-string "${DB_NAME}" \
  --description "Database name for App Inventory Management" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name app-inventory/db-user \
  --secret-string "${DB_USER}" \
  --description "Database user for App Inventory Management" \
  --region ${AWS_REGION}

aws secretsmanager create-secret \
  --name app-inventory/db-password \
  --secret-string "${DB_PASSWORD}" \
  --description "Database password for App Inventory Management" \
  --region ${AWS_REGION}

echo "✓ Secrets created successfully"

# ============================================================================
# 2. CREATE ECR REPOSITORY
# ============================================================================

echo "Creating ECR repository..."

aws ecr create-repository \
  --repository-name app-inventory-management \
  --image-scanning-configuration scanOnPush=true \
  --region ${AWS_REGION}

echo "✓ ECR repository created"

# ============================================================================
# 3. CREATE CLOUDWATCH LOG GROUP
# ============================================================================

echo "Creating CloudWatch log group..."

aws logs create-log-group \
  --log-group-name /ecs/app-inventory-management \
  --region ${AWS_REGION}

aws logs put-retention-policy \
  --log-group-name /ecs/app-inventory-management \
  --retention-in-days 30 \
  --region ${AWS_REGION}

echo "✓ CloudWatch log group created"

# ============================================================================
# 4. CREATE IAM ROLES
# ============================================================================

echo "Creating IAM roles..."

# ECS Task Execution Role
cat > ecs-task-execution-role-trust-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

aws iam create-role \
  --role-name ecsTaskExecutionRole \
  --assume-role-policy-document file://ecs-task-execution-role-trust-policy.json

aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# Custom policy for Secrets Manager access
cat > secrets-manager-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:${AWS_REGION}:${AWS_ACCOUNT_ID}:secret:app-inventory/*"
      ]
    }
  ]
}
EOF

aws iam create-policy \
  --policy-name AppInventorySecretsManagerAccess \
  --policy-document file://secrets-manager-policy.json

aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::${AWS_ACCOUNT_ID}:policy/AppInventorySecretsManagerAccess

# ECS Task Role (for application runtime)
aws iam create-role \
  --role-name ecsTaskRole \
  --assume-role-policy-document file://ecs-task-execution-role-trust-policy.json

echo "✓ IAM roles created"

# ============================================================================
# 5. CREATE SECURITY GROUPS
# ============================================================================

echo "Creating security groups..."

# ECS Security Group
ECS_SG_ID=$(aws ec2 create-security-group \
  --group-name app-inventory-ecs-sg \
  --description "Security group for App Inventory ECS tasks" \
  --vpc-id ${VPC_ID} \
  --region ${AWS_REGION} \
  --query 'GroupId' \
  --output text)

# Allow inbound traffic from ALB (port 8080)
aws ec2 authorize-security-group-ingress \
  --group-id ${ECS_SG_ID} \
  --protocol tcp \
  --port 8080 \
  --source-group ${ALB_SG_ID} \
  --region ${AWS_REGION}

echo "✓ Security groups created: ${ECS_SG_ID}"

# ============================================================================
# 6. CREATE APPLICATION LOAD BALANCER TARGET GROUP
# ============================================================================

echo "Creating ALB target group..."

TARGET_GROUP_ARN=$(aws elbv2 create-target-group \
  --name app-inventory-backend-tg \
  --protocol HTTP \
  --port 8080 \
  --vpc-id ${VPC_ID} \
  --health-check-protocol HTTP \
  --health-check-port 8080 \
  --health-check-path /actuator/health \
  --health-check-interval-seconds 30 \
  --health-check-timeout-seconds 5 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3 \
  --matcher HttpCode=200 \
  --target-type ip \
  --region ${AWS_REGION} \
  --query 'TargetGroups[0].TargetGroupArn' \
  --output text)

echo "✓ Target group created: ${TARGET_GROUP_ARN}"

# ============================================================================
# 7. CREATE ECS CLUSTER
# ============================================================================

echo "Creating ECS cluster..."

aws ecs create-cluster \
  --cluster-name app-inventory-cluster \
  --region ${AWS_REGION}

echo "✓ ECS cluster created"

# ============================================================================
# 8. BUILD AND PUSH DOCKER IMAGE
# ============================================================================

echo "Building and pushing Docker image..."

# Build the Docker image
docker build -t app-inventory-management:latest .

# Tag for ECR
docker tag app-inventory-management:latest \
  ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/app-inventory-management:latest

# Login to ECR
aws ecr get-login-password --region ${AWS_REGION} | \
  docker login --username AWS --password-stdin \
  ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

# Push to ECR
docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/app-inventory-management:latest

echo "✓ Docker image pushed to ECR"

# ============================================================================
# 9. REGISTER ECS TASK DEFINITION
# ============================================================================

echo "Registering ECS task definition..."

# Update the task definition file with actual values
sed -i "s/<ACCOUNT_ID>/${AWS_ACCOUNT_ID}/g" ecs-task-definition.json
sed -i "s/<REGION>/${AWS_REGION}/g" ecs-task-definition.json

aws ecs register-task-definition \
  --cli-input-json file://ecs-task-definition.json \
  --region ${AWS_REGION}

echo "✓ Task definition registered"

# ============================================================================
# 10. CREATE ECS SERVICE
# ============================================================================

echo "Creating ECS service..."

aws ecs create-service \
  --cluster app-inventory-cluster \
  --service-name app-inventory-backend-service \
  --task-definition app-inventory-management \
  --desired-count 2 \
  --launch-type FARGATE \
  --platform-version LATEST \
  --network-configuration "awsvpcConfiguration={subnets=[${SUBNET_1},${SUBNET_2}],securityGroups=[${ECS_SG_ID}],assignPublicIp=DISABLED}" \
  --load-balancers "targetGroupArn=${TARGET_GROUP_ARN},containerName=app-inventory-backend,containerPort=8080" \
  --health-check-grace-period-seconds 60 \
  --region ${AWS_REGION}

echo "✓ ECS service created"

# ============================================================================
# 11. CREATE CLOUDWATCH ALARMS
# ============================================================================

echo "Creating CloudWatch alarms..."

# Unhealthy target alarm
aws cloudwatch put-metric-alarm \
  --alarm-name app-inventory-unhealthy-targets \
  --alarm-description "Alert when targets are unhealthy" \
  --metric-name UnHealthyHostCount \
  --namespace AWS/ApplicationELB \
  --statistic Average \
  --period 60 \
  --evaluation-periods 2 \
  --threshold 1 \
  --comparison-operator GreaterThanOrEqualToThreshold \
  --dimensions Name=TargetGroup,Value=${TARGET_GROUP_ARN} \
  --region ${AWS_REGION}

# High CPU alarm
aws cloudwatch put-metric-alarm \
  --alarm-name app-inventory-high-cpu \
  --alarm-description "Alert when CPU utilization is high" \
  --metric-name CPUUtilization \
  --namespace AWS/ECS \
  --statistic Average \
  --period 300 \
  --evaluation-periods 2 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --dimensions Name=ServiceName,Value=app-inventory-backend-service Name=ClusterName,Value=app-inventory-cluster \
  --region ${AWS_REGION}

echo "✓ CloudWatch alarms created"

# ============================================================================
# CLEANUP TEMPORARY FILES
# ============================================================================

rm -f ecs-task-execution-role-trust-policy.json
rm -f secrets-manager-policy.json

# ============================================================================
# SUMMARY
# ============================================================================

echo ""
echo "============================================================================"
echo "DEPLOYMENT COMPLETE!"
echo "============================================================================"
echo ""
echo "Resources created:"
echo "  - Secrets Manager secrets: app-inventory/*"
echo "  - ECR repository: app-inventory-management"
echo "  - CloudWatch log group: /ecs/app-inventory-management"
echo "  - IAM roles: ecsTaskExecutionRole, ecsTaskRole"
echo "  - Security group: ${ECS_SG_ID}"
echo "  - Target group: ${TARGET_GROUP_ARN}"
echo "  - ECS cluster: app-inventory-cluster"
echo "  - ECS service: app-inventory-backend-service"
echo ""
echo "Next steps:"
echo "  1. Configure ALB listener rule to forward traffic to the target group"
echo "  2. Update DNS to point to the ALB"
echo "  3. Monitor service health: aws ecs describe-services --cluster app-inventory-cluster --services app-inventory-backend-service"
echo "  4. View logs: aws logs tail /ecs/app-inventory-management --follow"
echo ""
echo "Health check endpoint: http://<ALB_DNS>/actuator/health"
echo "============================================================================"
