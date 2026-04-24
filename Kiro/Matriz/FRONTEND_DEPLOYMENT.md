# Frontend Deployment Guide - Matriz de Usuarios

This document describes the frontend deployment architecture and procedures for the Matriz de Usuarios application.

## Architecture Overview

The frontend is deployed using a modern, scalable architecture:

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ HTTPS
       ▼
┌─────────────────────────────────────────┐
│         CloudFront CDN                  │
│  ┌────────────────┬──────────────────┐  │
│  │  Static Files  │   API Proxy      │  │
│  │  (S3 Origin)   │   (ALB Origin)   │  │
│  └────────┬───────┴────────┬─────────┘  │
└───────────┼────────────────┼────────────┘
            │                │
            ▼                ▼
    ┌───────────────┐  ┌──────────────┐
    │  S3 Bucket    │  │     ALB      │
    │  (Angular     │  │  (Backend    │
    │   Build)      │  │   API)       │
    └───────────────┘  └──────────────┘
```

### Components

1. **S3 Bucket**: Stores the static Angular build files (HTML, CSS, JS, assets)
2. **CloudFront Distribution**: Global CDN that:
   - Serves static files from S3 with caching
   - Proxies `/api/*` requests to the backend ALB
   - Handles SPA routing (404/403 → index.html)
   - Provides HTTPS encryption
3. **Origin Access Control (OAC)**: Secures S3 bucket access (only CloudFront can read)

## Features

### ✅ Static Asset Hosting
- Angular production build served from S3
- Global CDN distribution via CloudFront
- Automatic compression (gzip/brotli)

### ✅ SPA Routing Support
- Custom error responses redirect 404/403 to index.html
- Enables Angular client-side routing
- No server-side configuration needed

### ✅ API Proxy Configuration
- `/api/*` requests automatically proxied to backend ALB
- No CORS issues (same origin for frontend and API)
- All cookies and headers forwarded to backend

### ✅ Caching Strategy
- **Static assets** (JS, CSS, images): Cached for 1 day (max 1 year)
- **index.html**: No caching (always fresh for SPA updates)
- **API requests**: No caching (always fresh data)

### ✅ Security
- HTTPS enforced (TLS 1.2+)
- S3 bucket not publicly accessible (OAC only)
- Optional custom domain with ACM certificate

## Prerequisites

Before deploying the frontend, ensure you have:

1. **Terraform Infrastructure Deployed**
   ```bash
   cd terraform
   terraform init
   terraform apply
   ```

2. **Angular Project Created**
   - Angular 17+ project in `./frontend` directory
   - Or specify custom path during deployment

3. **AWS CLI Configured**
   ```bash
   aws configure
   ```

4. **Node.js and npm Installed**
   - Node.js 18+ recommended
   - npm 9+ recommended

## Deployment Steps

### Option 1: Automated Deployment Script (Recommended)

The `deploy-frontend.sh` script automates the entire deployment process:

```bash
# Make script executable
chmod +x deploy-frontend.sh

# Deploy to dev environment (default)
./deploy-frontend.sh

# Deploy to specific environment
./deploy-frontend.sh prod ./frontend

# Usage: ./deploy-frontend.sh [environment] [frontend-directory]
```

The script will:
1. ✅ Check requirements (AWS CLI, Node.js, npm)
2. ✅ Retrieve S3 bucket and CloudFront info from Terraform
3. ✅ Install npm dependencies (if needed)
4. ✅ Build Angular app for production
5. ✅ Upload files to S3 with appropriate cache headers
6. ✅ Create CloudFront invalidation
7. ✅ Wait for invalidation to complete
8. ✅ Display the frontend URL

### Option 2: Manual Deployment

If you prefer manual control:

#### Step 1: Build Angular Application

```bash
cd frontend
npm install
npm run build -- --configuration production
```

#### Step 2: Get Infrastructure Information

```bash
cd ../terraform
export S3_BUCKET=$(terraform output -raw frontend_s3_bucket_name)
export CLOUDFRONT_ID=$(terraform output -raw frontend_cloudfront_distribution_id)
export FRONTEND_URL=$(terraform output -raw frontend_url)
cd ..
```

#### Step 3: Upload to S3

```bash
# Upload all files except index.html with long cache
aws s3 sync frontend/dist/your-app-name/ s3://$S3_BUCKET/ \
    --delete \
    --cache-control "public, max-age=31536000, immutable" \
    --exclude "index.html"

# Upload index.html with no-cache
aws s3 cp frontend/dist/your-app-name/index.html s3://$S3_BUCKET/index.html \
    --cache-control "no-cache, no-store, must-revalidate"
```

#### Step 4: Invalidate CloudFront Cache

```bash
aws cloudfront create-invalidation \
    --distribution-id $CLOUDFRONT_ID \
    --paths "/*"
```

#### Step 5: Access Your Application

```bash
echo "Frontend URL: $FRONTEND_URL"
```

## Configuration

### Terraform Variables

Configure frontend deployment in `terraform/terraform.tfvars`:

```hcl
# CloudFront Configuration
cloudfront_price_class = "PriceClass_100"  # US, Canada, Europe only (cheapest)
# cloudfront_price_class = "PriceClass_200"  # Most locations
# cloudfront_price_class = "PriceClass_All"  # All edge locations (most expensive)

# Optional: Custom Domain Configuration
cloudfront_certificate_arn = "arn:aws:acm:us-east-1:123456789012:certificate/abc123..."
cloudfront_domain_aliases  = ["app.example.com", "www.app.example.com"]
```

**Important Notes:**
- CloudFront certificates **must** be in `us-east-1` region
- Domain aliases require a valid ACM certificate
- You'll need to create a Route 53 alias record pointing to CloudFront

### Angular Environment Configuration

Configure API endpoint in Angular environment files:

**src/environments/environment.prod.ts:**
```typescript
export const environment = {
  production: true,
  apiUrl: '/api/v1'  // Relative URL - proxied by CloudFront
};
```

The `/api` prefix is automatically proxied to the backend ALB by CloudFront.

## Caching Behavior

### Static Assets (JS, CSS, Images)
- **Cache-Control**: `public, max-age=31536000, immutable`
- **CloudFront TTL**: 1 day default, 1 year max
- **Compression**: gzip and brotli enabled
- **Versioning**: Angular build includes content hashes in filenames

### index.html
- **Cache-Control**: `no-cache, no-store, must-revalidate`
- **CloudFront TTL**: 0 (no caching)
- **Reason**: Ensures users always get the latest SPA version

### API Requests (/api/*)
- **CloudFront TTL**: 0 (no caching)
- **Behavior**: All requests forwarded to ALB
- **Headers**: All viewer headers forwarded
- **Cookies**: All cookies forwarded

## SPA Routing

CloudFront is configured to handle Angular's client-side routing:

```hcl
custom_error_response {
  error_code            = 404
  response_code         = 200
  response_page_path    = "/index.html"
  error_caching_min_ttl = 0
}

custom_error_response {
  error_code            = 403
  response_code         = 200
  response_page_path    = "/index.html"
  error_caching_min_ttl = 0
}
```

This means:
- ✅ Direct navigation to `/users` works
- ✅ Refresh on `/applications/123` works
- ✅ Deep linking works
- ✅ Browser back/forward works

## Troubleshooting

### Issue: "Access Denied" when accessing CloudFront URL

**Cause**: S3 bucket policy not applied or OAC not configured correctly.

**Solution**:
```bash
cd terraform
terraform apply  # Re-apply to fix bucket policy
```

### Issue: Old version still showing after deployment

**Cause**: CloudFront cache not invalidated or browser cache.

**Solution**:
```bash
# Create invalidation
aws cloudfront create-invalidation \
    --distribution-id $CLOUDFRONT_ID \
    --paths "/*"

# Clear browser cache or use incognito mode
```

### Issue: API requests failing with CORS errors

**Cause**: API requests not using the `/api` prefix.

**Solution**: Ensure all API calls use `/api/v1/...` prefix:
```typescript
// ✅ Correct
this.http.get('/api/v1/users')

// ❌ Wrong
this.http.get('http://alb-dns-name/api/v1/users')
```

### Issue: 404 on Angular routes

**Cause**: Custom error responses not configured.

**Solution**: Verify CloudFront distribution has custom error responses for 403 and 404. Re-apply Terraform if needed.

### Issue: Slow initial load

**Cause**: CloudFront edge location not warmed up.

**Solution**: 
- First request to a new edge location is slower (cache miss)
- Subsequent requests will be fast (cache hit)
- Consider using CloudFront PriceClass_All for global coverage

## Monitoring

### CloudFront Metrics (CloudWatch)

Monitor your distribution:
```bash
# View CloudFront metrics
aws cloudwatch get-metric-statistics \
    --namespace AWS/CloudFront \
    --metric-name Requests \
    --dimensions Name=DistributionId,Value=$CLOUDFRONT_ID \
    --start-time 2024-01-01T00:00:00Z \
    --end-time 2024-01-02T00:00:00Z \
    --period 3600 \
    --statistics Sum
```

Key metrics to monitor:
- **Requests**: Total number of requests
- **BytesDownloaded**: Data transfer
- **4xxErrorRate**: Client errors
- **5xxErrorRate**: Server errors
- **CacheHitRate**: Percentage of cached responses

### S3 Bucket Size

```bash
aws s3 ls s3://$S3_BUCKET --recursive --summarize
```

## Cost Optimization

### CloudFront Price Classes

Choose based on your user base:

| Price Class | Coverage | Use Case |
|-------------|----------|----------|
| PriceClass_100 | US, Canada, Europe | North American/European users |
| PriceClass_200 | Above + Asia, Africa | Global users (most locations) |
| PriceClass_All | All edge locations | Maximum performance globally |

### S3 Storage

- Angular production builds are typically 1-5 MB
- S3 Standard storage: ~$0.023 per GB/month
- Monthly cost: < $0.12 for typical app

### CloudFront Data Transfer

- First 10 TB/month: $0.085 per GB (US/Europe)
- Caching reduces origin requests and costs
- Compression reduces data transfer

## Security Best Practices

### ✅ Implemented

- [x] HTTPS enforced (TLS 1.2+)
- [x] S3 bucket not publicly accessible
- [x] Origin Access Control (OAC) for S3
- [x] Security headers can be added via Lambda@Edge

### 🔒 Recommended Additions

Consider adding security headers via Lambda@Edge:

```javascript
// Lambda@Edge function for security headers
exports.handler = async (event) => {
    const response = event.Records[0].cf.response;
    const headers = response.headers;

    headers['strict-transport-security'] = [{
        key: 'Strict-Transport-Security',
        value: 'max-age=31536000; includeSubDomains'
    }];
    
    headers['x-content-type-options'] = [{
        key: 'X-Content-Type-Options',
        value: 'nosniff'
    }];
    
    headers['x-frame-options'] = [{
        key: 'X-Frame-Options',
        value: 'DENY'
    }];
    
    headers['x-xss-protection'] = [{
        key: 'X-XSS-Protection',
        value: '1; mode=block'
    }];

    return response;
};
```

## Rollback Procedure

If a deployment causes issues:

### Option 1: Redeploy Previous Version

```bash
# Checkout previous version
git checkout <previous-commit>

# Redeploy
./deploy-frontend.sh
```

### Option 2: S3 Versioning (if enabled)

```bash
# List object versions
aws s3api list-object-versions \
    --bucket $S3_BUCKET \
    --prefix index.html

# Restore previous version
aws s3api copy-object \
    --bucket $S3_BUCKET \
    --copy-source $S3_BUCKET/index.html?versionId=<version-id> \
    --key index.html
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Deploy Frontend

on:
  push:
    branches: [main]
    paths:
      - 'frontend/**'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Configure AWS Credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1
      
      - name: Deploy Frontend
        run: |
          chmod +x deploy-frontend.sh
          ./deploy-frontend.sh prod ./frontend
```

## Additional Resources

- [CloudFront Developer Guide](https://docs.aws.amazon.com/cloudfront/)
- [S3 Static Website Hosting](https://docs.aws.amazon.com/s3/static-website-hosting/)
- [Angular Deployment Guide](https://angular.io/guide/deployment)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)

## Support

For issues or questions:
1. Check the Troubleshooting section above
2. Review CloudFront and S3 logs in CloudWatch
3. Verify Terraform state matches deployed resources
4. Contact the infrastructure team

