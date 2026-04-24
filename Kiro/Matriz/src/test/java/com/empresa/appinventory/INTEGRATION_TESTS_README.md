# Integration Tests with Testcontainers

## Overview

This project uses **Testcontainers** to provide a real MySQL 8 database for integration testing. This approach ensures that integration tests run against the same database engine used in production, providing higher confidence in test results compared to using in-memory databases like H2.

## Architecture

### Base Class: `AbstractIntegrationTest`

All integration tests should extend `AbstractIntegrationTest`, which provides:

- **Shared MySQL 8 Container**: A single MySQL container instance shared across all test classes
- **Dynamic Datasource Configuration**: Automatic configuration of Spring datasource properties to point to the containerized database
- **Automatic Flyway Migrations**: Database schema is automatically created and migrated before tests run
- **Spring Boot Test Context**: Full Spring application context with all beans available

### Key Features

1. **Container Reuse**: The MySQL container is started once and shared across all test classes, significantly improving test execution performance
2. **Isolation**: Each test can use `@Transactional` to ensure database changes are rolled back after the test
3. **Real Database**: Tests run against MySQL 8, matching the production environment
4. **Automatic Cleanup**: Testcontainers automatically stops and removes the container after all tests complete

## Prerequisites

### Required Software

- **Docker Desktop** (or Docker Engine on Linux)
  - Windows: [Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/)
  - macOS: [Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/)
  - Linux: [Docker Engine](https://docs.docker.com/engine/install/)

- **Java 17+**
- **Maven 3.6+**

### Verify Docker Installation

Before running integration tests, ensure Docker is running:

```bash
docker ps
```

This command should execute without errors. If you see "Cannot connect to the Docker daemon", start Docker Desktop.

## Usage

### Creating an Integration Test

```java
package com.empresa.appinventory.module.mymodule;

import com.empresa.appinventory.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // Optional: rolls back changes after each test
class MyModuleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MyService myService;

    @Test
    void testMyFeature() {
        // Arrange
        var input = new MyDTO("test data");
        
        // Act
        var result = myService.processData(input);
        
        // Assert
        assertNotNull(result);
        assertEquals("expected", result.getValue());
    }
}
```

### Running Integration Tests

#### Run all tests (including integration tests)

```bash
mvn test
```

#### Run only integration tests

```bash
mvn test -Dtest="*IntegrationTest"
```

#### Run a specific integration test

```bash
mvn test -Dtest=MyModuleIntegrationTest
```

#### Skip integration tests (for faster builds)

```bash
mvn test -DskipITs
```

## Configuration

### Container Configuration

The MySQL container is configured in `AbstractIntegrationTest`:

```java
@Container
protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("matriz_usuarios")
        .withUsername("test")
        .withPassword("test");
```

### Datasource Configuration

The datasource is dynamically configured to point to the Testcontainers MySQL instance:

```java
@DynamicPropertySource
static void configureTestDatasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
    registry.add("spring.datasource.username", mysqlContainer::getUsername);
    registry.add("spring.datasource.password", mysqlContainer::getPassword);
    registry.add("spring.flyway.enabled", () -> true);
    registry.add("spring.flyway.baseline-on-migrate", () -> true);
}
```

### Flyway Migrations

Flyway automatically applies all migrations from `src/main/resources/db/migration` when the Spring context loads. This ensures the database schema is up-to-date before tests run.

## Best Practices

### 1. Use `@Transactional` for Test Isolation

```java
@SpringBootTest
@Transactional
class MyIntegrationTest extends AbstractIntegrationTest {
    // Tests will be rolled back automatically
}
```

### 2. Test Real Business Scenarios

Integration tests should verify:
- Database constraints (foreign keys, unique constraints)
- Transaction behavior
- Complex queries and joins
- Flyway migration correctness

### 3. Keep Tests Fast

- Minimize data setup in each test
- Use `@BeforeEach` for common setup
- Consider using test data builders

### 4. Test Error Scenarios

```java
@Test
void testDuplicateNameThrowsException() {
    // Create first entity
    service.create(new DTO("name"));
    
    // Attempt to create duplicate should fail
    assertThrows(DuplicateResourceException.class, () -> {
        service.create(new DTO("name"));
    });
}
```

## Troubleshooting

### Problem: "Could not find a valid Docker environment"

**Solution**: Ensure Docker Desktop is running. On Windows, check the system tray for the Docker icon.

### Problem: Tests are slow

**Possible causes**:
1. Container is being recreated for each test class (check that you're extending `AbstractIntegrationTest`)
2. Too much data setup in tests
3. Missing database indexes

**Solutions**:
- Verify all integration tests extend `AbstractIntegrationTest`
- Profile slow tests and optimize data setup
- Review database schema and add appropriate indexes

### Problem: Port conflicts

**Solution**: Testcontainers automatically assigns random available ports, so port conflicts should not occur. If you see port-related errors, ensure no other MySQL instances are interfering.

### Problem: Container fails to start

**Possible causes**:
1. Insufficient Docker resources
2. Network issues preventing image download
3. Docker daemon not running

**Solutions**:
- Increase Docker Desktop memory/CPU allocation in settings
- Check internet connectivity
- Restart Docker Desktop

## Performance Metrics

Typical performance characteristics:

- **First test class**: ~10-15 seconds (includes container startup + Flyway migrations)
- **Subsequent test classes**: ~2-3 seconds (container is reused)
- **Individual test**: <1 second (depends on test complexity)

## References

- [Testcontainers Documentation](https://java.testcontainers.org/)
- [Testcontainers MySQL Module](https://java.testcontainers.org/modules/databases/mysql/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Flyway Documentation](https://flywaydb.org/documentation/)

## Task Information

**Feature**: app-inventory-management  
**Task**: 12.1 - Configurar suite de pruebas de integración con Testcontainers MySQL  
**Requirements**: 1.3, 10.1, 10.2, 10.3, 10.4, 10.5, 10.6

This integration test infrastructure validates:
- MySQL 8 connectivity (Requirement 1.3)
- Database schema integrity (Requirements 10.1, 10.2, 10.3, 10.4)
- Unique constraints (Requirement 10.5)
- Foreign key constraints (Requirement 10.6)
