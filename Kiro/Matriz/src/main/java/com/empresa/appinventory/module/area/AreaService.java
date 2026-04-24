package com.empresa.appinventory.module.area;

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
 * Service layer for Area business logic.
 * Handles CRUD operations with validation and referential integrity checks.
 */
@Service
public class AreaService {

    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    /**
     * Create a new area.
     * Validates name uniqueness before persisting.
     *
     * @param requestDTO the area data
     * @return the created area
     * @throws DuplicateResourceException if an area with the same name already exists
     */
    @Transactional
    public AreaResponseDTO createArea(AreaRequestDTO requestDTO) {
        // Check for duplicate name (case-insensitive)
        if (areaRepository.existsByNameIgnoreCase(requestDTO.name())) {
            throw new DuplicateResourceException("El nombre del área ya existe");
        }

        // Create and persist entity
        AreaEntity entity = new AreaEntity(requestDTO.name(), requestDTO.description());
        AreaEntity savedEntity = areaRepository.save(entity);

        return AreaResponseDTO.fromEntity(savedEntity);
    }

    /**
     * Get paginated list of areas with optional name search.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param search optional search term for name filtering (case-insensitive, partial match)
     * @return paginated response with areas
     */
    @Transactional(readOnly = true)
    public PagedResponseDTO<AreaResponseDTO> getAreas(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AreaEntity> areaPage;

        if (search != null && !search.trim().isEmpty()) {
            areaPage = areaRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            areaPage = areaRepository.findAll(pageable);
        }

        return new PagedResponseDTO<>(
                areaPage.getContent().stream()
                        .map(AreaResponseDTO::fromEntity)
                        .toList(),
                areaPage.getTotalElements(),
                areaPage.getTotalPages(),
                areaPage.getNumber(),
                areaPage.getSize()
        );
    }

    /**
     * Get an area by ID.
     *
     * @param id the area ID
     * @return the area
     * @throws ResourceNotFoundException if the area does not exist
     */
    @Transactional(readOnly = true)
    public AreaResponseDTO getAreaById(Long id) {
        AreaEntity entity = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada con ID: " + id));

        return AreaResponseDTO.fromEntity(entity);
    }

    /**
     * Update an existing area.
     * Validates existence and name uniqueness.
     *
     * @param id the area ID
     * @param requestDTO the updated area data
     * @return the updated area
     * @throws ResourceNotFoundException if the area does not exist
     * @throws DuplicateResourceException if the new name conflicts with another area
     */
    @Transactional
    public AreaResponseDTO updateArea(Long id, AreaRequestDTO requestDTO) {
        // Check if area exists
        AreaEntity entity = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada con ID: " + id));

        // Check for duplicate name (case-insensitive), excluding current area
        if (!entity.getName().equalsIgnoreCase(requestDTO.name()) &&
                areaRepository.existsByNameIgnoreCase(requestDTO.name())) {
            throw new DuplicateResourceException("El nombre del área ya existe");
        }

        // Update entity
        entity.setName(requestDTO.name());
        entity.setDescription(requestDTO.description());
        AreaEntity updatedEntity = areaRepository.save(entity);

        return AreaResponseDTO.fromEntity(updatedEntity);
    }

    /**
     * Delete an area by ID.
     * Validates that the area is not referenced by any user.
     *
     * @param id the area ID
     * @throws ResourceNotFoundException if the area does not exist
     * @throws ReferentialIntegrityException if the area is referenced by users
     */
    @Transactional
    public void deleteArea(Long id) {
        // Check if area exists
        AreaEntity entity = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada con ID: " + id));

        // Check if area is referenced by any user
        // Note: This check will be implemented when UserRepository is available.
        // For now, we rely on database foreign key constraints which will throw
        // DataIntegrityViolationException if the area is in use.
        // The GlobalExceptionHandler should catch this and convert it to ReferentialIntegrityException.
        
        // TODO: When UserRepository is available, add explicit check:
        // if (userRepository.existsByAreaId(id)) {
        //     throw new ReferentialIntegrityException("El área está en uso y no puede eliminarse");
        // }

        areaRepository.delete(entity);
    }
}
