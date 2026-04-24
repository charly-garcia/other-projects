# Frontend Module Variables

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
}

variable "alb_dns_name" {
  description = "DNS name of the Application Load Balancer for API proxy"
  type        = string
}

variable "certificate_arn" {
  description = "ARN of ACM certificate for CloudFront (optional, uses default CloudFront cert if empty)"
  type        = string
  default     = ""
}

variable "domain_aliases" {
  description = "List of domain aliases for CloudFront distribution (requires certificate_arn)"
  type        = list(string)
  default     = []
}

variable "cloudfront_price_class" {
  description = "CloudFront price class (PriceClass_All, PriceClass_200, PriceClass_100)"
  type        = string
  default     = "PriceClass_100"
}

