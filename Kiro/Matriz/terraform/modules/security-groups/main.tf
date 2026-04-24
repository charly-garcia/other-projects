# Security Groups Module - Creates security groups for ALB, Application, and RDS

# Security Group for Application Load Balancer
resource "aws_security_group" "alb" {
  name        = "${var.environment}-matriz-usuarios-alb-sg"
  description = "Security group for Application Load Balancer - allows HTTPS from internet"
  vpc_id      = var.vpc_id

  tags = {
    Name = "${var.environment}-matriz-usuarios-alb-sg"
  }
}

# ALB Ingress Rule - Allow HTTPS from internet
resource "aws_vpc_security_group_ingress_rule" "alb_https" {
  security_group_id = aws_security_group.alb.id
  description       = "Allow HTTPS traffic from internet"

  cidr_ipv4   = "0.0.0.0/0"
  from_port   = 443
  to_port     = 443
  ip_protocol = "tcp"

  tags = {
    Name = "alb-https-ingress"
  }
}

# ALB Ingress Rule - Allow HTTP from internet (optional, for redirect to HTTPS)
resource "aws_vpc_security_group_ingress_rule" "alb_http" {
  security_group_id = aws_security_group.alb.id
  description       = "Allow HTTP traffic from internet (redirect to HTTPS)"

  cidr_ipv4   = "0.0.0.0/0"
  from_port   = 80
  to_port     = 80
  ip_protocol = "tcp"

  tags = {
    Name = "alb-http-ingress"
  }
}

# ALB Egress Rule - Allow all outbound traffic
resource "aws_vpc_security_group_egress_rule" "alb_egress" {
  security_group_id = aws_security_group.alb.id
  description       = "Allow all outbound traffic"

  cidr_ipv4   = "0.0.0.0/0"
  ip_protocol = "-1"

  tags = {
    Name = "alb-all-egress"
  }
}

# Security Group for Application (EC2/ECS)
resource "aws_security_group" "app" {
  name        = "${var.environment}-matriz-usuarios-app-sg"
  description = "Security group for application instances - allows traffic only from ALB"
  vpc_id      = var.vpc_id

  tags = {
    Name = "${var.environment}-matriz-usuarios-app-sg"
  }
}

# App Ingress Rule - Allow traffic from ALB on application port
resource "aws_vpc_security_group_ingress_rule" "app_from_alb" {
  security_group_id = aws_security_group.app.id
  description       = "Allow traffic from ALB on application port"

  referenced_security_group_id = aws_security_group.alb.id
  from_port                    = 8080
  to_port                      = 8080
  ip_protocol                  = "tcp"

  tags = {
    Name = "app-from-alb-ingress"
  }
}

# App Egress Rule - Allow all outbound traffic
resource "aws_vpc_security_group_egress_rule" "app_egress" {
  security_group_id = aws_security_group.app.id
  description       = "Allow all outbound traffic"

  cidr_ipv4   = "0.0.0.0/0"
  ip_protocol = "-1"

  tags = {
    Name = "app-all-egress"
  }
}

# Security Group for RDS
resource "aws_security_group" "rds" {
  name        = "${var.environment}-matriz-usuarios-rds-sg"
  description = "Security group for RDS MySQL - allows traffic only from application"
  vpc_id      = var.vpc_id

  tags = {
    Name = "${var.environment}-matriz-usuarios-rds-sg"
  }
}

# RDS Ingress Rule - Allow MySQL traffic from application
resource "aws_vpc_security_group_ingress_rule" "rds_from_app" {
  security_group_id = aws_security_group.rds.id
  description       = "Allow MySQL traffic from application instances"

  referenced_security_group_id = aws_security_group.app.id
  from_port                    = 3306
  to_port                      = 3306
  ip_protocol                  = "tcp"

  tags = {
    Name = "rds-from-app-ingress"
  }
}

# RDS Egress Rule - Allow all outbound traffic (for updates, etc.)
resource "aws_vpc_security_group_egress_rule" "rds_egress" {
  security_group_id = aws_security_group.rds.id
  description       = "Allow all outbound traffic"

  cidr_ipv4   = "0.0.0.0/0"
  ip_protocol = "-1"

  tags = {
    Name = "rds-all-egress"
  }
}
