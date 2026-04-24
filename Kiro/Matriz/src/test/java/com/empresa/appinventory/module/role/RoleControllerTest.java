package com.empresa.appinventory.module.role;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import com.empresa.appinventory.exception.DuplicateResourceException;
import com.empresa.appinventory.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RoleController.
 * Tests REST endpoints, request/response serialization, and HTTP status codes.
 */
@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    private RoleRequestDTO validRequestDTO;
    private RoleResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        validRequestDTO = new RoleRequestDTO("Admin", "Administrator role");
        responseDTO = new RoleResponseDTO(1L, "Admin", "Administrator role");
    }

    // ========== GET /api/v1/roles TESTS ==========

    @Test
    void getRoles_WithDefaultParameters_ShouldReturnPagedResults() throws Exception {
        // Given
        PagedResponseDTO<RoleResponseDTO> pagedResponse = new PagedResponseDTO<>(
            List.of(responseDTO),
            1L,
            1,
            0,
            20
        );
        when(roleService.getRoles(0, 20, null)).thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/roles"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Admin"))
            .andExpect(jsonPath("$.content[0].description").value("Administrator role"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.currentPage").value(0))
            .andExpect(jsonPath("$.pageSize").value(20));

        verify(roleService).getRoles(0, 20, null);
    }

    @Test
    void getRoles_WithCustomPagination_ShouldReturnPagedResults() throws Exception {
        // Given
        PagedResponseDTO<RoleResponseDTO> pagedResponse = new PagedResponseDTO<>(
            List.of(responseDTO),
            10L,
            2,
            1,
            5
        );
        when(roleService.getRoles(1, 5, null)).thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/roles")
                .param("page", "1")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentPage").value(1))
            .andExpect(jsonPath("$.pageSize").value(5));

        verify(roleService).getRoles(1, 5, null);
    }

    @Test
    void getRoles_WithSearchParameter_ShouldReturnFilteredResults() throws Exception {
        // Given
        PagedResponseDTO<RoleResponseDTO> pagedResponse = new PagedResponseDTO<>(
            List.of(responseDTO),
            1L,
            1,
            0,
            20
        );
        when(roleService.getRoles(0, 20, "admin")).thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/roles")
                .param("search", "admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Admin"));

        verify(roleService).getRoles(0, 20, "admin");
    }

    @Test
    void getRoles_WithEmptyResults_ShouldReturnEmptyPage() throws Exception {
        // Given
        PagedResponseDTO<RoleResponseDTO> emptyResponse = new PagedResponseDTO<>(
            List.of(),
            0L,
            0,
            0,
            20
        );
        when(roleService.getRoles(0, 20, null)).thenReturn(emptyResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/roles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(0));

        verify(roleService).getRoles(0, 20, null);
    }

    // ========== GET /api/v1/roles/{id} TESTS ==========

    @Test
    void getRoleById_WithExistingId_ShouldReturnRole() throws Exception {
        // Given
        when(roleService.getRoleById(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/roles/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Admin"))
            .andExpect(jsonPath("$.description").value("Administrator role"));

        verify(roleService).getRoleById(1L);
    }

    @Test
    void getRoleById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        when(roleService.getRoleById(999L))
            .thenThrow(new ResourceNotFoundException("Rol no encontrado con ID: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/roles/999"))
            .andExpect(status().isNotFound());

        verify(roleService).getRoleById(999L);
    }

    // ========== POST /api/v1/roles TESTS ==========

    @Test
    void createRole_WithValidData_ShouldReturnCreatedRole() throws Exception {
        // Given
        when(roleService.createRole(any(RoleRequestDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Admin"))
            .andExpect(jsonPath("$.description").value("Administrator role"));

        verify(roleService).createRole(any(RoleRequestDTO.class));
    }

    @Test
    void createRole_WithMissingName_ShouldReturnBadRequest() throws Exception {
        // Given
        RoleRequestDTO invalidDTO = new RoleRequestDTO(null, "Description");

        // When & Then
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
            .andExpect(status().isBadRequest());

        verify(roleService, never()).createRole(any(RoleRequestDTO.class));
    }

    @Test
    void createRole_WithBlankName_ShouldReturnBadRequest() throws Exception {
        // Given
        RoleRequestDTO invalidDTO = new RoleRequestDTO("   ", "Description");

        // When & Then
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
            .andExpect(status().isBadRequest());

        verify(roleService, never()).createRole(any(RoleRequestDTO.class));
    }

    @Test
    void createRole_WithNameTooLong_ShouldReturnBadRequest() throws Exception {
        // Given
        String longName = "A".repeat(101);
        RoleRequestDTO invalidDTO = new RoleRequestDTO(longName, "Description");

        // When & Then
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
            .andExpect(status().isBadRequest());

        verify(roleService, never()).createRole(any(RoleRequestDTO.class));
    }

    @Test
    void createRole_WithDescriptionTooLong_ShouldReturnBadRequest() throws Exception {
        // Given
        String longDescription = "A".repeat(256);
        RoleRequestDTO invalidDTO = new RoleRequestDTO("Admin", longDescription);

        // When & Then
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
            .andExpect(status().isBadRequest());

        verify(roleService, never()).createRole(any(RoleRequestDTO.class));
    }

    @Test
    void createRole_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        // Given
        when(roleService.createRole(any(RoleRequestDTO.class)))
            .thenThrow(new DuplicateResourceException("El nombre del rol ya existe"));

        // When & Then
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
            .andExpect(status().isBadRequest());

        verify(roleService).createRole(any(RoleRequestDTO.class));
    }

    // ========== PUT /api/v1/roles/{id} TESTS ==========

    @Test
    void updateRole_WithValidData_ShouldReturnUpdatedRole() throws Exception {
        // Given
        RoleRequestDTO updateDTO = new RoleRequestDTO("Super Admin", "Updated description");
        RoleResponseDTO updatedResponse = new RoleResponseDTO(1L, "Super Admin", "Updated description");
        when(roleService.updateRole(eq(1L), any(RoleRequestDTO.class))).thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Super Admin"))
            .andExpect(jsonPath("$.description").value("Updated description"));

        verify(roleService).updateRole(eq(1L), any(RoleRequestDTO.class));
    }

    @Test
    void updateRole_WithMissingName_ShouldReturnBadRequest() throws Exception {
        // Given
        RoleRequestDTO invalidDTO = new RoleRequestDTO(null, "Description");

        // When & Then
        mockMvc.perform(put("/api/v1/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
            .andExpect(status().isBadRequest());

        verify(roleService, never()).updateRole(anyLong(), any(RoleRequestDTO.class));
    }

    @Test
    void updateRole_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        when(roleService.updateRole(eq(999L), any(RoleRequestDTO.class)))
            .thenThrow(new ResourceNotFoundException("Rol no encontrado con ID: 999"));

        // When & Then
        mockMvc.perform(put("/api/v1/roles/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
            .andExpect(status().isNotFound());

        verify(roleService).updateRole(eq(999L), any(RoleRequestDTO.class));
    }

    @Test
    void updateRole_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        // Given
        when(roleService.updateRole(eq(1L), any(RoleRequestDTO.class)))
            .thenThrow(new DuplicateResourceException("El nombre del rol ya existe"));

        // When & Then
        mockMvc.perform(put("/api/v1/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDTO)))
            .andExpect(status().isBadRequest());

        verify(roleService).updateRole(eq(1L), any(RoleRequestDTO.class));
    }

    // ========== DELETE /api/v1/roles/{id} TESTS ==========

    @Test
    void deleteRole_WithExistingId_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(roleService).deleteRole(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/roles/1"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        verify(roleService).deleteRole(1L);
    }

    @Test
    void deleteRole_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Rol no encontrado con ID: 999"))
            .when(roleService).deleteRole(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/roles/999"))
            .andExpect(status().isNotFound());

        verify(roleService).deleteRole(999L);
    }
}
