# Frontend Deployment - Quick Start Guide

## 🚀 Quick Deployment (5 minutes)

### Prerequisites Check
```bash
# Verify AWS CLI
aws --version

# Verify Node.js
node --version  # Should be 18+

# Verify npm
npm --version

# Verify Terraform is applied
cd terraform && terraform output frontend_url && cd ..
```

### Deploy in 3 Steps

#### 1. Make deployment script executable
```bash
chmod +x deploy-frontend.sh
```

#### 2. Run deployment
```bash
./deploy-frontend.sh
```

#### 3. Access your application
The script will output the CloudFront URL at the end:
```
Frontend URL: https://d1234567890abc.cloudfront.net
```

## 📋 What Gets Deployed

```
┌─────────────────────────────────────────┐
│         CloudFront Distribution         │
│                                         │
│  ┌─────────────┐    ┌──────────────┐   │
│  │   Static    │    │  API Proxy   │   │
│  │   Files     │    │  /api/* →    │   │
│  │   (S3)      │    │  ALB         │   │
│  └─────────────┘    └──────────────┘   │
└─────────────────────────────────────────┘
```

### Features Enabled
- ✅ Global CDN (CloudFront)
- ✅ HTTPS encryption
- ✅ SPA routing support (404 → index.html)
- ✅ API proxy (/api/* → backend)
- ✅ Optimized caching
- ✅ Automatic compression

## 🔧 Configuration

### Default Configuration
The deployment uses sensible defaults:
- **Environment**: dev
- **Frontend Directory**: ./frontend
- **CloudFront Price Class**: PriceClass_100 (US, Canada, Europe)

### Custom Configuration

#### Deploy to different environment
```bash
./deploy-frontend.sh prod
```

#### Deploy from custom directory
```bash
./deploy-frontend.sh dev ./my-angular-app
```

#### Use custom domain (requires ACM certificate)
Edit `terraform/terraform.tfvars`:
```hcl
cloudfront_certificate_arn = "arn:aws:acm:us-east-1:123456789012:certificate/abc123..."
cloudfront_domain_aliases  = ["app.example.com"]
```

Then re-apply Terraform:
```bash
cd terraform
terraform apply
cd ..
```

## 🏗️ First Time Setup

If you haven't created the Angular frontend yet:

### 1. Create Angular Project
```bash
npm install -g @angular/cli
ng new frontend --routing --style=scss
cd frontend
```

### 2. Configure API Endpoint
Edit `src/environments/environment.prod.ts`:
```typescript
export const environment = {
  production: true,
  apiUrl: '/api/v1'  // CloudFront will proxy to backend
};
```

### 3. Update API Service
```typescript
import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private apiUrl = environment.apiUrl;
  
  constructor(private http: HttpClient) {}
  
  getUsers() {
    return this.http.get(`${this.apiUrl}/users`);
  }
}
```

### 4. Deploy
```bash
cd ..
./deploy-frontend.sh
```

## 🔍 Verify Deployment

### Check CloudFront Status
```bash
cd terraform
terraform output frontend_url
cd ..
```

### Test Static Files
```bash
curl -I https://your-cloudfront-url.cloudfront.net
```

### Test API Proxy
```bash
curl https://your-cloudfront-url.cloudfront.net/api/v1/health
```

### Test SPA Routing
Open in browser:
- `https://your-cloudfront-url.cloudfront.net/`
- `https://your-cloudfront-url.cloudfront.net/users`
- `https://your-cloudfront-url.cloudfront.net/applications`

All should load the Angular app (not 404).

## 🐛 Common Issues

### Issue: "Frontend directory not found"
**Solution**: Create Angular project first or specify correct path
```bash
ng new frontend
./deploy-frontend.sh dev ./frontend
```

### Issue: "Terraform state file not found"
**Solution**: Deploy infrastructure first
```bash
cd terraform
terraform init
terraform apply
cd ..
```

### Issue: "Old version still showing"
**Solution**: Clear browser cache or wait for CloudFront invalidation
```bash
# Check invalidation status
aws cloudfront list-invalidations --distribution-id $(cd terraform && terraform output -raw frontend_cloudfront_distribution_id)
```

### Issue: "API requests failing"
**Solution**: Ensure API calls use `/api` prefix
```typescript
// ✅ Correct
this.http.get('/api/v1/users')

// ❌ Wrong
this.http.get('http://backend-url/api/v1/users')
```

## 📊 Monitoring

### View Recent Deployments
```bash
# List S3 objects with timestamps
aws s3 ls s3://$(cd terraform && terraform output -raw frontend_s3_bucket_name)/ --recursive
```

### Check CloudFront Cache
```bash
# View CloudFront metrics
aws cloudwatch get-metric-statistics \
    --namespace AWS/CloudFront \
    --metric-name Requests \
    --dimensions Name=DistributionId,Value=$(cd terraform && terraform output -raw frontend_cloudfront_distribution_id) \
    --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
    --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
    --period 300 \
    --statistics Sum
```

## 🔄 Update Deployment

### Deploy New Version
```bash
# Make changes to Angular code
cd frontend
# ... make changes ...
cd ..

# Deploy
./deploy-frontend.sh
```

The script will:
1. Build the new version
2. Upload to S3
3. Invalidate CloudFront cache
4. Wait for invalidation to complete

## 💰 Cost Estimate

### Monthly Costs (Typical Small App)
- **S3 Storage**: ~$0.12/month (5 MB)
- **CloudFront**: ~$1-5/month (1000-10000 requests)
- **Data Transfer**: ~$0.85/month (10 GB)

**Total**: ~$2-6/month for small to medium traffic

### Cost Optimization
- Use PriceClass_100 for regional apps (cheapest)
- Enable compression (reduces data transfer)
- Leverage caching (reduces origin requests)

## 📚 Next Steps

1. **Set up CI/CD**: Automate deployments with GitHub Actions
2. **Add Custom Domain**: Configure Route 53 and ACM certificate
3. **Enable Monitoring**: Set up CloudWatch alarms
4. **Add Security Headers**: Implement Lambda@Edge for security headers
5. **Enable Logging**: Configure CloudFront access logs

## 📖 Full Documentation

For detailed information, see [FRONTEND_DEPLOYMENT.md](./FRONTEND_DEPLOYMENT.md)

## 🆘 Need Help?

1. Check [FRONTEND_DEPLOYMENT.md](./FRONTEND_DEPLOYMENT.md) for detailed troubleshooting
2. Review CloudFront logs in CloudWatch
3. Verify Terraform outputs: `cd terraform && terraform output`
4. Check S3 bucket contents: `aws s3 ls s3://your-bucket-name/`

