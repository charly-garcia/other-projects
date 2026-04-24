# Docker Setup for Integration Tests

## Issue
The integration tests require Docker to run Testcontainers with MySQL 8.0. Currently encountering:
```
Could not find a valid Docker environment
```

## Solution

### Option 1: Restart Docker Desktop (Recommended)
1. Close Docker Desktop completely
2. Restart Docker Desktop
3. Wait for Docker to fully start (whale icon should be steady, not animated)
4. Run tests: `mvn test -Dtest=DatabaseConstraintsIntegrationTest`

### Option 2: Enable Docker Daemon Exposure
1. Open Docker Desktop
2. Go to Settings → General
3. Ensure "Expose daemon on tcp://localhost:2375 without TLS" is checked (if available)
4. Apply & Restart
5. Run tests: `mvn test -Dtest=DatabaseConstraintsIntegrationTest`

### Option 3: Use WSL 2 Backend (Windows)
1. Open Docker Desktop
2. Go to Settings → General
3. Enable "Use the WSL 2 based engine"
4. Apply & Restart
5. Run tests: `mvn test -Dtest=DatabaseConstraintsIntegrationTest`

### Option 4: Check Docker Context
```bash
# Check current context
docker context ls

# If needed, switch to default context
docker context use default

# Verify Docker is working
docker ps
```

## Verifying Docker is Ready

Before running tests, verify Docker is working:
```bash
# Should show Docker version
docker --version

# Should list running containers (may be empty)
docker ps

# Should pull and run a test container
docker run hello-world
```

## Running the Tests

Once Docker is working:
```bash
# Run only database constraint tests
mvn test -Dtest=DatabaseConstraintsIntegrationTest

# Run all integration tests
mvn test

# Run with verbose output
mvn test -Dtest=DatabaseConstraintsIntegrationTest -X
```

## Expected Test Output

When Docker is working correctly, you should see:
```
[INFO] Running com.empresa.appinventory.DatabaseConstraintsIntegrationTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
```

All 14 tests should pass:
- 6 UNIQUE constraint tests
- 6 FOREIGN KEY constraint tests
- 2 positive deletion tests

## Testcontainers Behavior

Testcontainers will:
1. Automatically pull MySQL 8.0 image (first run only)
2. Start a MySQL container
3. Apply Flyway migrations
4. Run all tests
5. Stop and remove the container

This ensures tests run against a real MySQL database with actual constraint enforcement.
