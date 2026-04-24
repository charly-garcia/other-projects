# Frontend Module - S3 + CloudFront for Angular SPA hosting

# S3 Bucket for static website hosting
resource "aws_s3_bucket" "frontend" {
  bucket = "${var.environment}-matriz-usuarios-frontend"

  tags = {
    Name = "${var.environment}-matriz-usuarios-frontend"
  }
}

# Block public access at bucket level (CloudFront will access via OAC)
resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  block_public_acls       = true
  block_public_policy     = false # Allow CloudFront policy
  ignore_public_acls      = true
  restrict_public_buckets = false # Allow CloudFront access
}

# S3 bucket policy to allow CloudFront OAC access
resource "aws_s3_bucket_policy" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudFrontServicePrincipal"
        Effect = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.frontend.arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.frontend.arn
          }
        }
      }
    ]
  })
}

# CloudFront Origin Access Control (OAC) - modern replacement for OAI
resource "aws_cloudfront_origin_access_control" "frontend" {
  name                              = "${var.environment}-matriz-frontend-oac"
  description                       = "OAC for Matriz de Usuarios frontend S3 bucket"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# CloudFront cache policy for static assets
resource "aws_cloudfront_cache_policy" "static_assets" {
  name        = "${var.environment}-matriz-static-assets-policy"
  comment     = "Cache policy for static assets (JS, CSS, images)"
  default_ttl = 86400    # 1 day
  max_ttl     = 31536000 # 1 year
  min_ttl     = 0

  parameters_in_cache_key_and_forwarded_to_origin {
    cookies_config {
      cookie_behavior = "none"
    }

    headers_config {
      header_behavior = "none"
    }

    query_strings_config {
      query_string_behavior = "none"
    }

    enable_accept_encoding_gzip   = true
    enable_accept_encoding_brotli = true
  }
}

# CloudFront cache policy for SPA (index.html should not be cached)
resource "aws_cloudfront_cache_policy" "spa_no_cache" {
  name        = "${var.environment}-matriz-spa-no-cache-policy"
  comment     = "No cache policy for SPA index.html"
  default_ttl = 0
  max_ttl     = 0
  min_ttl     = 0

  parameters_in_cache_key_and_forwarded_to_origin {
    cookies_config {
      cookie_behavior = "none"
    }

    headers_config {
      header_behavior = "none"
    }

    query_strings_config {
      query_string_behavior = "none"
    }

    enable_accept_encoding_gzip   = true
    enable_accept_encoding_brotli = true
  }
}

# CloudFront origin request policy for API proxy
resource "aws_cloudfront_origin_request_policy" "api_proxy" {
  name    = "${var.environment}-matriz-api-proxy-policy"
  comment = "Origin request policy for API proxy to ALB"

  cookies_config {
    cookie_behavior = "all"
  }

  headers_config {
    header_behavior = "allViewer"
  }

  query_strings_config {
    query_string_behavior = "all"
  }
}

# CloudFront Distribution
resource "aws_cloudfront_distribution" "frontend" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "${var.environment} Matriz de Usuarios Frontend"
  default_root_object = "index.html"
  price_class         = var.cloudfront_price_class
  aliases             = var.domain_aliases

  # S3 Origin for static files
  origin {
    domain_name              = aws_s3_bucket.frontend.bucket_regional_domain_name
    origin_id                = "S3-${aws_s3_bucket.frontend.id}"
    origin_access_control_id = aws_cloudfront_origin_access_control.frontend.id
  }

  # ALB Origin for API requests
  origin {
    domain_name = var.alb_dns_name
    origin_id   = "ALB-Backend"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "https-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  # Default cache behavior (SPA static files)
  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD", "OPTIONS"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "S3-${aws_s3_bucket.frontend.id}"
    cache_policy_id        = aws_cloudfront_cache_policy.static_assets.id
    viewer_protocol_policy = "redirect-to-https"
    compress               = true
  }

  # Cache behavior for API requests - proxy to ALB
  ordered_cache_behavior {
    path_pattern           = "/api/*"
    allowed_methods        = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "ALB-Backend"
    viewer_protocol_policy = "redirect-to-https"
    compress               = true

    # Disable caching for API requests
    cache_policy_id          = aws_cloudfront_cache_policy.spa_no_cache.id
    origin_request_policy_id = aws_cloudfront_origin_request_policy.api_proxy.id
  }

  # Custom error response for SPA routing (404/403 -> index.html)
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

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  # SSL/TLS Certificate
  viewer_certificate {
    cloudfront_default_certificate = var.certificate_arn == "" ? true : false
    acm_certificate_arn            = var.certificate_arn != "" ? var.certificate_arn : null
    ssl_support_method             = var.certificate_arn != "" ? "sni-only" : null
    minimum_protocol_version       = "TLSv1.2_2021"
  }

  tags = {
    Name = "${var.environment}-matriz-usuarios-cdn"
  }
}

