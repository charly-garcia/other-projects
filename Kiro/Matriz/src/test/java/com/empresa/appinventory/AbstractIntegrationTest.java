package com.empresa.appinventory;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using Testcontainers MySQL 8.
 * 
 * This abstract class provides:
 * - A shared MySQL 8 container instance for all integration tests
 * - Dynamic datasource configuration pointing to the containerized database
 * - Automatic Flyway migration execution before each test suite
 * 
 * All integration tests should extend this class to ensure consistent
 * test environment setup with a real MySQL database.
 * 
 * Usage:
 * <pre>
 * {@code
 * @SpringBootTest
 * class MyIntegrationTest extends AbstractIntegrationTest {
 *     // Your integration tests here
 * }
 * }
 * </pre>
 * 
 * The MySQL container is shared across all test classes that extend this base class,
 * improving test execution performance by avoiding repeated container startup/shutdown.
 */
@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * Shared MySQL 8 container instance.
     * 
     * The container is configured with:
     * - Database name: matriz_usuarios
     * - Username: test
     * - Password: test
     * 
     * The container is automatically started before tests run and stopped after all tests complete.
     * Flyway migrations are automatically applied when the Spring context loads.
     */
    @Container
    protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("matriz_usuarios")
            .withUsername("test")
            .withPassword("test");

    /**
     * Dynamically configures Spring datasource properties to point to the Testcontainers MySQL instance.
     * 
     * This method is invoked by Spring before the application context is loaded,
     * ensuring that the datasource configuration uses the containerized database
     * instead of the default configuration from application.yml.
     * 
     * @param registry the dynamic property registry to configure datasource properties
     */
    @DynamicPropertySource
    static void configureTestDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        
        // Ensure Flyway is enabled and applies migrations to the test container
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
    }
}
