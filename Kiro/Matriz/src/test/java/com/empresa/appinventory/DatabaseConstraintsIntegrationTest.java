package com.empresa.appinventory;

import com.empresa.appinventory.module.application.ApplicationEntity;
import com.empresa.appinventory.module.application.ApplicationRepository;
import com.empresa.appinventory.module.area.AreaEntity;
import com.empresa.appinventory.module.area.AreaRepository;
import com.empresa.appinventory.module.company.CompanyEntity;
import com.empresa.appinventory.module.company.CompanyRepository;
import com.empresa.appinventory.module.role.RoleEntity;
import com.empresa.appinventory.module.role.RoleRepository;
import com.empresa.appinventory.module.supplier.SupplierEntity;
import com.empresa.appinventory.module.supplier.SupplierRepository;
import com.empresa.appinventory.module.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for database constraints.
 * 
 * Feature: app-inventory-management
 * Task: 12.2 - Escribir pruebas de integracion para restricciones de DB
 * 
 * These tests verify that database-level constraints are properly enforced:
 * - UNIQUE constraints on name fields reject duplicates
 * - UNIQUE constraint on user email rejects duplicates
 * - FOREIGN KEY constraints prevent deletion of referenced entities
 * 
 * Tests use Testcontainers MySQL to verify actual database behavior.
 * 
 * Validates Requirements: 10.1, 10.2, 10.5, 10.6
 */
@Transactional
class DatabaseConstraintsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private UserRepository userRepository;

    // ========== UNIQUE Constraint Tests - Name Fields ==========

    /**
     * Validates Requirement 10.5: UNIQUE constraint on roles.name
     * 
     * Verifies that the database rejects duplicate role names at the DB level.
     */
    @Test
    void databaseShouldRejectDuplicateRoleName() {
        // Given: A role with a specific name exists in the database
        RoleEntity role1 = new RoleEntity("Admin", "Administrator role");
        roleRepository.saveAndFlush(role1);

        // When: Attempting to insert another role with the same name
        RoleEntity role2 = new RoleEntity("Admin", "Different description");

        // Then: Database should throw DataIntegrityViolationException due to UNIQUE constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            roleRepository.saveAndFlush(role2);
        }, "Database should reject duplicate role name via UNIQUE constraint");
    }

    /**
     * Validates Requirement 10.5: UNIQUE constraint on areas.name
     * 
     * Verifies that the database rejects duplicate area names at the DB level.
     */
    @Test
    void databaseShouldRejectDuplicateAreaName() {
        // Given: An area with a specific name exists in the database
        AreaEntity area1 = new AreaEntity("IT Department", "Information Technology");
        areaRepository.saveAndFlush(area1);

        // When: Attempting to insert another area with the same name
        AreaEntity area2 = new AreaEntity("IT Department", "Different description");

        // Then: Database should throw DataIntegrityViolationException due to UNIQUE constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            areaRepository.saveAndFlush(area2);
        }, "Database should reject duplicate area name via UNIQUE constraint");
    }

    /**
     * Validates Requirement 10.5: UNIQUE constraint on companies.name
     * 
     * Verifies that the database rejects duplicate company names at the DB level.
     */
    @Test
    void databaseShouldRejectDuplicateCompanyName() {
        // Given: A company with a specific name exists in the database
        CompanyEntity company1 = new CompanyEntity("Acme Corp", "USA");
        companyRepository.saveAndFlush(company1);

        // When: Attempting to insert another company with the same name
        CompanyEntity company2 = new CompanyEntity("Acme Corp", "Canada");

        // Then: Database should throw DataIntegrityViolationException due to UNIQUE constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            companyRepository.saveAndFlush(company2);
        }, "Database should reject duplicate company name via UNIQUE constraint");
    }

    /**
     * Validates Requirement 10.5: UNIQUE constraint on suppliers.name
     * 
     * Verifies that the database rejects duplicate supplier names at the DB level.
     */
    @Test
    void databaseShouldRejectDuplicateSupplierName() {
        // Given: A supplier with a specific name exists in the database
        SupplierEntity supplier1 = new SupplierEntity("Tech Supplies Inc", true);
        supplierRepository.saveAndFlush(supplier1);

        // When: Attempting to insert another supplier with the same name
        SupplierEntity supplier2 = new SupplierEntity("Tech Supplies Inc", false);

        // Then: Database should throw DataIntegrityViolationException due to UNIQUE constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            supplierRepository.saveAndFlush(supplier2);
        }, "Database should reject duplicate supplier name via UNIQUE constraint");
    }

    /**
     * Validates Requirement 10.5: UNIQUE constraint on applications.name
     * 
     * Verifies that the database rejects duplicate application names at the DB level.
     */
    @Test
    void databaseShouldRejectDuplicateApplicationName() {
        // Given: A role and an application with a specific name exist in the database
        RoleEntity role = new RoleEntity("User", "Standard user role");
        roleRepository.saveAndFlush(role);

        ApplicationEntity app1 = new ApplicationEntity("CRM System", "John Doe", "https://crm.example.com", role);
        applicationRepository.saveAndFlush(app1);

        // When: Attempting to insert another application with the same name
        ApplicationEntity app2 = new ApplicationEntity("CRM System", "Jane Smith", "https://crm2.example.com", role);

        // Then: Database should throw DataIntegrityViolationException due to UNIQUE constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            applicationRepository.saveAndFlush(app2);
        }, "Database should reject duplicate application name via UNIQUE constraint");
    }

    // ========== UNIQUE Constraint Tests - User Email ==========

    /**
     * Validates Requirement 10.6: UNIQUE constraint on users.email
     * 
     * Verifies that the database rejects duplicate user emails at the DB level.
     */
    @Test
    void databaseShouldRejectDuplicateUserEmail() {
        // Given: A user with a specific email exists in the database
        UserEntity user1 = new UserEntity(
            "John Doe",
            "john.doe@example.com",
            UserType.Interno,
            UserStatus.ACTIVO,
            LocalDate.of(2024, 1, 1),
            Scope.General,
            InformationAccess.Uso_Interno
        );
        userRepository.saveAndFlush(user1);

        // When: Attempting to insert another user with the same email
        UserEntity user2 = new UserEntity(
            "Jane Doe",
            "john.doe@example.com",
            UserType.Practicante,
            UserStatus.ACTIVO,
            LocalDate.of(2024, 2, 1),
            Scope.PCI,
            InformationAccess.Confidencial
        );

        // Then: Database should throw DataIntegrityViolationException due to UNIQUE constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(user2);
        }, "Database should reject duplicate user email via UNIQUE constraint");
    }

    // ========== FOREIGN KEY Constraint Tests - Prevent Deletion ==========

    /**
     * Validates Requirement 10.2: FOREIGN KEY constraint fk_applications_role
     * 
     * Verifies that the database prevents deletion of a role that is referenced
     * by at least one application.
     */
    @Test
    void databaseShouldPreventDeletionOfRoleReferencedByApplication() {
        // Given: A role that is referenced by an application
        RoleEntity role = new RoleEntity("Manager", "Manager role");
        roleRepository.saveAndFlush(role);

        ApplicationEntity app = new ApplicationEntity("HR System", "HR Team", "https://hr.example.com", role);
        applicationRepository.saveAndFlush(app);

        // When: Attempting to delete the role
        // Then: Database should throw DataIntegrityViolationException due to FOREIGN KEY constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            roleRepository.delete(role);
            roleRepository.flush();
        }, "Database should prevent deletion of role referenced by application via FOREIGN KEY constraint");

        // Verify the role still exists
        assertTrue(roleRepository.existsById(role.getId()), "Role should still exist after failed deletion");
    }

    /**
     * Validates Requirement 10.2: FOREIGN KEY constraint fk_users_area
     * 
     * Verifies that the database prevents deletion of an area that is referenced
     * by at least one user.
     */
    @Test
    void databaseShouldPreventDeletionOfAreaReferencedByUser() {
        // Given: An area that is referenced by a user
        AreaEntity area = new AreaEntity("Finance", "Finance department");
        areaRepository.saveAndFlush(area);

        UserEntity user = new UserEntity(
            "Alice Smith",
            "alice.smith@example.com",
            UserType.Interno,
            UserStatus.ACTIVO,
            LocalDate.of(2024, 1, 15),
            Scope.ISO,
            InformationAccess.Confidencial
        );
        user.setArea(area);
        userRepository.saveAndFlush(user);

        // When: Attempting to delete the area
        // Then: Database should throw DataIntegrityViolationException due to FOREIGN KEY constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            areaRepository.delete(area);
            areaRepository.flush();
        }, "Database should prevent deletion of area referenced by user via FOREIGN KEY constraint");

        // Verify the area still exists
        assertTrue(areaRepository.existsById(area.getId()), "Area should still exist after failed deletion");
    }

    /**
     * Validates Requirement 10.2: FOREIGN KEY constraint fk_users_company
     * 
     * Verifies that the database prevents deletion of a company that is referenced
     * by at least one user.
     */
    @Test
    void databaseShouldPreventDeletionOfCompanyReferencedByUser() {
        // Given: A company that is referenced by a user
        CompanyEntity company = new CompanyEntity("Global Tech", "Germany");
        companyRepository.saveAndFlush(company);

        UserEntity user = new UserEntity(
            "Bob Johnson",
            "bob.johnson@example.com",
            UserType.Contractor,
            UserStatus.ACTIVO,
            LocalDate.of(2024, 3, 1),
            Scope.General,
            InformationAccess.Uso_Interno
        );
        user.setCompany(company);
        userRepository.saveAndFlush(user);

        // When: Attempting to delete the company
        // Then: Database should throw DataIntegrityViolationException due to FOREIGN KEY constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            companyRepository.delete(company);
            companyRepository.flush();
        }, "Database should prevent deletion of company referenced by user via FOREIGN KEY constraint");

        // Verify the company still exists
        assertTrue(companyRepository.existsById(company.getId()), "Company should still exist after failed deletion");
    }

    /**
     * Validates Requirement 10.2: FOREIGN KEY constraint fk_users_supplier
     * 
     * Verifies that the database prevents deletion of a supplier that is referenced
     * by at least one user.
     */
    @Test
    void databaseShouldPreventDeletionOfSupplierReferencedByUser() {
        // Given: A supplier that is referenced by a user
        SupplierEntity supplier = new SupplierEntity("Consulting Partners", true);
        supplierRepository.saveAndFlush(supplier);

        UserEntity user = new UserEntity(
            "Carol White",
            "carol.white@example.com",
            UserType.Contractor,
            UserStatus.ACTIVO,
            LocalDate.of(2024, 4, 1),
            Scope.PCI,
            InformationAccess.Secreta
        );
        user.setSupplier(supplier);
        userRepository.saveAndFlush(user);

        // When: Attempting to delete the supplier
        // Then: Database should throw DataIntegrityViolationException due to FOREIGN KEY constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            supplierRepository.delete(supplier);
            supplierRepository.flush();
        }, "Database should prevent deletion of supplier referenced by user via FOREIGN KEY constraint");

        // Verify the supplier still exists
        assertTrue(supplierRepository.existsById(supplier.getId()), "Supplier should still exist after failed deletion");
    }

    /**
     * Validates Requirement 10.3: FOREIGN KEY constraint fk_users_application
     * 
     * Verifies that the database prevents deletion of an application that is referenced
     * by at least one user.
     */
    @Test
    void databaseShouldPreventDeletionOfApplicationReferencedByUser() {
        // Given: An application that is referenced by a user
        RoleEntity role = new RoleEntity("Viewer", "Read-only access");
        roleRepository.saveAndFlush(role);

        ApplicationEntity app = new ApplicationEntity("Analytics Dashboard", "Data Team", "https://analytics.example.com", role);
        applicationRepository.saveAndFlush(app);

        UserEntity user = new UserEntity(
            "David Brown",
            "david.brown@example.com",
            UserType.Interno,
            UserStatus.ACTIVO,
            LocalDate.of(2024, 5, 1),
            Scope.General,
            InformationAccess.Uso_Interno
        );
        user.setApplication(app);
        user.setRole(role);
        userRepository.saveAndFlush(user);

        // When: Attempting to delete the application
        // Then: Database should throw DataIntegrityViolationException due to FOREIGN KEY constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            applicationRepository.delete(app);
            applicationRepository.flush();
        }, "Database should prevent deletion of application referenced by user via FOREIGN KEY constraint");

        // Verify the application still exists
        assertTrue(applicationRepository.existsById(app.getId()), "Application should still exist after failed deletion");
    }

    /**
     * Validates Requirement 10.2: FOREIGN KEY constraint fk_users_role
     * 
     * Verifies that the database prevents deletion of a role that is referenced
     * by at least one user.
     */
    @Test
    void databaseShouldPreventDeletionOfRoleReferencedByUser() {
        // Given: A role that is referenced by a user
        RoleEntity role = new RoleEntity("Editor", "Content editor role");
        roleRepository.saveAndFlush(role);

        UserEntity user = new UserEntity(
            "Emma Davis",
            "emma.davis@example.com",
            UserType.Practicante,
            UserStatus.ACTIVO,
            LocalDate.of(2024, 6, 1),
            Scope.ISO,
            InformationAccess.Confidencial
        );
        user.setRole(role);
        userRepository.saveAndFlush(user);

        // When: Attempting to delete the role
        // Then: Database should throw DataIntegrityViolationException due to FOREIGN KEY constraint
        assertThrows(DataIntegrityViolationException.class, () -> {
            roleRepository.delete(role);
            roleRepository.flush();
        }, "Database should prevent deletion of role referenced by user via FOREIGN KEY constraint");

        // Verify the role still exists
        assertTrue(roleRepository.existsById(role.getId()), "Role should still exist after failed deletion");
    }

    // ========== Positive Tests - Successful Deletion When Not Referenced ==========

    /**
     * Validates that deletion succeeds when entity is not referenced.
     * 
     * This test ensures that FOREIGN KEY constraints only prevent deletion
     * when references exist, not in all cases.
     */
    @Test
    void databaseShouldAllowDeletionOfUnreferencedRole() {
        // Given: A role that is not referenced by any entity
        RoleEntity role = new RoleEntity("Unused Role", "This role is not used");
        roleRepository.saveAndFlush(role);
        Long roleId = role.getId();

        // When: Deleting the unreferenced role
        roleRepository.delete(role);
        roleRepository.flush();

        // Then: Deletion should succeed
        assertFalse(roleRepository.existsById(roleId), "Unreferenced role should be deleted successfully");
    }

    /**
     * Validates that deletion succeeds when entity is not referenced.
     * 
     * This test ensures that FOREIGN KEY constraints only prevent deletion
     * when references exist, not in all cases.
     */
    @Test
    void databaseShouldAllowDeletionOfUnreferencedArea() {
        // Given: An area that is not referenced by any user
        AreaEntity area = new AreaEntity("Unused Area", "This area is not used");
        areaRepository.saveAndFlush(area);
        Long areaId = area.getId();

        // When: Deleting the unreferenced area
        areaRepository.delete(area);
        areaRepository.flush();

        // Then: Deletion should succeed
        assertFalse(areaRepository.existsById(areaId), "Unreferenced area should be deleted successfully");
    }
}
