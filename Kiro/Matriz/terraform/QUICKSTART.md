# Quick Start Guide - Matriz de Usuarios Infrastructure

This guide will help you deploy the infrastructure in under 15 minutes.

## Prerequisites Checklist

- [ ] AWS Account with appropriate permissions
- [ ] AWS CLI installed and configured (`aws configure`)
- [ ] Terraform >= 1.0 installed
- [ ] ACM Certificate created in your target region

## Step-by-Step Deployment

### 1. Prepare Configuration (2 minutes)

```bash
# Navigate to terraform directory
cd terraform

# Copy example variables
cp terraform.tfvars.example terraform.tfvars

# Edit variables (use your favorite editor)
nano terraform.tfvars
```

**Minimum required changes:**
```hcl
aws_region      = "us-east-1"              # Your AWS region
environment     = "dev"                     # dev, staging, or prod
db_password     = "YourSecurePassword123!"  # Strong password
certificate_arn = "arn:aws:acm:us-east-1:123456789012:certificate/xxx"  # Your ACM cert
```

### 2. Initialize Terraform (1 minute)

```bash
terraform init
```

Expected output:
```
Terraform has been successfully initialized!
```

### 3. Review Plan (2 minutes)

```bash
terraform plan
```

This shows all resources that will be created. Review the output to ensure everything looks correct.

### 4. Deploy Infrastructure (10-15 minutes)

```bash
terraform apply
```

Type `yes` when prompted.

**Note**: RDS creation takes the longest (10-15 minutes). Be patient!

### 5. Save Outputs (1 minute)

```bash
# View all outputs
terraform output

# Save specific outputs for later use
terraform output -raw alb_dns_name > alb_dns.txt
terraform output -raw rds_endpoint > rds_endpoint.txt
```

## What Gets Created?

### Network Infrastructure
- ✅ VPC with public and private subnets across 2 availability zones
- ✅ Internet Gateway for public internet access
- ✅ NAT Gateways for private subnet outbound connectivity
- ✅ Route tables configured appropriately

### Security
- ✅ ALB Security Group (allows HTTPS from internet)
- ✅ App Security Group (allows traffic only from ALB)
- ✅ RDS Security Group (allows MySQL only from app)

### Database
- ✅ RDS MySQL 8.0 instance in private subnet
- ✅ Automated backups enabled
- ✅ Performance Insights enabled
- ✅ CloudWatch Logs integration

### Load Balancer
- ✅ Application Load Balancer in public subnets
- ✅ HTTPS listener with your ACM certificate
- ✅ HTTP to HTTPS redirect
- ✅ Target group with health checks

## Verify Deployment

### Check ALB is accessible

```bash
ALB_DNS=$(terraform output -raw alb_dns_name)
curl -I https://$ALB_DNS
```

Expected: Connection established (may show 503 until backend is deployed)

### Check RDS is running

```bash
aws rds describe-db-instances \
  --db-instance-identifier dev-matriz-usuarios-db \
  --query 'DBInstances[0].DBInstanceStatus'
```

Expected: `"available"`

### Check Security Groups

```bash
# List security groups
aws ec2 describe-security-groups \
  --filters "Name=tag:Project,Values=MatrizDeUsuarios" \
  --query 'SecurityGroups[*].[GroupName,GroupId]' \
  --output table
```

## Connect Your Application

### Backend Configuration

Use these environment variables in your Spring Boot application:

```bash
# Extract database endpoint (without port)
DB_HOST=$(terraform output -raw rds_endpoint | cut -d: -f1)
DB_PORT=3306
DB_NAME=$(terraform output -raw rds_database_name)
DB_USERNAME=admin
DB_PASSWORD=<your-password-from-tfvars>

# Export for local testing
export DB_HOST DB_PORT DB_NAME DB_USERNAME DB_PASSWORD
```

### Deploy to EC2 (Example)

```bash
# Get required IDs
APP_SG=$(terraform output -raw app_security_group_id)
PRIVATE_SUBNET=$(terraform output -json private_subnet_ids | jq -r '.[0]')
TARGET_GROUP=$(terraform output -raw alb_target_group_arn)

# Launch EC2 instance (example)
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --instance-type t3.micro \
  --subnet-id $PRIVATE_SUBNET \
  --security-group-ids $APP_SG \
  --user-data file://deploy-script.sh \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=matriz-backend}]'

# Register instance with target group
INSTANCE_ID=<your-instance-id>
aws elbv2 register-targets \
  --target-group-arn $TARGET_GROUP \
  --targets Id=$INSTANCE_ID
```

## Common Issues & Solutions

### Issue: "Error creating DB Instance: InvalidParameterValue"
**Cause**: Invalid database password
**Solution**: Ensure password is at least 8 characters with letters and numbers

### Issue: "Error creating ALB: CertificateNotFound"
**Cause**: Certificate ARN is incorrect or in wrong region
**Solution**: Verify certificate exists in the same region as your infrastructure

### Issue: "Error creating NAT Gateway: InsufficientFreeAddressesInSubnet"
**Cause**: Not enough IP addresses in public subnet
**Solution**: Use larger CIDR blocks for public subnets

### Issue: Health checks failing
**Cause**: Application not responding on `/actuator/health`
**Solution**: Ensure Spring Boot Actuator is enabled and accessible

## Clean Up

To destroy all resources:

```bash
terraform destroy
```

**Warning**: This deletes everything including the database. Ensure you have backups!

## Next Steps

1. ✅ Infrastructure deployed
2. ⬜ Deploy Spring Boot backend to EC2/ECS
3. ⬜ Configure Route 53 DNS
4. ⬜ Deploy Angular frontend to S3 + CloudFront
5. ⬜ Set up monitoring and alerts
6. ⬜ Configure CI/CD pipeline

## Cost Estimate

**Development environment** (approximate monthly costs):
- VPC & Networking: $0 (free tier)
- NAT Gateways: ~$65 (2 NAT Gateways × $0.045/hour)
- RDS db.t3.micro: ~$15
- ALB: ~$20
- **Total: ~$100/month**

**Cost optimization tips:**
- Use single NAT Gateway for dev: saves ~$32/month
- Use db.t4g.micro: saves ~$5/month
- Stop RDS when not in use: saves proportionally

## Support Resources

- [Terraform AWS Provider Docs](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS RDS Documentation](https://docs.aws.amazon.com/rds/)
- [AWS ALB Documentation](https://docs.aws.amazon.com/elasticloadbalancing/)
- [Project README](./README.md)

## Troubleshooting Commands

```bash
# Check Terraform state
terraform show

# List all resources
terraform state list

# Get specific resource details
terraform state show module.rds.aws_db_instance.main

# Refresh state from AWS
terraform refresh

# Validate configuration
terraform validate

# Format configuration files
terraform fmt -recursive
```

---

**Need help?** Review the detailed [README.md](./README.md) or check AWS CloudWatch logs for specific resource issues.
