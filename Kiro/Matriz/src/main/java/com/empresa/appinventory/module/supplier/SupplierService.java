package com.empresa.appinventory.module.supplier;

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
 * Service layer for Supplier business logic.
 * Handles CRUD operations with validation and referential integrity checks.
 */
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    /**
     * Create a new supplier.
     * Validates name uniqueness before persisting.
     *
     * @param requestDTO the supplier data
     * @return the created supplier
     * @throws DuplicateResourceException if a supplier with the same name already exists
     */
    @Transactional
    public SupplierResponseDTO createSupplier(SupplierRequestDTO requestDTO) {
        // Check for duplicate name (case-insensitive)
        if (supplierRepository.existsByNameIgnoreCase(requestDTO.name())) {
            throw new DuplicateResourceException("El nombre del proveedor ya existe");
        }

        // Create and persist entity
        SupplierEntity entity = new SupplierEntity(requestDTO.name(), requestDTO.compliance());
        SupplierEntity savedEntity = supplierRepository.save(entity);

        return SupplierResponseDTO.fromEntity(savedEntity);
    }

    /**
     * Get paginated list of suppliers with optional name search.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param search optional search term for name filtering (case-insensitive, partial match)
     * @return paginated response with suppliers
     */
    @Transactional(readOnly = true)
    public PagedResponseDTO<SupplierResponseDTO> getSuppliers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SupplierEntity> supplierPage;

        if (search != null && !search.trim().isEmpty()) {
            supplierPage = supplierRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            supplierPage = supplierRepository.findAll(pageable);
        }

        return new PagedResponseDTO<>(
                supplierPage.getContent().stream()
                        .map(SupplierResponseDTO::fromEntity)
                        .toList(),
                supplierPage.getTotalElements(),
                supplierPage.getTotalPages(),
                supplierPage.getNumber(),
                supplierPage.getSize()
        );
    }

    /**
     * Get a supplier by ID.
     *
     * @param id the supplier ID
     * @return the supplier
     * @throws ResourceNotFoundException if the supplier does not exist
     */
    @Transactional(readOnly = true)
    public SupplierResponseDTO getSupplierById(Long id) {
        SupplierEntity entity = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));

        return SupplierResponseDTO.fromEntity(entity);
    }

    /**
     * Update an existing supplier.
     * Validates existence and name uniqueness.
     *
     * @param id the supplier ID
     * @param requestDTO the updated supplier data
     * @return the updated supplier
     * @throws ResourceNotFoundException if the supplier does not exist
     * @throws DuplicateResourceException if the new name conflicts with another supplier
     */
    @Transactional
    public SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO requestDTO) {
        // Check if supplier exists
        SupplierEntity entity = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));

        // Check for duplicate name (case-insensitive), excluding current supplier
        if (!entity.getName().equalsIgnoreCase(requestDTO.name()) &&
                supplierRepository.existsByNameIgnoreCase(requestDTO.name())) {
            throw new DuplicateResourceException("El nombre del proveedor ya existe");
        }

        // Update entity
        entity.setName(requestDTO.name());
        entity.setCompliance(requestDTO.compliance());
        SupplierEntity updatedEntity = supplierRepository.save(entity);

        return SupplierResponseDTO.fromEntity(updatedEntity);
    }

    /**
     * Delete a supplier by ID.
     * Validates that the supplier is not referenced by any user.
     *
     * @param id the supplier ID
     * @throws ResourceNotFoundException if the supplier does not exist
     * @throws ReferentialIntegrityException if the supplier is referenced by users
     */
    @Transactional
    public void deleteSupplier(Long id) {
        // Check if supplier exists
        SupplierEntity entity = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));

        // Check if supplier is referenced by any user
        // Note: This check will be implemented when UserRepository is available.
        // For now, we rely on database foreign key constraints which will throw
        // DataIntegrityViolationException if the supplier is in use.
        // The GlobalExceptionHandler should catch this and convert it to ReferentialIntegrityException.
        
        // TODO: When UserRepository is available, add explicit check:
        // if (userRepository.existsBySupplierId(id)) {
        //     throw new ReferentialIntegrityException("El proveedor está en uso y no puede eliminarse");
        // }

        supplierRepository.delete(entity);
    }
}
