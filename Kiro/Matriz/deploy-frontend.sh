#!/bin/bash

# Deploy Frontend Script
# This script builds the Angular application and deploys it to S3 + CloudFront

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if required tools are installed
check_requirements() {
    print_info "Checking requirements..."
    
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed. Please install it first."
        exit 1
    fi
    
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed. Please install it first."
        exit 1
    fi
    
    if ! command -v npm &> /dev/null; then
        print_error "npm is not installed. Please install it first."
        exit 1
    fi
    
    print_info "All requirements met."
}

# Parse command line arguments
ENVIRONMENT=${1:-dev}
FRONTEND_DIR=${2:-./frontend}

print_info "Deployment Configuration:"
echo "  Environment: $ENVIRONMENT"
echo "  Frontend Directory: $FRONTEND_DIR"

# Check if frontend directory exists
if [ ! -d "$FRONTEND_DIR" ]; then
    print_error "Frontend directory not found: $FRONTEND_DIR"
    print_info "Please create an Angular project first or specify the correct path."
    print_info "Usage: $0 [environment] [frontend-directory]"
    print_info "Example: $0 dev ./frontend"
    exit 1
fi

# Get S3 bucket name and CloudFront distribution ID from Terraform outputs
print_info "Retrieving infrastructure information from Terraform..."
cd terraform

if [ ! -f "terraform.tfstate" ]; then
    print_error "Terraform state file not found. Please run 'terraform apply' first."
    exit 1
fi

S3_BUCKET=$(terraform output -raw frontend_s3_bucket_name 2>/dev/null)
CLOUDFRONT_ID=$(terraform output -raw frontend_cloudfront_distribution_id 2>/dev/null)
CLOUDFRONT_URL=$(terraform output -raw frontend_url 2>/dev/null)

if [ -z "$S3_BUCKET" ] || [ -z "$CLOUDFRONT_ID" ]; then
    print_error "Could not retrieve S3 bucket or CloudFront distribution ID from Terraform."
    print_error "Please ensure the frontend module is deployed."
    exit 1
fi

print_info "S3 Bucket: $S3_BUCKET"
print_info "CloudFront Distribution: $CLOUDFRONT_ID"
print_info "Frontend URL: $CLOUDFRONT_URL"

cd ..

# Build Angular application
print_info "Building Angular application..."
cd "$FRONTEND_DIR"

if [ ! -f "package.json" ]; then
    print_error "package.json not found in $FRONTEND_DIR. Is this an Angular project?"
    exit 1
fi

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    print_info "Installing npm dependencies..."
    npm install
fi

# Build for production
print_info "Running production build..."
npm run build -- --configuration production

# Check if build was successful
if [ ! -d "dist" ]; then
    print_error "Build failed. dist directory not found."
    exit 1
fi

# Find the build output directory (usually dist/project-name)
BUILD_DIR=$(find dist -mindepth 1 -maxdepth 1 -type d | head -n 1)

if [ -z "$BUILD_DIR" ]; then
    print_error "Could not find build output directory in dist/"
    exit 1
fi

print_info "Build output directory: $BUILD_DIR"

cd ..

# Upload to S3
print_info "Uploading files to S3 bucket: $S3_BUCKET..."
aws s3 sync "$FRONTEND_DIR/$BUILD_DIR/" "s3://$S3_BUCKET/" \
    --delete \
    --cache-control "public, max-age=31536000, immutable" \
    --exclude "index.html"

# Upload index.html separately with no-cache headers (for SPA routing)
print_info "Uploading index.html with no-cache headers..."
aws s3 cp "$FRONTEND_DIR/$BUILD_DIR/index.html" "s3://$S3_BUCKET/index.html" \
    --cache-control "no-cache, no-store, must-revalidate" \
    --metadata-directive REPLACE

# Invalidate CloudFront cache
print_info "Creating CloudFront invalidation..."
INVALIDATION_ID=$(aws cloudfront create-invalidation \
    --distribution-id "$CLOUDFRONT_ID" \
    --paths "/*" \
    --query 'Invalidation.Id' \
    --output text)

print_info "CloudFront invalidation created: $INVALIDATION_ID"
print_info "Waiting for invalidation to complete (this may take a few minutes)..."

aws cloudfront wait invalidation-completed \
    --distribution-id "$CLOUDFRONT_ID" \
    --id "$INVALIDATION_ID"

print_info "Invalidation completed!"

# Print success message
echo ""
print_info "========================================="
print_info "Frontend deployment completed successfully!"
print_info "========================================="
echo ""
print_info "Your application is now available at:"
echo "  $CLOUDFRONT_URL"
echo ""
print_info "Note: It may take a few minutes for CloudFront to fully propagate the changes."
echo ""

