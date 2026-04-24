package com.empresa.appinventory.module.role;

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
 * Service layer for Role business logic.
 * Handles CRUD operations with validation and referential integrity checks.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Create a new role.
     * Validates name uniqueness before persisting.
     *
     * @param requestDTO the role data
     * @return the created role
     * @throws DuplicateResourceException if a role with the same name already exists
     */
    @Transactional
    public RoleResponseDTO createRole(RoleRequestDTO requestDTO) {
        // Check for duplicate name (case-insensitive)
        if (roleRepository.existsByNameIgnoreCase(requestDTO.name())) {
            throw new DuplicateResourceException("El nombre del rol ya existe");
        }

        // Create and persist entity
        RoleEntity entity = new RoleEntity(requestDTO.name(), requestDTO.description());
        RoleEntity savedEntity = roleRepository.save(entity);

        return RoleResponseDTO.fromEntity(savedEntity);
    }

    /**
     * Get paginated list of roles with optional name search.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param search optional search term for name filtering (case-insensitive, partial match)
     * @return paginated response with roles
     */
    @Transactional(readOnly = true)
    public PagedResponseDTO<RoleResponseDTO> getRoles(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoleEntity> rolePage;

        if (search != null && !search.trim().isEmpty()) {
            rolePage = roleRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            rolePage = roleRepository.findAll(pageable);
        }

        return new PagedResponseDTO<>(
                rolePage.getContent().stream()
                        .map(RoleResponseDTO::fromEntity)
                        .toList(),
                rolePage.getTotalElements(),
                rolePage.getTotalPages(),
                rolePage.getNumber(),
                rolePage.getSize()
        );
    }

    /**
     * Get a role by ID.
     *
     * @param id the role ID
     * @return the role
     * @throws ResourceNotFoundException if the role does not exist
     */
    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleById(Long id) {
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));

        return RoleResponseDTO.fromEntity(entity);
    }

    /**
     * Update an existing role.
     * Validates existence and name uniqueness.
     *
     * @param id the role ID
     * @param requestDTO the updated role data
     * @return the updated role
     * @throws ResourceNotFoundException if the role does not exist
     * @throws DuplicateResourceException if the new name conflicts with another role
     */
    @Transactional
    public RoleResponseDTO updateRole(Long id, RoleRequestDTO requestDTO) {
        // Check if role exists
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));

        // Check for duplicate name (case-insensitive), excluding current role
        if (!entity.getName().equalsIgnoreCase(requestDTO.name()) &&
                roleRepository.existsByNameIgnoreCase(requestDTO.name())) {
            throw new DuplicateResourceException("El nombre del rol ya existe");
        }

        // Update entity
        entity.setName(requestDTO.name());
        entity.setDescription(requestDTO.description());
        RoleEntity updatedEntity = roleRepository.save(entity);

        return RoleResponseDTO.fromEntity(updatedEntity);
    }

    /**
     * Delete a role by ID.
     * Validates that the role is not referenced by any application.
     *
     * @param id the role ID
     * @throws ResourceNotFoundException if the role does not exist
     * @throws ReferentialIntegrityException if the role is referenced by applications
     */
    @Transactional
    public void deleteRole(Long id) {
        // Check if role exists
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));

        // Check if role is referenced by any application
        // Note: This check will be implemented when ApplicationRepository is available.
        // For now, we rely on database foreign key constraints which will throw
        // DataIntegrityViolationException if the role is in use.
        // The GlobalExceptionHandler should catch this and convert it to ReferentialIntegrityException.
        
        // TODO: When ApplicationRepository is available, add explicit check:
        // if (applicationRepository.existsByRoleId(id)) {
        //     throw new ReferentialIntegrityException("El rol está en uso y no puede eliminarse");
        // }

        roleRepository.delete(entity);
    }
}
