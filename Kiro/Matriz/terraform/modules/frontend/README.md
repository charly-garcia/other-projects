# Frontend Module - S3 + CloudFront

This Terraform module creates the infrastructure for hosting an Angular SPA (Single Page Application) using Amazon S3 and CloudFront.

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                  CloudFront Distribution                 │
│                                                          │
│  ┌────────────────────────┐  ┌──────────────────────┐   │
│  │   Default Behavior     │  │  Ordered Behavior    │   │
│  │   Path: /*             │  │  Path: /api/*        │   │
│  │   Origin: S3           │  │  Origin: ALB         │   │
│  │   Cache: Yes           │  │  Cache: No           │   │
│  └────────────────────────┘  └──────────────────────┘   │
│                                                          │
│  Custom Error Responses:                                │
│  - 404 → /index.html (200)                              │
│  - 403 → /index.html (200)                              │
└──────────────────────────────────────────────────────────┘
           │                              │
           ▼                              ▼
    ┌─────────────┐              ┌──────────────┐
    │  S3 Bucket  │              │     ALB      │
    │  (Private)  │              │  (Backend)   │
    │             │              │              │
    │  - OAC      │              │  - HTTPS     │
    │  - Blocked  │              │  - Health    │
    │    Public   │              │    Checks    │
    └─────────────┘              └──────────────┘
```

## Features

### 🔒 Security
- **Origin Access Control (OAC)**: S3 bucket is private, only CloudFront can access
- **HTTPS Enforced**: TLS 1.2+ required
- **No Public Access**: S3 bucket blocks all public access
- **Signed Requests**: CloudFront signs requests to S3 using SigV4

### 🚀 Performance
- **Global CDN**: Content served from edge locations worldwide
- **Compression**: Automatic gzip and brotli compression
- **Caching**: Intelligent caching strategy for static assets
- **HTTP/2**: Enabled by default

### 🎯 SPA Support
- **Client-Side Routing**: 404/403 errors redirect to index.html
- **Deep Linking**: Direct navigation to any route works
- **Refresh Support**: Page refresh maintains current route

### 🔄 API Proxy
- **Seamless Integration**: /api/* requests proxied to backend ALB
- **No CORS Issues**: Same origin for frontend and API
- **Header Forwarding**: All headers and cookies forwarded

## Usage

```hcl
module "frontend" {
  source = "./modules/frontend"

  environment             = "prod"
  alb_dns_name            = module.alb.alb_dns_name
  certificate_arn         = "arn:aws:acm:us-east-1:123456789012:certificate/abc123..."
  domain_aliases          = ["app.example.com"]
  cloudfront_price_class  = "PriceClass_100"
}
```

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|----------|
| environment | Environment name (dev, staging, prod) | string | - | yes |
| alb_dns_name | DNS name of the ALB for API proxy | string | - | yes |
| certificate_arn | ARN of ACM certificate (must be in us-east-1) | string | "" | no |
| domain_aliases | List of custom domains for CloudFront | list(string) | [] | no |
| cloudfront_price_class | CloudFront price class | string | "PriceClass_100" | no |

### CloudFront Price Classes

| Price Class | Coverage | Use Case |
|-------------|----------|----------|
| PriceClass_100 | US, Canada, Europe | Regional apps (cheapest) |
| PriceClass_200 | Above + Asia, Africa, South America | Global apps (most locations) |
| PriceClass_All | All edge locations | Maximum global performance |

## Outputs

| Name | Description |
|------|-------------|
| s3_bucket_name | Name of the S3 bucket |
| s3_bucket_arn | ARN of the S3 bucket |
| cloudfront_distribution_id | ID of the CloudFront distribution |
| cloudfront_domain_name | Domain name of CloudFront distribution |
| cloudfront_hosted_zone_id | Route 53 zone ID for CloudFront |

## Caching Strategy

### Static Assets (JS, CSS, Images)
```hcl
Cache-Control: public, max-age=31536000, immutable
CloudFront TTL: 1 day (default), 1 year (max)
```
- Long cache duration for versioned assets
- Angular build includes content hashes in filenames
- Compression enabled (gzip + brotli)

### index.html
```hcl
Cache-Control: no-cache, no-store, must-revalidate
CloudFront TTL: 0 (no caching)
```
- Always fetch fresh version
- Ensures users get latest SPA version
- No stale app versions

### API Requests (/api/*)
```hcl
CloudFront TTL: 0 (no caching)
Origin Request Policy: Forward all headers, cookies, query strings
```
- No caching for dynamic data
- All request context forwarded to backend
- Maintains session state

## Custom Domain Setup

### 1. Request ACM Certificate (us-east-1)
```bash
aws acm request-certificate \
    --domain-name app.example.com \
    --validation-method DNS \
    --region us-east-1
```

**Important**: CloudFront requires certificates in `us-east-1` region.

### 2. Validate Certificate
Add the DNS validation records to your domain's DNS.

### 3. Update Terraform Variables
```hcl
cloudfront_certificate_arn = "arn:aws:acm:us-east-1:123456789012:certificate/abc123..."
domain_aliases             = ["app.example.com"]
```

### 4. Create Route 53 Alias Record
```hcl
resource "aws_route53_record" "frontend" {
  zone_id = aws_route53_zone.main.zone_id
  name    = "app.example.com"
  type    = "A"

  alias {
    name                   = module.frontend.cloudfront_domain_name
    zone_id                = module.frontend.cloudfront_hosted_zone_id
    evaluate_target_health = false
  }
}
```

## Deployment Workflow

### 1. Build Angular App
```bash
cd frontend
npm run build -- --configuration production
```

### 2. Upload to S3
```bash
# Static assets with long cache
aws s3 sync dist/app-name/ s3://bucket-name/ \
    --delete \
    --cache-control "public, max-age=31536000, immutable" \
    --exclude "index.html"

# index.html with no cache
aws s3 cp dist/app-name/index.html s3://bucket-name/index.html \
    --cache-control "no-cache, no-store, must-revalidate"
```

### 3. Invalidate CloudFront Cache
```bash
aws cloudfront create-invalidation \
    --distribution-id DISTRIBUTION_ID \
    --paths "/*"
```

## Monitoring

### CloudFront Metrics
- **Requests**: Total number of requests
- **BytesDownloaded**: Data transfer volume
- **4xxErrorRate**: Client error rate
- **5xxErrorRate**: Server error rate
- **CacheHitRate**: Percentage of cached responses

### CloudWatch Alarms
```hcl
resource "aws_cloudwatch_metric_alarm" "high_5xx_rate" {
  alarm_name          = "${var.environment}-cloudfront-high-5xx"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "5xxErrorRate"
  namespace           = "AWS/CloudFront"
  period              = "300"
  statistic           = "Average"
  threshold           = "5"
  alarm_description   = "CloudFront 5xx error rate is too high"
  
  dimensions = {
    DistributionId = aws_cloudfront_distribution.frontend.id
  }
}
```

## Security Considerations

### ✅ Implemented
- [x] S3 bucket not publicly accessible
- [x] Origin Access Control (OAC) for S3 access
- [x] HTTPS enforced (TLS 1.2+)
- [x] Signed requests to S3 (SigV4)

### 🔒 Recommended Additions

#### 1. Security Headers (Lambda@Edge)
Add security headers to responses:
- `Strict-Transport-Security`
- `X-Content-Type-Options`
- `X-Frame-Options`
- `X-XSS-Protection`
- `Content-Security-Policy`

#### 2. WAF (Web Application Firewall)
```hcl
resource "aws_wafv2_web_acl" "frontend" {
  name  = "${var.environment}-frontend-waf"
  scope = "CLOUDFRONT"
  
  default_action {
    allow {}
  }
  
  rule {
    name     = "RateLimitRule"
    priority = 1
    
    action {
      block {}
    }
    
    statement {
      rate_based_statement {
        limit              = 2000
        aggregate_key_type = "IP"
      }
    }
    
    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "RateLimitRule"
      sampled_requests_enabled   = true
    }
  }
}
```

#### 3. CloudFront Access Logs
```hcl
resource "aws_s3_bucket" "logs" {
  bucket = "${var.environment}-cloudfront-logs"
}

# Add to CloudFront distribution
logging_config {
  include_cookies = false
  bucket          = aws_s3_bucket.logs.bucket_domain_name
  prefix          = "cloudfront/"
}
```

## Troubleshooting

### Issue: Access Denied (403)
**Cause**: S3 bucket policy not allowing CloudFront OAC.

**Solution**: Verify bucket policy includes CloudFront service principal:
```json
{
  "Effect": "Allow",
  "Principal": {
    "Service": "cloudfront.amazonaws.com"
  },
  "Action": "s3:GetObject",
  "Resource": "arn:aws:s3:::bucket-name/*",
  "Condition": {
    "StringEquals": {
      "AWS:SourceArn": "arn:aws:cloudfront::account-id:distribution/distribution-id"
    }
  }
}
```

### Issue: 404 on Angular Routes
**Cause**: Custom error responses not configured.

**Solution**: Verify CloudFront has custom error responses for 403 and 404 pointing to /index.html.

### Issue: API Requests Failing
**Cause**: API requests not using /api prefix or CORS issues.

**Solution**: Ensure all API calls use relative URLs with /api prefix:
```typescript
// ✅ Correct
this.http.get('/api/v1/users')

// ❌ Wrong
this.http.get('https://alb-dns-name/api/v1/users')
```

### Issue: Old Version Cached
**Cause**: CloudFront cache not invalidated.

**Solution**: Create invalidation for all paths:
```bash
aws cloudfront create-invalidation \
    --distribution-id DISTRIBUTION_ID \
    --paths "/*"
```

## Cost Optimization

### 1. Choose Appropriate Price Class
- **PriceClass_100**: Cheapest, good for US/Canada/Europe
- **PriceClass_200**: Mid-tier, covers most locations
- **PriceClass_All**: Most expensive, all edge locations

### 2. Optimize Caching
- Long cache TTLs for static assets
- Use versioned filenames (Angular does this automatically)
- Enable compression

### 3. Monitor Data Transfer
- Review CloudWatch metrics regularly
- Identify and optimize large assets
- Consider image optimization

## Examples

### Basic Setup (Default CloudFront Certificate)
```hcl
module "frontend" {
  source = "./modules/frontend"

  environment    = "dev"
  alb_dns_name   = "my-alb-123456.us-east-1.elb.amazonaws.com"
}
```

### Production Setup (Custom Domain)
```hcl
module "frontend" {
  source = "./modules/frontend"

  environment             = "prod"
  alb_dns_name            = module.alb.alb_dns_name
  certificate_arn         = "arn:aws:acm:us-east-1:123456789012:certificate/abc123..."
  domain_aliases          = ["app.example.com", "www.app.example.com"]
  cloudfront_price_class  = "PriceClass_200"
}
```

### Global Setup (All Edge Locations)
```hcl
module "frontend" {
  source = "./modules/frontend"

  environment             = "prod"
  alb_dns_name            = module.alb.alb_dns_name
  certificate_arn         = "arn:aws:acm:us-east-1:123456789012:certificate/abc123..."
  domain_aliases          = ["app.example.com"]
  cloudfront_price_class  = "PriceClass_All"
}
```

## Resources Created

This module creates the following AWS resources:

1. **S3 Bucket**: For static file storage
2. **S3 Bucket Public Access Block**: Blocks all public access
3. **S3 Bucket Policy**: Allows CloudFront OAC access
4. **CloudFront Origin Access Control**: Secures S3 access
5. **CloudFront Cache Policies** (2): For static assets and no-cache
6. **CloudFront Origin Request Policy**: For API proxy
7. **CloudFront Distribution**: Main CDN distribution

## License

This module is part of the Matriz de Usuarios project.

