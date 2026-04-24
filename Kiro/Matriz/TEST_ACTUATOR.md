# Testing Spring Boot Actuator Health Endpoint

## Overview

This document provides instructions for testing the Spring Boot Actuator health endpoint that will be used by the ALB health checks.

## Prerequisites

- Application running locally or deployed to AWS
- Access to the application endpoint

## Test Scenarios

### 1. Basic Health Check

Test the main health endpoint:

```bash
curl -v http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

**HTTP Status Code:** 200 OK

### 2. Detailed Health Check

Test with detailed information (requires authorization in production):

```bash
curl -v http://localhost:8080/actuator/health?showDetails=true
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 123456789012,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### 3. Liveness Probe

Test the Kubernetes-style liveness probe:

```bash
curl -v http://localhost:8080/actuator/health/liveness
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

### 4. Readiness Probe

Test the Kubernetes-style readiness probe:

```bash
curl -v http://localhost:8080/actuator/health/readiness
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

### 5. Test with Database Down

Simulate database failure to verify health check detects it:

1. Stop the MySQL database
2. Call the health endpoint:

```bash
curl -v http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "DOWN"
}
```

**HTTP Status Code:** 503 Service Unavailable

### 6. Test from Docker Container

If running in Docker:

```bash
# From host machine
curl http://localhost:8080/actuator/health

# From inside container
docker exec app-inventory-backend wget -qO- http://localhost:8080/actuator/health
```

### 7. Test ALB Health Check Simulation

Simulate what the ALB will do:

```bash
# Test with wget (same as Docker health check)
wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health
echo $?  # Should return 0 for success

# Test with curl and check HTTP status
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
if [ $HTTP_STATUS -eq 200 ]; then
  echo "Health check passed"
else
  echo "Health check failed with status: $HTTP_STATUS"
fi
```

## Testing in AWS

### ECS Container Health Check

View container health status:

```bash
aws ecs describe-tasks \
  --cluster app-inventory-cluster \
  --tasks <TASK_ARN> \
  --query 'tasks[0].containers[0].healthStatus' \
  --region us-east-1
```

**Expected Output:** `"HEALTHY"`

### ALB Target Health

Check target health in the ALB target group:

```bash
aws elbv2 describe-target-health \
  --target-group-arn <TARGET_GROUP_ARN> \
  --region us-east-1
```

**Expected Output:**
```json
{
  "TargetHealthDescriptions": [
    {
      "Target": {
        "Id": "10.0.1.100",
        "Port": 8080
      },
      "HealthCheckPort": "8080",
      "TargetHealth": {
        "State": "healthy"
      }
    }
  ]
}
```

### CloudWatch Logs

Check application logs for health check requests:

```bash
# ECS
aws logs tail /ecs/app-inventory-management --follow --filter-pattern "actuator/health"

# EC2
sudo journalctl -u app-inventory-management -f | grep "actuator/health"
```

## Troubleshooting

### Health Check Returns 404

**Cause:** Actuator endpoints not enabled or incorrect path

**Solution:**
1. Verify `spring-boot-starter-actuator` dependency is in `pom.xml`
2. Check `management.endpoints.web.exposure.include` in `application.yml`
3. Verify base path: `management.endpoints.web.base-path=/actuator`

### Health Check Returns 503

**Cause:** Database connection failure or other component down

**Solution:**
1. Check database connectivity:
   ```bash
   mysql -h <DB_HOST> -P <DB_PORT> -u <DB_USER> -p<DB_PASSWORD> <DB_NAME>
   ```
2. Verify security group allows traffic from ECS/EC2 to RDS
3. Check application logs for connection errors

### Health Check Times Out

**Cause:** Application not responding or network issue

**Solution:**
1. Verify application is running:
   ```bash
   # ECS
   aws ecs list-tasks --cluster app-inventory-cluster --service-name app-inventory-backend-service
   
   # EC2
   sudo systemctl status app-inventory-management
   ```
2. Check security group allows inbound traffic on port 8080
3. Verify ALB can reach the target (check route tables, NACLs)

### Health Check Flapping

**Cause:** Intermittent database connections or resource constraints

**Solution:**
1. Increase health check interval and timeout in ALB target group
2. Check database connection pool settings in `application-production.yml`
3. Monitor CPU and memory usage
4. Review application logs for errors

## Performance Testing

### Load Test Health Endpoint

Test health endpoint under load:

```bash
# Using Apache Bench
ab -n 1000 -c 10 http://localhost:8080/actuator/health

# Using wrk
wrk -t4 -c100 -d30s http://localhost:8080/actuator/health
```

**Expected:** Health endpoint should respond quickly (<100ms) even under load

### Monitor Health Check Impact

Check if health checks impact application performance:

```bash
# View health check request rate in logs
aws logs filter-log-events \
  --log-group-name /ecs/app-inventory-management \
  --filter-pattern "actuator/health" \
  --start-time $(date -d '1 hour ago' +%s)000 \
  --region us-east-1 | jq '.events | length'
```

## Automated Testing

### Integration Test

Create an integration test to verify health endpoint:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorHealthEndpointTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpointShouldReturnUp() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health",
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
    }

    @Test
    void livenessEndpointShouldReturnUp() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health/liveness",
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
    }

    @Test
    void readinessEndpointShouldReturnUp() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health/readiness",
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
    }
}
```

### Smoke Test Script

```bash
#!/bin/bash

# Smoke test for deployed application
ENDPOINT="${1:-http://localhost:8080}"

echo "Testing health endpoint at: ${ENDPOINT}/actuator/health"

# Test health endpoint
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" ${ENDPOINT}/actuator/health)

if [ $HTTP_STATUS -eq 200 ]; then
  echo "✓ Health check passed (HTTP $HTTP_STATUS)"
  
  # Get health status
  HEALTH_STATUS=$(curl -s ${ENDPOINT}/actuator/health | jq -r '.status')
  
  if [ "$HEALTH_STATUS" = "UP" ]; then
    echo "✓ Application status: UP"
    exit 0
  else
    echo "✗ Application status: $HEALTH_STATUS"
    exit 1
  fi
else
  echo "✗ Health check failed (HTTP $HTTP_STATUS)"
  exit 1
fi
```

## Checklist

Before deploying to production, verify:

- [ ] Health endpoint returns HTTP 200 when application is healthy
- [ ] Health endpoint returns HTTP 503 when database is down
- [ ] Liveness probe endpoint is accessible
- [ ] Readiness probe endpoint is accessible
- [ ] Health check responds within 5 seconds
- [ ] ALB target group health check is configured correctly
- [ ] ECS container health check is configured (if using ECS)
- [ ] CloudWatch alarms are set up for unhealthy targets
- [ ] Health check logs are not flooding application logs
- [ ] Database connection pool is properly configured
- [ ] Security groups allow health check traffic

## References

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [AWS ALB Health Checks](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/target-group-health-checks.html)
- [ECS Container Health Checks](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_definition_parameters.html#container_definition_healthcheck)
