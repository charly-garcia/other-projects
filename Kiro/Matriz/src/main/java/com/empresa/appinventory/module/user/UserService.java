package com.empresa.appinventory.module.user;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import com.empresa.appinventory.exception.DuplicateResourceException;
import com.empresa.appinventory.exception.ResourceNotFoundException;
import com.empresa.appinventory.exception.ValidationException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for User business logic.
 * Handles CRUD operations with validation and referential integrity checks.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final CompanyRepository companyRepository;
    private final SupplierRepository supplierRepository;
    private final ApplicationRepository applicationRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository,
                       AreaRepository areaRepository,
                       CompanyRepository companyRepository,
                       SupplierRepository supplierRepository,
                       ApplicationRepository applicationRepository,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.areaRepository = areaRepository;
        this.companyRepository = companyRepository;
        this.supplierRepository = supplierRepository;
        this.applicationRepository = applicationRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Create a new user.
     * Validates email uniqueness, foreign key existence, role-application consistency,
     * and date range before persisting.
     *
     * @param requestDTO the user data
     * @return the created user with resolved names
     * @throws DuplicateResourceException if a user with the same email already exists
     * @throws ResourceNotFoundException if any referenced entity does not exist
     * @throws ValidationException if business validation fails
     */
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        // Validate email uniqueness (case-insensitive)
        if (userRepository.existsByEmailIgnoreCase(requestDTO.email())) {
            throw new DuplicateResourceException("El correo electrónico ya está registrado");
        }

        // Validate date range if endDate is provided
        if (requestDTO.endDate() != null && requestDTO.endDate().isBefore(requestDTO.startDate())) {
            throw new ValidationException("La fecha de baja no puede ser anterior a la fecha de alta");
        }

        // Verify optional foreign key references exist
        AreaEntity area = null;
        if (requestDTO.areaId() != null) {
            area = areaRepository.findById(requestDTO.areaId())
                    .orElseThrow(() -> new ResourceNotFoundException("El área especificada no existe"));
        }

        CompanyEntity company = null;
        if (requestDTO.companyId() != null) {
            company = companyRepository.findById(requestDTO.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException("La compañía especificada no existe"));
        }

        SupplierEntity supplier = null;
        if (requestDTO.supplierId() != null) {
            supplier = supplierRepository.findById(requestDTO.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("El proveedor especificado no existe"));
        }

        ApplicationEntity application = null;
        if (requestDTO.applicationId() != null) {
            application = applicationRepository.findById(requestDTO.applicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("La aplicación especificada no existe"));
        }

        RoleEntity role = null;
        if (requestDTO.roleId() != null) {
            role = roleRepository.findById(requestDTO.roleId())
                    .orElseThrow(() -> new ResourceNotFoundException("El rol especificado no existe"));

            // Validate that roleId belongs to applicationId if both are provided
            if (application != null) {
                if (!application.getRole().getId().equals(requestDTO.roleId())) {
                    throw new ValidationException("El rol especificado no pertenece a la aplicación seleccionada");
                }
            }
        }

        // Create and persist entity
        UserEntity entity = new UserEntity(
                requestDTO.name(),
                requestDTO.email(),
                requestDTO.userType(),
                requestDTO.status(),
                requestDTO.startDate(),
                requestDTO.scope(),
                requestDTO.informationAccess()
        );

        // Set optional fields
        entity.setArea(area);
        entity.setCompany(company);
        entity.setSupplier(supplier);
        entity.setApplication(application);
        entity.setRole(role);
        entity.setPosition(requestDTO.position());
        entity.setManager(requestDTO.manager());
        entity.setEndDate(requestDTO.endDate());

        UserEntity savedEntity = userRepository.save(entity);

        return UserResponseDTO.fromEntity(savedEntity);
    }

    /**
     * Get paginated list of users with optional name search.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param search optional search term for name filtering (case-insensitive, partial match)
     * @return paginated response with users
     */
    @Transactional(readOnly = true)
    public PagedResponseDTO<UserResponseDTO> getUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserEntity> userPage;

        if (search != null && !search.trim().isEmpty()) {
            userPage = userRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return new PagedResponseDTO<>(
                userPage.getContent().stream()
                        .map(UserResponseDTO::fromEntity)
                        .toList(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.getNumber(),
                userPage.getSize()
        );
    }

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user with resolved names
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        return UserResponseDTO.fromEntity(entity);
    }

    /**
     * Update an existing user.
     * Validates existence, email uniqueness, foreign key existence,
     * role-application consistency, and date range.
     *
     * @param id the user ID
     * @param requestDTO the updated user data
     * @return the updated user with resolved names
     * @throws ResourceNotFoundException if the user or any referenced entity does not exist
     * @throws DuplicateResourceException if the new email conflicts with another user
     * @throws ValidationException if business validation fails
     */
    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO) {
        // Check if user exists
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        // Check for duplicate email (case-insensitive), excluding current user
        if (!entity.getEmail().equalsIgnoreCase(requestDTO.email()) &&
                userRepository.existsByEmailIgnoreCase(requestDTO.email())) {
            throw new DuplicateResourceException("El correo electrónico ya está registrado");
        }

        // Validate date range if endDate is provided
        if (requestDTO.endDate() != null && requestDTO.endDate().isBefore(requestDTO.startDate())) {
            throw new ValidationException("La fecha de baja no puede ser anterior a la fecha de alta");
        }

        // Verify optional foreign key references exist
        AreaEntity area = null;
        if (requestDTO.areaId() != null) {
            area = areaRepository.findById(requestDTO.areaId())
                    .orElseThrow(() -> new ResourceNotFoundException("El área especificada no existe"));
        }

        CompanyEntity company = null;
        if (requestDTO.companyId() != null) {
            company = companyRepository.findById(requestDTO.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException("La compañía especificada no existe"));
        }

        SupplierEntity supplier = null;
        if (requestDTO.supplierId() != null) {
            supplier = supplierRepository.findById(requestDTO.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("El proveedor especificado no existe"));
        }

        ApplicationEntity application = null;
        if (requestDTO.applicationId() != null) {
            application = applicationRepository.findById(requestDTO.applicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("La aplicación especificada no existe"));
        }

        RoleEntity role = null;
        if (requestDTO.roleId() != null) {
            role = roleRepository.findById(requestDTO.roleId())
                    .orElseThrow(() -> new ResourceNotFoundException("El rol especificado no existe"));

            // Validate that roleId belongs to applicationId if both are provided
            if (application != null) {
                if (!application.getRole().getId().equals(requestDTO.roleId())) {
                    throw new ValidationException("El rol especificado no pertenece a la aplicación seleccionada");
                }
            }
        }

        // Update entity fields
        entity.setName(requestDTO.name());
        entity.setEmail(requestDTO.email());
        entity.setUserType(requestDTO.userType());
        entity.setStatus(requestDTO.status());
        entity.setStartDate(requestDTO.startDate());
        entity.setScope(requestDTO.scope());
        entity.setInformationAccess(requestDTO.informationAccess());
        entity.setArea(area);
        entity.setCompany(company);
        entity.setSupplier(supplier);
        entity.setApplication(application);
        entity.setRole(role);
        entity.setPosition(requestDTO.position());
        entity.setManager(requestDTO.manager());
        entity.setEndDate(requestDTO.endDate());

        UserEntity updatedEntity = userRepository.save(entity);

        return UserResponseDTO.fromEntity(updatedEntity);
    }

    /**
     * Delete a user by ID.
     *
     * @param id the user ID
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Transactional
    public void deleteUser(Long id) {
        // Check if user exists
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        userRepository.delete(entity);
    }
}
