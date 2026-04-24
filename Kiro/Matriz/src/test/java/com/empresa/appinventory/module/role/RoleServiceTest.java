package com.empresa.appinventory.module.role;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import com.empresa.appinventory.exception.DuplicateResourceException;
import com.empresa.appinventory.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleService.
 * Tests business logic including validation, pagination, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private RoleRequestDTO validRequestDTO;
    private RoleEntity roleEntity;

    @BeforeEach
    void setUp() {
        validRequestDTO = new RoleRequestDTO("Admin", "Administrator role");
        roleEntity = new RoleEntity("Admin", "Administrator role");
        roleEntity.setId(1L);
    }

    // ========== CREATE ROLE TESTS ==========

    @Test
    void createRole_WithValidData_ShouldReturnCreatedRole() {
        // Given
        when(roleRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(roleEntity);

        // When
        RoleResponseDTO result = roleService.createRole(validRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Admin", result.name());
        assertEquals("Administrator role", result.description());
        verify(roleRepository).existsByNameIgnoreCase("Admin");
        verify(roleRepository).save(any(RoleEntity.class));
    }

    @Test
    void createRole_WithDuplicateName_ShouldThrowDuplicateResourceException() {
        // Given
        when(roleRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // When & Then
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> roleService.createRole(validRequestDTO)
        );
        
        assertEquals("El nombre del rol ya existe", exception.getMessage());
        verify(roleRepository).existsByNameIgnoreCase("Admin");
        verify(roleRepository, never()).save(any(RoleEntity.class));
    }

    @Test
    void createRole_WithDuplicateNameDifferentCase_ShouldThrowDuplicateResourceException() {
        // Given
        RoleRequestDTO requestDTO = new RoleRequestDTO("ADMIN", "Administrator role");
        when(roleRepository.existsByNameIgnoreCase("ADMIN")).thenReturn(true);

        // When & Then
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> roleService.createRole(requestDTO)
        );
        
        assertEquals("El nombre del rol ya existe", exception.getMessage());
        verify(roleRepository).existsByNameIgnoreCase("ADMIN");
        verify(roleRepository, never()).save(any(RoleEntity.class));
    }

    // ========== GET ROLES (PAGINATION) TESTS ==========

    @Test
    void getRoles_WithoutSearch_ShouldReturnPagedResults() {
        // Given
        List<RoleEntity> roles = List.of(
            createRoleEntity(1L, "Admin", "Administrator"),
            createRoleEntity(2L, "User", "Regular user")
        );
        Page<RoleEntity> page = new PageImpl<>(roles, PageRequest.of(0, 20), 2);
        when(roleRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PagedResponseDTO<RoleResponseDTO> result = roleService.getRoles(0, 20, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(0, result.currentPage());
        assertEquals(20, result.pageSize());
        verify(roleRepository).findAll(any(Pageable.class));
        verify(roleRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void getRoles_WithEmptySearch_ShouldReturnPagedResults() {
        // Given
        List<RoleEntity> roles = List.of(createRoleEntity(1L, "Admin", "Administrator"));
        Page<RoleEntity> page = new PageImpl<>(roles, PageRequest.of(0, 20), 1);
        when(roleRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        PagedResponseDTO<RoleResponseDTO> result = roleService.getRoles(0, 20, "   ");

        // Then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(roleRepository).findAll(any(Pageable.class));
        verify(roleRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void getRoles_WithSearch_ShouldReturnFilteredResults() {
        // Given
        List<RoleEntity> roles = List.of(createRoleEntity(1L, "Admin", "Administrator"));
        Page<RoleEntity> page = new PageImpl<>(roles, PageRequest.of(0, 20), 1);
        when(roleRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);

        // When
        PagedResponseDTO<RoleResponseDTO> result = roleService.getRoles(0, 20, "admin");

        // Then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("Admin", result.content().get(0).name());
        verify(roleRepository).findByNameContainingIgnoreCase("admin", PageRequest.of(0, 20));
        verify(roleRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getRoles_WithSearchCaseInsensitive_ShouldReturnFilteredResults() {
        // Given
        List<RoleEntity> roles = List.of(createRoleEntity(1L, "Admin", "Administrator"));
        Page<RoleEntity> page = new PageImpl<>(roles, PageRequest.of(0, 20), 1);
        when(roleRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);

        // When
        PagedResponseDTO<RoleResponseDTO> result = roleService.getRoles(0, 20, "ADMIN");

        // Then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(roleRepository).findByNameContainingIgnoreCase("ADMIN", PageRequest.of(0, 20));
    }

    @Test
    void getRoles_WithNoResults_ShouldReturnEmptyPage() {
        // Given
        Page<RoleEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(roleRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When
        PagedResponseDTO<RoleResponseDTO> result = roleService.getRoles(0, 20, null);

        // Then
        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        assertEquals(0, result.totalPages());
    }

    // ========== GET ROLE BY ID TESTS ==========

    @Test
    void getRoleById_WithExistingId_ShouldReturnRole() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleEntity));

        // When
        RoleResponseDTO result = roleService.getRoleById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Admin", result.name());
        assertEquals("Administrator role", result.description());
        verify(roleRepository).findById(1L);
    }

    @Test
    void getRoleById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> roleService.getRoleById(999L)
        );
        
        assertEquals("Rol no encontrado con ID: 999", exception.getMessage());
        verify(roleRepository).findById(999L);
    }

    // ========== UPDATE ROLE TESTS ==========

    @Test
    void updateRole_WithValidData_ShouldReturnUpdatedRole() {
        // Given
        RoleRequestDTO updateDTO = new RoleRequestDTO("Super Admin", "Updated description");
        RoleEntity updatedEntity = new RoleEntity("Super Admin", "Updated description");
        updatedEntity.setId(1L);
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleEntity));
        when(roleRepository.existsByNameIgnoreCase("Super Admin")).thenReturn(false);
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(updatedEntity);

        // When
        RoleResponseDTO result = roleService.updateRole(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Super Admin", result.name());
        assertEquals("Updated description", result.description());
        verify(roleRepository).findById(1L);
        verify(roleRepository).existsByNameIgnoreCase("Super Admin");
        verify(roleRepository).save(any(RoleEntity.class));
    }

    @Test
    void updateRole_WithSameName_ShouldUpdateSuccessfully() {
        // Given
        RoleRequestDTO updateDTO = new RoleRequestDTO("Admin", "Updated description");
        RoleEntity updatedEntity = new RoleEntity("Admin", "Updated description");
        updatedEntity.setId(1L);
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleEntity));
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(updatedEntity);

        // When
        RoleResponseDTO result = roleService.updateRole(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("Admin", result.name());
        assertEquals("Updated description", result.description());
        verify(roleRepository).findById(1L);
        verify(roleRepository, never()).existsByNameIgnoreCase(anyString());
        verify(roleRepository).save(any(RoleEntity.class));
    }

    @Test
    void updateRole_WithSameNameDifferentCase_ShouldUpdateSuccessfully() {
        // Given
        RoleRequestDTO updateDTO = new RoleRequestDTO("ADMIN", "Updated description");
        RoleEntity updatedEntity = new RoleEntity("ADMIN", "Updated description");
        updatedEntity.setId(1L);
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleEntity));
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(updatedEntity);

        // When
        RoleResponseDTO result = roleService.updateRole(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("ADMIN", result.name());
        verify(roleRepository).findById(1L);
        verify(roleRepository, never()).existsByNameIgnoreCase(anyString());
        verify(roleRepository).save(any(RoleEntity.class));
    }

    @Test
    void updateRole_WithDuplicateName_ShouldThrowDuplicateResourceException() {
        // Given
        RoleRequestDTO updateDTO = new RoleRequestDTO("User", "Updated description");
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleEntity));
        when(roleRepository.existsByNameIgnoreCase("User")).thenReturn(true);

        // When & Then
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> roleService.updateRole(1L, updateDTO)
        );
        
        assertEquals("El nombre del rol ya existe", exception.getMessage());
        verify(roleRepository).findById(1L);
        verify(roleRepository).existsByNameIgnoreCase("User");
        verify(roleRepository, never()).save(any(RoleEntity.class));
    }

    @Test
    void updateRole_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        RoleRequestDTO updateDTO = new RoleRequestDTO("User", "Description");
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> roleService.updateRole(999L, updateDTO)
        );
        
        assertEquals("Rol no encontrado con ID: 999", exception.getMessage());
        verify(roleRepository).findById(999L);
        verify(roleRepository, never()).existsByNameIgnoreCase(anyString());
        verify(roleRepository, never()).save(any(RoleEntity.class));
    }

    // ========== DELETE ROLE TESTS ==========

    @Test
    void deleteRole_WithExistingId_ShouldDeleteSuccessfully() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleEntity));
        doNothing().when(roleRepository).delete(any(RoleEntity.class));

        // When
        roleService.deleteRole(1L);

        // Then
        verify(roleRepository).findById(1L);
        verify(roleRepository).delete(roleEntity);
    }

    @Test
    void deleteRole_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Given
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> roleService.deleteRole(999L)
        );
        
        assertEquals("Rol no encontrado con ID: 999", exception.getMessage());
        verify(roleRepository).findById(999L);
        verify(roleRepository, never()).delete(any(RoleEntity.class));
    }

    // ========== HELPER METHODS ==========

    private RoleEntity createRoleEntity(Long id, String name, String description) {
        RoleEntity entity = new RoleEntity(name, description);
        entity.setId(id);
        return entity;
    }
}
