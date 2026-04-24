# Main Terraform configuration for Matriz de Usuarios application
# This file orchestrates all infrastructure components

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "MatrizDeUsuarios"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# VPC and Networking
module "vpc" {
  source = "./modules/vpc"

  environment          = var.environment
  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
}

# Security Groups
module "security_groups" {
  source = "./modules/security-groups"

  environment = var.environment
  vpc_id      = module.vpc.vpc_id
  vpc_cidr    = var.vpc_cidr
}

# RDS MySQL Database
module "rds" {
  source = "./modules/rds"

  environment              = var.environment
  vpc_id                   = module.vpc.vpc_id
  private_subnet_ids       = module.vpc.private_subnet_ids
  rds_security_group_id    = module.security_groups.rds_security_group_id
  db_name                  = var.db_name
  db_username              = var.db_username
  db_password              = var.db_password
  db_instance_class        = var.db_instance_class
  db_allocated_storage     = var.db_allocated_storage
  db_multi_az              = var.db_multi_az
  db_backup_retention_days = var.db_backup_retention_days
}

# Application Load Balancer
module "alb" {
  source = "./modules/alb"

  environment           = var.environment
  vpc_id                = module.vpc.vpc_id
  public_subnet_ids     = module.vpc.public_subnet_ids
  alb_security_group_id = module.security_groups.alb_security_group_id
  certificate_arn       = var.certificate_arn
}

# Frontend - S3 + CloudFront for Angular SPA
module "frontend" {
  source = "./modules/frontend"

  environment            = var.environment
  alb_dns_name           = module.alb.alb_dns_name
  certificate_arn        = var.cloudfront_certificate_arn
  domain_aliases         = var.cloudfront_domain_aliases
  cloudfront_price_class = var.cloudfront_price_class
}

# Outputs
output "vpc_id" {
  description = "ID of the VPC"
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "IDs of public subnets"
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "IDs of private subnets"
  value       = module.vpc.private_subnet_ids
}

output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = module.alb.alb_dns_name
}

output "alb_zone_id" {
  description = "Zone ID of the Application Load Balancer"
  value       = module.alb.alb_zone_id
}

output "alb_target_group_arn" {
  description = "ARN of the ALB target group for backend application"
  value       = module.alb.target_group_arn
}

output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = module.rds.db_endpoint
  sensitive   = true
}

output "rds_database_name" {
  description = "RDS database name"
  value       = module.rds.db_name
}

output "app_security_group_id" {
  description = "Security group ID for application instances (EC2/ECS)"
  value       = module.security_groups.app_security_group_id
}
output "frontend_s3_bucket_name" {
  description = "Name of the S3 bucket for frontend static files"
  value       = module.frontend.s3_bucket_name
}

output "frontend_cloudfront_distribution_id" {
  description = "ID of the CloudFront distribution"
  value       = module.frontend.cloudfront_distribution_id
}

output "frontend_cloudfront_domain_name" {
  description = "Domain name of the CloudFront distribution (use this to access the application)"
  value       = module.frontend.cloudfront_domain_name
}

output "frontend_url" {
  description = "Full HTTPS URL to access the frontend application"
  value       = "https://${module.frontend.cloudfront_domain_name}"
}
