package com.empresa.appinventory.module.company;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import com.empresa.appinventory.exception.DuplicateResourceException;
import com.empresa.appinventory.exception.ReferentialIntegrityException;
import com.empresa.appinventory.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for Company business logic.
 * Handles CRUD operations with validation and referential integrity checks.
 */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Create a new company.
     * Validates name uniqueness before persisting.
     *
     * @param requestDTO the company data
     * @return the created company
     * @throws DuplicateResourceException if a company with the same name already exists
     */
    @Transactional
    public CompanyResponseDTO createCompany(CompanyRequestDTO requestDTO) {
        // Check for duplicate name (case-insensitive)
        if (companyRepository.existsByNameIgnoreCase(requestDTO.name())) {
            throw new DuplicateResourceException("El nombre de la compañía ya existe");
        }

        // Create and persist entity
        CompanyEntity entity = new CompanyEntity(requestDTO.name(), requestDTO.country());
        CompanyEntity savedEntity = companyRepository.save(entity);

        return CompanyResponseDTO.fromEntity(savedEntity);
    }

    /**
     * Get paginated list of companies with optional name search.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param search optional search term for name filtering (case-insensitive, partial match)
     * @return paginated response with companies
     */
    @Transactional(readOnly = true)
    public PagedResponseDTO<CompanyResponseDTO> getCompanies(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CompanyEntity> companyPage;

        if (search != null && !search.trim().isEmpty()) {
            companyPage = companyRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            companyPage = companyRepository.findAll(pageable);
        }

        return new PagedResponseDTO<>(
                companyPage.getContent().stream()
                        .map(CompanyResponseDTO::fromEntity)
                        .toList(),
                companyPage.getTotalElements(),
                companyPage.getTotalPages(),
                companyPage.getNumber(),
                companyPage.getSize()
        );
    }

    /**
     * Get a company by ID.
     *
     * @param id the company ID
     * @return the company
     * @throws ResourceNotFoundException if the company does not exist
     */
    @Transactional(readOnly = true)
    public CompanyResponseDTO getCompanyById(Long id) {
        CompanyEntity entity = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada con ID: " + id));

        return CompanyResponseDTO.fromEntity(entity);
    }

    /**
     * Update an existing company.
     * Validates existence and name uniqueness.
     *
     * @param id the company ID
     * @param requestDTO the updated company data
     * @return the updated company
     * @throws ResourceNotFoundException if the company does not exist
     * @throws DuplicateResourceException if the new name conflicts with another company
     */
    @Transactional
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO requestDTO) {
        // Check if company exists
        CompanyEntity entity = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada con ID: " + id));

        // Check for duplicate name (case-insensitive), excluding current company
        if (!entity.getName().equalsIgnoreCase(requestDTO.name()) &&
                companyRepository.existsByNameIgnoreCase(requestDTO.name())) {
            throw new DuplicateResourceException("El nombre de la compañía ya existe");
        }

        // Update entity
        entity.setName(requestDTO.name());
        entity.setCountry(requestDTO.country());
        CompanyEntity updatedEntity = companyRepository.save(entity);

        return CompanyResponseDTO.fromEntity(updatedEntity);
    }

    /**
     * Delete a company by ID.
     * Validates that the company is not referenced by any user.
     *
     * @param id the company ID
     * @throws ResourceNotFoundException if the company does not exist
     * @throws ReferentialIntegrityException if the company is referenced by users
     */
    @Transactional
    public void deleteCompany(Long id) {
        // Check if company exists
        CompanyEntity entity = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada con ID: " + id));

        // Check if company is referenced by any user
        // Note: This check will be implemented when UserRepository is available.
        // For now, we rely on database foreign key constraints which will throw
        // DataIntegrityViolationException if the company is in use.
        // The GlobalExceptionHandler should catch this and convert it to ReferentialIntegrityException.
        
        // TODO: When UserRepository is available, add explicit check:
        // if (userRepository.existsByCompanyId(id)) {
        //     throw new ReferentialIntegrityException("La compañía está en uso y no puede eliminarse");
        // }

        companyRepository.delete(entity);
    }
}
