package com.empresa.appinventory.pagination;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import com.empresa.appinventory.module.application.ApplicationRequestDTO;
import com.empresa.appinventory.module.application.ApplicationResponseDTO;
import com.empresa.appinventory.module.application.ApplicationService;
import com.empresa.appinventory.module.area.AreaRequestDTO;
import com.empresa.appinventory.module.area.AreaResponseDTO;
import com.empresa.appinventory.module.area.AreaService;
import com.empresa.appinventory.module.company.CompanyRequestDTO;
import com.empresa.appinventory.module.company.CompanyResponseDTO;
import com.empresa.appinventory.module.company.CompanyService;
import com.empresa.appinventory.module.role.RoleRequestDTO;
import com.empresa.appinventory.module.role.RoleResponseDTO;
import com.empresa.appinventory.module.role.RoleService;
import com.empresa.appinventory.module.supplier.SupplierRequestDTO;
import com.empresa.appinventory.module.supplier.SupplierResponseDTO;
import com.empresa.appinventory.module.supplier.SupplierService;
import com.empresa.appinventory.module.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify pagination implementation across all modules.
 * Tests requirements 8.1, 8.2, and 8.5.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaginationIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserService userService;

    @Test
    void testRolePaginationWithDefaults() {
        // Create test data
        roleService.createRole(new RoleRequestDTO("Admin", "Administrator role"));
        roleService.createRole(new RoleRequestDTO("User", "User role"));

        // Test with default parameters (page=0, size=20)
        PagedResponseDTO<RoleResponseDTO> response = roleService.getRoles(0, 20, null);

        assertNotNull(response);
        assertEquals(2, response.totalElements());
        assertEquals(1, response.totalPages());
        assertEquals(0, response.currentPage());
        assertEquals(20, response.pageSize());
        assertEquals(2, response.content().size());
    }

    @Test
    void testAreaPaginationWithCustomSize() {
        // Create test data
        for (int i = 1; i <= 5; i++) {
            areaService.createArea(new AreaRequestDTO("Area " + i, "Description " + i));
        }

        // Test with custom page size
        PagedResponseDTO<AreaResponseDTO> response = areaService.getAreas(0, 2, null);

        assertNotNull(response);
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages()); // ceil(5/2) = 3
        assertEquals(0, response.currentPage());
        assertEquals(2, response.pageSize());
        assertEquals(2, response.content().size());
    }

    @Test
    void testCompanyPaginationExceedingTotalPages() {
        // Create test data
        companyService.createCompany(new CompanyRequestDTO("Company A", "USA"));
        companyService.createCompany(new CompanyRequestDTO("Company B", "Canada"));

        // Request page that exceeds total pages
        PagedResponseDTO<CompanyResponseDTO> response = companyService.getCompanies(10, 20, null);

        assertNotNull(response);
        assertEquals(2, response.totalElements());
        assertEquals(1, response.totalPages());
        assertEquals(10, response.currentPage());
        assertEquals(20, response.pageSize());
        assertEquals(0, response.content().size()); // Empty list when page exceeds total
    }

    @Test
    void testSupplierPaginationWithSearch() {
        // Create test data
        supplierService.createSupplier(new SupplierRequestDTO("Acme Corp", true));
        supplierService.createSupplier(new SupplierRequestDTO("Beta Inc", false));
        supplierService.createSupplier(new SupplierRequestDTO("Acme Solutions", true));

        // Test pagination with search
        PagedResponseDTO<SupplierResponseDTO> response = supplierService.getSuppliers(0, 20, "Acme");

        assertNotNull(response);
        assertEquals(2, response.totalElements());
        assertEquals(1, response.totalPages());
        assertEquals(0, response.currentPage());
        assertEquals(20, response.pageSize());
        assertEquals(2, response.content().size());
    }

    @Test
    void testApplicationPaginationMultiplePages() {
        // Create role first
        RoleResponseDTO role = roleService.createRole(new RoleRequestDTO("Developer", "Dev role"));

        // Create test data
        for (int i = 1; i <= 25; i++) {
            applicationService.createApplication(new ApplicationRequestDTO(
                    "App " + i,
                    "Owner " + i,
                    "https://app" + i + ".example.com",
                    role.id()
            ));
        }

        // Test first page
        PagedResponseDTO<ApplicationResponseDTO> page1 = applicationService.getApplications(0, 10, null);
        assertEquals(25, page1.totalElements());
        assertEquals(3, page1.totalPages()); // ceil(25/10) = 3
        assertEquals(0, page1.currentPage());
        assertEquals(10, page1.pageSize());
        assertEquals(10, page1.content().size());

        // Test second page
        PagedResponseDTO<ApplicationResponseDTO> page2 = applicationService.getApplications(1, 10, null);
        assertEquals(25, page2.totalElements());
        assertEquals(3, page2.totalPages());
        assertEquals(1, page2.currentPage());
        assertEquals(10, page2.pageSize());
        assertEquals(10, page2.content().size());

        // Test last page
        PagedResponseDTO<ApplicationResponseDTO> page3 = applicationService.getApplications(2, 10, null);
        assertEquals(25, page3.totalElements());
        assertEquals(3, page3.totalPages());
        assertEquals(2, page3.currentPage());
        assertEquals(10, page3.pageSize());
        assertEquals(5, page3.content().size()); // Only 5 items on last page
    }

    @Test
    void testUserPaginationWithAllMetadata() {
        // Create dependencies
        RoleResponseDTO role = roleService.createRole(new RoleRequestDTO("Manager", "Manager role"));
        AreaResponseDTO area = areaService.createArea(new AreaRequestDTO("IT", "IT Department"));
        CompanyResponseDTO company = companyService.createCompany(new CompanyRequestDTO("TechCorp", "USA"));
        SupplierResponseDTO supplier = supplierService.createSupplier(new SupplierRequestDTO("Supplier A", true));
        ApplicationResponseDTO app = applicationService.createApplication(new ApplicationRequestDTO(
                "CRM",
                "Sales Team",
                "https://crm.example.com",
                role.id()
        ));

        // Create test users
        for (int i = 1; i <= 15; i++) {
            userService.createUser(new UserRequestDTO(
                    "User " + i,
                    "user" + i + "@example.com",
                    UserType.Interno,
                    UserStatus.ACTIVO,
                    LocalDate.now(),
                    Scope.General,
                    InformationAccess.Uso_Interno,
                    area.id(),
                    company.id(),
                    supplier.id(),
                    app.id(),
                    role.id(),
                    "Position " + i,
                    "Manager " + i,
                    null
            ));
        }

        // Test pagination
        PagedResponseDTO<UserResponseDTO> response = userService.getUsers(0, 5, null);

        assertNotNull(response);
        assertEquals(15, response.totalElements());
        assertEquals(3, response.totalPages()); // ceil(15/5) = 3
        assertEquals(0, response.currentPage());
        assertEquals(5, response.pageSize());
        assertEquals(5, response.content().size());
    }

    @Test
    void testEmptyResultSetPagination() {
        // Test pagination on empty dataset
        PagedResponseDTO<RoleResponseDTO> response = roleService.getRoles(0, 20, null);

        assertNotNull(response);
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
        assertEquals(0, response.currentPage());
        assertEquals(20, response.pageSize());
        assertEquals(0, response.content().size());
    }

    @Test
    void testPaginationMetadataConsistency() {
        // Create test data
        for (int i = 1; i <= 7; i++) {
            companyService.createCompany(new CompanyRequestDTO("Company " + i, "Country " + i));
        }

        // Test with size=3, should have 3 pages (7/3 = 2.33 -> ceil = 3)
        PagedResponseDTO<CompanyResponseDTO> response = companyService.getCompanies(0, 3, null);

        // Verify mathematical consistency: totalPages = ceil(totalElements / pageSize)
        int expectedTotalPages = (int) Math.ceil((double) response.totalElements() / response.pageSize());
        assertEquals(expectedTotalPages, response.totalPages());

        // Verify content size <= pageSize
        assertTrue(response.content().size() <= response.pageSize());
    }
}
