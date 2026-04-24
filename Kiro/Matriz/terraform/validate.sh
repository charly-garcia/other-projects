#!/bin/bash

# Terraform Configuration Validation Script
# This script validates the Terraform configuration before deployment

set -e

echo "=========================================="
echo "Terraform Configuration Validation"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the terraform directory
if [ ! -f "main.tf" ]; then
    echo -e "${RED}Error: main.tf not found. Please run this script from the terraform directory.${NC}"
    exit 1
fi

echo "✓ Running from terraform directory"
echo ""

# Check if Terraform is installed
echo "Checking prerequisites..."
if ! command -v terraform &> /dev/null; then
    echo -e "${RED}✗ Terraform is not installed${NC}"
    echo "  Install from: https://www.terraform.io/downloads"
    exit 1
fi
echo -e "${GREEN}✓ Terraform is installed${NC}"

# Check Terraform version
TF_VERSION=$(terraform version -json | grep -o '"terraform_version":"[^"]*' | cut -d'"' -f4)
echo "  Version: $TF_VERSION"

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${YELLOW}⚠ AWS CLI is not installed (optional but recommended)${NC}"
else
    echo -e "${GREEN}✓ AWS CLI is installed${NC}"
    AWS_VERSION=$(aws --version 2>&1 | cut -d' ' -f1)
    echo "  Version: $AWS_VERSION"
fi

echo ""

# Check if terraform.tfvars exists
echo "Checking configuration files..."
if [ ! -f "terraform.tfvars" ]; then
    echo -e "${YELLOW}⚠ terraform.tfvars not found${NC}"
    echo "  Copy terraform.tfvars.example to terraform.tfvars and configure it"
    if [ -f "terraform.tfvars.example" ]; then
        echo -e "${YELLOW}  Run: cp terraform.tfvars.example terraform.tfvars${NC}"
    fi
else
    echo -e "${GREEN}✓ terraform.tfvars exists${NC}"
    
    # Check for required variables
    echo ""
    echo "Checking required variables..."
    
    REQUIRED_VARS=("aws_region" "environment" "db_password" "certificate_arn")
    MISSING_VARS=()
    
    for var in "${REQUIRED_VARS[@]}"; do
        if grep -q "^${var}\s*=" terraform.tfvars; then
            VALUE=$(grep "^${var}\s*=" terraform.tfvars | cut -d'=' -f2 | tr -d ' "')
            if [ -z "$VALUE" ] || [ "$VALUE" == "CHANGE_ME_SECURE_PASSWORD" ] || [[ "$VALUE" == *"your-certificate-id"* ]]; then
                echo -e "${YELLOW}⚠ ${var} needs to be configured${NC}"
                MISSING_VARS+=("$var")
            else
                echo -e "${GREEN}✓ ${var} is configured${NC}"
            fi
        else
            echo -e "${RED}✗ ${var} is missing${NC}"
            MISSING_VARS+=("$var")
        fi
    done
    
    if [ ${#MISSING_VARS[@]} -gt 0 ]; then
        echo ""
        echo -e "${YELLOW}Please configure the following variables in terraform.tfvars:${NC}"
        for var in "${MISSING_VARS[@]}"; do
            echo "  - $var"
        done
    fi
fi

echo ""

# Initialize Terraform if needed
echo "Checking Terraform initialization..."
if [ ! -d ".terraform" ]; then
    echo -e "${YELLOW}⚠ Terraform not initialized${NC}"
    echo "  Run: terraform init"
else
    echo -e "${GREEN}✓ Terraform is initialized${NC}"
fi

echo ""

# Validate Terraform configuration
echo "Validating Terraform configuration..."
if terraform validate > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Terraform configuration is valid${NC}"
else
    echo -e "${RED}✗ Terraform configuration has errors${NC}"
    terraform validate
    exit 1
fi

echo ""

# Format check
echo "Checking Terraform formatting..."
if terraform fmt -check -recursive > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Terraform files are properly formatted${NC}"
else
    echo -e "${YELLOW}⚠ Some files need formatting${NC}"
    echo "  Run: terraform fmt -recursive"
fi

echo ""

# Check AWS credentials if AWS CLI is available
if command -v aws &> /dev/null; then
    echo "Checking AWS credentials..."
    if aws sts get-caller-identity > /dev/null 2>&1; then
        echo -e "${GREEN}✓ AWS credentials are configured${NC}"
        ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
        echo "  Account ID: $ACCOUNT_ID"
    else
        echo -e "${RED}✗ AWS credentials are not configured or invalid${NC}"
        echo "  Run: aws configure"
    fi
fi

echo ""

# Security checks
echo "Running security checks..."

# Check for hardcoded secrets in .tf files
if grep -r "password.*=.*\".*\"" *.tf modules/ 2>/dev/null | grep -v "variable\|description\|sensitive" > /dev/null; then
    echo -e "${RED}✗ Potential hardcoded passwords found in .tf files${NC}"
    echo "  Use variables with sensitive = true instead"
else
    echo -e "${GREEN}✓ No hardcoded passwords in .tf files${NC}"
fi

# Check if terraform.tfvars is in .gitignore
if [ -f ".gitignore" ]; then
    if grep -q "terraform.tfvars" .gitignore; then
        echo -e "${GREEN}✓ terraform.tfvars is in .gitignore${NC}"
    else
        echo -e "${YELLOW}⚠ terraform.tfvars should be in .gitignore${NC}"
    fi
fi

echo ""

# Module checks
echo "Checking modules..."
MODULES=("vpc" "security-groups" "rds" "alb")
for module in "${MODULES[@]}"; do
    if [ -d "modules/$module" ]; then
        if [ -f "modules/$module/main.tf" ] && [ -f "modules/$module/variables.tf" ] && [ -f "modules/$module/outputs.tf" ]; then
            echo -e "${GREEN}✓ Module $module is complete${NC}"
        else
            echo -e "${YELLOW}⚠ Module $module is missing files${NC}"
        fi
    else
        echo -e "${RED}✗ Module $module directory not found${NC}"
    fi
done

echo ""
echo "=========================================="
echo "Validation Summary"
echo "=========================================="

# Final recommendations
echo ""
echo "Next steps:"
echo "1. Ensure all required variables are configured in terraform.tfvars"
echo "2. Run: terraform init (if not already done)"
echo "3. Run: terraform plan (to preview changes)"
echo "4. Run: terraform apply (to create infrastructure)"
echo ""
echo "For detailed guidance, see:"
echo "  - QUICKSTART.md for step-by-step deployment"
echo "  - README.md for comprehensive documentation"
echo "  - ARCHITECTURE.md for infrastructure details"
echo ""

exit 0
