package com.empresa.appinventory.module.application;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import com.empresa.appinventory.exception.DuplicateResourceException;
import com.empresa.appinventory.exception.ReferentialIntegrityException;
import com.empresa.appinventory.exception.ResourceNotFoundException;
import com.empresa.appinventory.module.role.RoleEntity;
import com.empresa.appinventory.module.role.RoleRepository;
import com.empresa.appinventory.module.role.RoleResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Application business logic.
 * Handles CRUD operations with validation and referential integrity checks.
 */
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final RoleRepository roleRepository;

    public ApplicationService(ApplicationRepository applicationRepository, RoleRepository roleRepository) {
        this.applicationRepository = applicationRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Create a new application.
     * Validates name uniqueness and role existence before persisting.
     *
     * @param requestDTO the application data
     * @return the created application with resolved role name
     * @throws DuplicateResourceException if an application with the same name already exists
     * @throws ResourceNotFoundException if the specified role does not exist
     */
    @Transactional
    public ApplicationResponseDTO createApplication(ApplicationRequestDTO requestDTO) {
        // Check for duplicate name (case-insensitive)
        if (applicationRepository.existsByNameIgnoreCase(requestDTO.name())) {
            throw new DuplicateResourceException("El nombre de la aplicación ya existe");
        }

        // Verify role exists
        RoleEntity role = roleRepository.findById(requestDTO.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("El rol especificado no existe"));

        // Create and persist entity
        ApplicationEntity entity = new ApplicationEntity(
                requestDTO.name(),
                requestDTO.owner(),
                requestDTO.url(),
                role
        );
        ApplicationEntity savedEntity = applicationRepository.save(entity);

        return toResponseDTO(savedEntity);
    }

    /**
     * Get paginated list of applications with optional name search.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param search optional search term for name filtering (case-insensitive, partial match)
     * @return paginated response with applications
     */
    @Transactional(readOnly = true)
    public PagedResponseDTO<ApplicationResponseDTO> getApplications(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationEntity> applicationPage;

        if (search != null && !search.trim().isEmpty()) {
            applicationPage = applicationRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            applicationPage = applicationRepository.findAll(pageable);
        }

        return new PagedResponseDTO<>(
                applicationPage.getContent().stream()
                        .map(this::toResponseDTO)
                        .toList(),
                applicationPage.getTotalElements(),
                applicationPage.getTotalPages(),
                applicationPage.getNumber(),
                applicationPage.getSize()
        );
    }

    /**
     * Get an application by ID.
     *
     * @param id the application ID
     * @return the application with resolved role name
     * @throws ResourceNotFoundException if the application does not exist
     */
    @Transactional(readOnly = true)
    public ApplicationResponseDTO getApplicationById(Long id) {
        ApplicationEntity entity = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplicación no encontrada con ID: " + id));

        return toResponseDTO(entity);
    }

    /**
     * Update an existing application.
     * Validates existence, name uniqueness, and role existence.
     *
     * @param id the application ID
     * @param requestDTO the updated application data
     * @return the updated application with resolved role name
     * @throws ResourceNotFoundException if the application or role does not exist
     * @throws DuplicateResourceException if the new name conflicts with another application
     */
    @Transactional
    public ApplicationResponseDTO updateApplication(Long id, ApplicationRequestDTO requestDTO) {
        // Check if application exists
        ApplicationEntity entity = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplicación no encontrada con ID: " + id));

        // Check for duplicate name (case-insensitive), excluding current application
        if (!entity.getName().equalsIgnoreCase(requestDTO.name()) &&
                applicationRepository.existsByNameIgnoreCase(requestDTO.name())) {
            throw new DuplicateResourceException("El nombre de la aplicación ya existe");
        }

        // Verify new role exists
        RoleEntity role = roleRepository.findById(requestDTO.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("El rol especificado no existe"));

        // Update entity
        entity.setName(requestDTO.name());
        entity.setOwner(requestDTO.owner());
        entity.setUrl(requestDTO.url());
        entity.setRole(role);
        ApplicationEntity updatedEntity = applicationRepository.save(entity);

        return toResponseDTO(updatedEntity);
    }

    /**
     * Delete an application by ID.
     * Validates that the application is not referenced by any user.
     *
     * @param id the application ID
     * @throws ResourceNotFoundException if the application does not exist
     * @throws ReferentialIntegrityException if the application is referenced by users
     */
    @Transactional
    public void deleteApplication(Long id) {
        // Check if application exists
        ApplicationEntity entity = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplicación no encontrada con ID: " + id));

        // Check if application is referenced by any user
        // Note: This check will be implemented when UserRepository is available.
        // For now, we rely on database foreign key constraints which will throw
        // DataIntegrityViolationException if the application is in use.
        // The GlobalExceptionHandler should catch this and convert it to ReferentialIntegrityException.
        
        // TODO: When UserRepository is available, add explicit check:
        // if (userRepository.existsByApplicationId(id)) {
        //     throw new ReferentialIntegrityException("La aplicación está en uso y no puede eliminarse");
        // }

        applicationRepository.delete(entity);
    }

    /**
     * Get roles associated with an application.
     * Returns a list containing the single role associated with the application.
     * This endpoint is used by the frontend dropdown for role selection.
     *
     * @param applicationId the application ID
     * @return list containing the role associated with the application
     * @throws ResourceNotFoundException if the application does not exist
     */
    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getRolesByApplicationId(Long applicationId) {
        ApplicationEntity entity = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Aplicación no encontrada con ID: " + applicationId));

        // Return the single role as a list for consistency with frontend dropdown
        return List.of(RoleResponseDTO.fromEntity(entity.getRole()));
    }

    /**
     * Convert ApplicationEntity to ApplicationResponseDTO with resolved role name.
     *
     * @param entity the application entity
     * @return the response DTO
     */
    private ApplicationResponseDTO toResponseDTO(ApplicationEntity entity) {
        return new ApplicationResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getOwner(),
                entity.getUrl(),
                entity.getRole().getId(),
                entity.getRole().getName()
        );
    }
}
