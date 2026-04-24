package com.empresa.appinventory.module.role;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Role management.
 * Exposes CRUD endpoints for the Role catalog.
 */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Get paginated list of roles with optional search.
     *
     * @param page the page number (0-indexed, default 0)
     * @param size the page size (default 20)
     * @param search optional search term for name filtering
     * @return paginated response with roles and HTTP 200
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<RoleResponseDTO>> getRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        PagedResponseDTO<RoleResponseDTO> response = roleService.getRoles(page, size, search);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a role by ID.
     *
     * @param id the role ID
     * @return the role and HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> getRoleById(@PathVariable Long id) {
        RoleResponseDTO response = roleService.getRoleById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new role.
     *
     * @param requestDTO the role data
     * @return the created role and HTTP 201
     */
    @PostMapping
    public ResponseEntity<RoleResponseDTO> createRole(@Valid @RequestBody RoleRequestDTO requestDTO) {
        RoleResponseDTO response = roleService.createRole(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing role.
     *
     * @param id the role ID
     * @param requestDTO the updated role data
     * @return the updated role and HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequestDTO requestDTO
    ) {
        RoleResponseDTO response = roleService.updateRole(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a role by ID.
     *
     * @param id the role ID
     * @return HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
