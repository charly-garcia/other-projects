# Terraform Infrastructure for Matriz de Usuarios

This directory contains Terraform Infrastructure as Code (IaC) for deploying the Matriz de Usuarios application on AWS.

## Architecture Overview

The infrastructure creates the following AWS resources:

### Network Layer
- **VPC** with configurable CIDR block
- **Public Subnets** (2 AZs) for Application Load Balancer
- **Private Subnets** (2 AZs) for application instances and RDS
- **Internet Gateway** for public subnet internet access
- **NAT Gateways** (one per AZ) for private subnet outbound connectivity
- **Route Tables** configured for public and private subnets

### Security Layer
- **ALB Security Group**: Allows HTTPS (443) and HTTP (80) from internet
- **Application Security Group**: Allows traffic only from ALB on port 8080
- **RDS Security Group**: Allows MySQL traffic (3306) only from application instances

### Database Layer
- **Amazon RDS MySQL 8.0** in private subnet
- Multi-AZ deployment (optional, configurable)
- Automated backups with configurable retention
- Performance Insights enabled
- CloudWatch Logs integration (error, general, slow query)
- Encrypted storage

### Load Balancer Layer
- **Application Load Balancer** in public subnets
- HTTPS listener on port 443 with ACM certificate
- HTTP listener on port 80 (redirects to HTTPS)
- Target group for backend application with health checks
- Path-based routing for `/api/*` requests

## Prerequisites

1. **Terraform**: Install Terraform >= 1.0
   ```bash
   # macOS
   brew install terraform
   
   # Linux
   wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip
   unzip terraform_1.6.0_linux_amd64.zip
   sudo mv terraform /usr/local/bin/
   ```

2. **AWS CLI**: Configure AWS credentials
   ```bash
   aws configure
   ```

3. **ACM Certificate**: Create an SSL/TLS certificate in AWS Certificate Manager for HTTPS

## Directory Structure

```
terraform/
├── main.tf                      # Main configuration orchestrating all modules
├── variables.tf                 # Input variables
├── terraform.tfvars.example     # Example variable values
├── README.md                    # This file
└── modules/
    ├── vpc/                     # VPC and networking resources
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── security-groups/         # Security group definitions
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── rds/                     # RDS MySQL database
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    └── alb/                     # Application Load Balancer
        ├── main.tf
        ├── variables.tf
        └── outputs.tf
```

## Usage

### 1. Configure Variables

Copy the example variables file and update with your values:

```bash
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` and set:
- `aws_region`: Your AWS region
- `environment`: Environment name (dev, staging, prod)
- `db_password`: Secure database password
- `certificate_arn`: ARN of your ACM certificate
- Other optional parameters as needed

### 2. Initialize Terraform

```bash
cd terraform
terraform init
```

This downloads the required providers and initializes the backend.

### 3. Plan Infrastructure

Review the resources that will be created:

```bash
terraform plan
```

### 4. Apply Infrastructure

Create the infrastructure:

```bash
terraform apply
```

Type `yes` when prompted to confirm.

### 5. Retrieve Outputs

After successful deployment, retrieve important outputs:

```bash
terraform output
```

Key outputs:
- `alb_dns_name`: DNS name of the load balancer
- `rds_endpoint`: Database connection endpoint
- `app_security_group_id`: Security group for EC2/ECS instances

## Configuration Options

### Environment-Specific Settings

The infrastructure adapts based on the `environment` variable:

- **Development** (`dev`):
  - Single-AZ RDS
  - No deletion protection
  - Smaller instance sizes

- **Production** (`prod`):
  - Multi-AZ RDS recommended
  - Deletion protection enabled
  - Final snapshot on deletion

### Database Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `db_instance_class` | RDS instance type | `db.t3.micro` |
| `db_allocated_storage` | Storage in GB | `20` |
| `db_multi_az` | Enable Multi-AZ | `false` |
| `db_backup_retention_days` | Backup retention | `7` |

### Network Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `vpc_cidr` | VPC CIDR block | `10.0.0.0/16` |
| `availability_zones` | AZs to use | `["us-east-1a", "us-east-1b"]` |
| `public_subnet_cidrs` | Public subnet CIDRs | `["10.0.1.0/24", "10.0.2.0/24"]` |
| `private_subnet_cidrs` | Private subnet CIDRs | `["10.0.10.0/24", "10.0.11.0/24"]` |

## Security Best Practices

1. **Database Credentials**: Use AWS Secrets Manager for production:
   ```hcl
   data "aws_secretsmanager_secret_version" "db_password" {
     secret_id = "matriz-usuarios-db-password"
   }
   ```

2. **State Management**: Use remote state with S3 + DynamoDB:
   ```hcl
   terraform {
     backend "s3" {
       bucket         = "your-terraform-state-bucket"
       key            = "matriz-usuarios/terraform.tfstate"
       region         = "us-east-1"
       dynamodb_table = "terraform-state-lock"
       encrypt        = true
     }
   }
   ```

3. **Network Isolation**: RDS is in private subnets with no public access

4. **Encryption**: RDS storage encryption enabled by default

5. **Security Groups**: Principle of least privilege - only necessary ports open

## Connecting Application to Infrastructure

### Backend (Spring Boot)

Configure `application.yml` with environment variables:

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Set environment variables from Terraform outputs:
```bash
export DB_HOST=$(terraform output -raw rds_endpoint | cut -d: -f1)
export DB_PORT=3306
export DB_NAME=$(terraform output -raw rds_database_name)
export DB_USERNAME=admin
export DB_PASSWORD=<your-secure-password>
```

### EC2/ECS Deployment

1. **EC2**: Launch instances in private subnets with `app_security_group_id`
2. **ECS**: Create ECS service with tasks in private subnets
3. **Target Group**: Register instances/tasks with the ALB target group

Example ECS task definition snippet:
```json
{
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "networkConfiguration": {
    "awsvpcConfiguration": {
      "subnets": ["<private-subnet-ids>"],
      "securityGroups": ["<app-security-group-id>"]
    }
  }
}
```

## Maintenance

### Updating Infrastructure

1. Modify variables or module configurations
2. Run `terraform plan` to review changes
3. Run `terraform apply` to apply changes

### Destroying Infrastructure

**Warning**: This will delete all resources including the database.

```bash
terraform destroy
```

For production, ensure you have backups before destroying.

## Troubleshooting

### Issue: Certificate ARN not found
**Solution**: Create an ACM certificate in the same region as your infrastructure

### Issue: RDS creation timeout
**Solution**: RDS creation can take 10-15 minutes. Wait for completion.

### Issue: NAT Gateway costs
**Solution**: For development, consider using a single NAT Gateway or VPC endpoints

### Issue: Health check failing
**Solution**: Ensure Spring Boot Actuator is enabled and `/actuator/health` endpoint is accessible

## Cost Optimization

For development environments:
- Use `db.t3.micro` or `db.t4g.micro` instances
- Disable Multi-AZ (`db_multi_az = false`)
- Use single NAT Gateway (modify VPC module)
- Reduce backup retention to 1-3 days

For production:
- Enable Multi-AZ for high availability
- Use appropriate instance sizes based on load
- Enable deletion protection
- Maintain adequate backup retention (7-30 days)

## Next Steps

After infrastructure is deployed:

1. **Deploy Backend**: Build Spring Boot JAR and deploy to EC2/ECS
2. **Configure DNS**: Point your domain to ALB DNS name using Route 53
3. **Deploy Frontend**: Build Angular app and deploy to S3 + CloudFront
4. **Configure Monitoring**: Set up CloudWatch alarms and dashboards
5. **Enable Logging**: Configure application logs to CloudWatch Logs

## Support

For issues or questions:
- Review Terraform documentation: https://www.terraform.io/docs
- Check AWS documentation: https://docs.aws.amazon.com
- Review module configurations in `modules/` directory
