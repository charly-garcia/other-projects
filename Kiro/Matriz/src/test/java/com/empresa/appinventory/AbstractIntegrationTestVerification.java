package com.empresa.appinventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification test for AbstractIntegrationTest configuration.
 * 
 * This test validates that:
 * - The AbstractIntegrationTest base class is properly configured
 * - Spring Boot context can be loaded with the test configuration
 * - Datasource is properly configured (when Docker is available)
 * - Flyway configuration is present
 * 
 * Note: This test extends AbstractIntegrationTest and will require Docker
 * to be running. If Docker is not available, the test will fail with a
 * clear error message indicating that Testcontainers cannot start.
 */
class AbstractIntegrationTestVerification extends AbstractIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Test
    void verifySpringContextLoads() {
        assertNotNull(environment, "Spring environment should be loaded");
        assertNotNull(dataSource, "DataSource should be configured");
    }

    @Test
    void verifyFlywayIsEnabled() {
        String flywayEnabled = environment.getProperty("spring.flyway.enabled");
        assertEquals("true", flywayEnabled, "Flyway should be enabled for integration tests");
    }

    @Test
    void verifyMySQLContainerIsRunning() {
        assertTrue(mysqlContainer.isRunning(), "MySQL container should be running");
        assertEquals("matriz_usuarios", mysqlContainer.getDatabaseName(), 
                "Database name should be matriz_usuarios");
        assertEquals("test", mysqlContainer.getUsername(), 
                "Username should be test");
    }

    @Test
    void verifyDatasourceConfiguration() {
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        assertNotNull(datasourceUrl, "Datasource URL should be configured");
        assertTrue(datasourceUrl.contains("jdbc:mysql://"), 
                "Datasource URL should point to MySQL");
        assertTrue(datasourceUrl.contains("matriz_usuarios"), 
                "Datasource URL should contain database name");
    }
}
