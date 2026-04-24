/**
 * Integration test infrastructure for the App Inventory Management system.
 * 
 * <h2>Overview</h2>
 * This package provides the base infrastructure for integration testing using
 * Testcontainers with MySQL 8. All integration tests should extend
 * {@link com.empresa.appinventory.AbstractIntegrationTest} to ensure consistent
 * test environment setup.
 * 
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link com.empresa.appinventory.AbstractIntegrationTest} - Base class for all integration tests</li>
 *   <li>MySQL 8 container managed by Testcontainers</li>
 *   <li>Automatic Flyway migration execution</li>
 *   <li>Dynamic datasource configuration</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * @SpringBootTest
 * class MyModuleIntegrationTest extends AbstractIntegrationTest {
 *     
 *     @Autowired
 *     private MyService myService;
 *     
 *     @Test
 *     void testMyFeature() {
 *         // Your test code here
 *         // The MySQL container is already running
 *         // Flyway migrations have been applied
 *     }
 * }
 * }</pre>
 * 
 * <h2>Requirements</h2>
 * <ul>
 *   <li>Docker must be running on the test machine</li>
 *   <li>Testcontainers dependencies must be in the classpath</li>
 *   <li>Flyway migrations must be in src/main/resources/db/migration</li>
 * </ul>
 * 
 * <h2>Configuration</h2>
 * The integration test suite automatically configures:
 * <ul>
 *   <li>spring.datasource.url - Points to the Testcontainers MySQL instance</li>
 *   <li>spring.datasource.username - Set to "test"</li>
 *   <li>spring.datasource.password - Set to "test"</li>
 *   <li>spring.flyway.enabled - Set to true</li>
 *   <li>spring.flyway.baseline-on-migrate - Set to true</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * The MySQL container is shared across all test classes that extend
 * {@link com.empresa.appinventory.AbstractIntegrationTest}, which significantly
 * improves test execution performance by avoiding repeated container startup/shutdown.
 * 
 * <h2>Troubleshooting</h2>
 * If tests fail with "Could not find a valid Docker environment":
 * <ol>
 *   <li>Ensure Docker Desktop is running</li>
 *   <li>Verify Docker is accessible from the command line: {@code docker ps}</li>
 *   <li>Check Testcontainers documentation: https://java.testcontainers.org/</li>
 * </ol>
 * 
 * @see com.empresa.appinventory.AbstractIntegrationTest
 * @see org.testcontainers.containers.MySQLContainer
 * @see org.springframework.boot.test.context.SpringBootTest
 */
package com.empresa.appinventory;
