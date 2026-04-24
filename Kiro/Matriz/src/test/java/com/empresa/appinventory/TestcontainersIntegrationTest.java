package com.empresa.appinventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test to verify that the Spring Boot application context
 * starts successfully with Testcontainers MySQL 8.
 * 
 * This test validates:
 * - MySQL container starts correctly
 * - Spring context loads with the containerized database
 * - Flyway migrations apply successfully
 * 
 * This test now extends AbstractIntegrationTest to use the shared
 * Testcontainers infrastructure.
 */
class TestcontainersIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoadsWithTestcontainersMySQL() {
        // Verify that the MySQL container is running
        assertTrue(mysqlContainer.isRunning(), "MySQL container should be running");
        
        // If we reach this point, the Spring context has loaded successfully
        // with Testcontainers MySQL and Flyway migrations have been applied
    }
}
