# RDS Module - Creates MySQL 8 database in private subnet

# DB Subnet Group
resource "aws_db_subnet_group" "main" {
  name       = "${var.environment}-matriz-usuarios-db-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "${var.environment}-matriz-usuarios-db-subnet-group"
  }
}

# DB Parameter Group for MySQL 8.0
resource "aws_db_parameter_group" "main" {
  name   = "${var.environment}-matriz-usuarios-mysql8-params"
  family = "mysql8.0"

  parameter {
    name  = "character_set_server"
    value = "utf8mb4"
  }

  parameter {
    name  = "collation_server"
    value = "utf8mb4_unicode_ci"
  }

  parameter {
    name  = "max_connections"
    value = "200"
  }

  tags = {
    Name = "${var.environment}-matriz-usuarios-mysql8-params"
  }
}

# RDS MySQL Instance
resource "aws_db_instance" "main" {
  identifier     = "${var.environment}-matriz-usuarios-db"
  engine         = "mysql"
  engine_version = "8.0"

  instance_class    = var.db_instance_class
  allocated_storage = var.db_allocated_storage
  storage_type      = "gp3"
  storage_encrypted = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  # Network configuration
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [var.rds_security_group_id]
  publicly_accessible    = false

  # High availability
  multi_az = var.db_multi_az

  # Backup configuration
  backup_retention_period = var.db_backup_retention_days
  backup_window           = "03:00-04:00"
  maintenance_window      = "mon:04:00-mon:05:00"

  # Parameter and option groups
  parameter_group_name = aws_db_parameter_group.main.name

  # Deletion protection
  deletion_protection       = var.environment == "prod" ? true : false
  skip_final_snapshot       = var.environment != "prod"
  final_snapshot_identifier = var.environment == "prod" ? "${var.environment}-matriz-usuarios-db-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}" : null

  # Performance Insights
  enabled_cloudwatch_logs_exports       = ["error", "general", "slowquery"]
  performance_insights_enabled          = true
  performance_insights_retention_period = 7

  # Auto minor version upgrade
  auto_minor_version_upgrade = true

  tags = {
    Name = "${var.environment}-matriz-usuarios-db"
  }
}
