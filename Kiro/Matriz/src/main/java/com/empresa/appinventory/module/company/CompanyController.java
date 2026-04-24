package com.empresa.appinventory.module.company;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Company management.
 * Exposes CRUD endpoints for the Company catalog.
 */
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Get paginated list of companies with optional search.
     *
     * @param page the page number (0-indexed, default 0)
     * @param size the page size (default 20)
     * @param search optional search term for name filtering
     * @return paginated response with companies and HTTP 200
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<CompanyResponseDTO>> getCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        PagedResponseDTO<CompanyResponseDTO> response = companyService.getCompanies(page, size, search);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a company by ID.
     *
     * @param id the company ID
     * @return the company and HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> getCompanyById(@PathVariable Long id) {
        CompanyResponseDTO response = companyService.getCompanyById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new company.
     *
     * @param requestDTO the company data
     * @return the created company and HTTP 201
     */
    @PostMapping
    public ResponseEntity<CompanyResponseDTO> createCompany(@Valid @RequestBody CompanyRequestDTO requestDTO) {
        CompanyResponseDTO response = companyService.createCompany(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing company.
     *
     * @param id the company ID
     * @param requestDTO the updated company data
     * @return the updated company and HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequestDTO requestDTO
    ) {
        CompanyResponseDTO response = companyService.updateCompany(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a company by ID.
     *
     * @param id the company ID
     * @return HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
