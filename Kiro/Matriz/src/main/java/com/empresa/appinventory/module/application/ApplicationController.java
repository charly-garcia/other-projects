package com.empresa.appinventory.module.application;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import com.empresa.appinventory.module.role.RoleResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Application management.
 * Exposes CRUD endpoints for the Application inventory.
 */
@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Get paginated list of applications with optional search.
     *
     * @param page the page number (0-indexed, default 0)
     * @param size the page size (default 20)
     * @param search optional search term for name filtering
     * @return paginated response with applications and HTTP 200
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<ApplicationResponseDTO>> getApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        PagedResponseDTO<ApplicationResponseDTO> response = applicationService.getApplications(page, size, search);
        return ResponseEntity.ok(response);
    }

    /**
     * Get an application by ID.
     *
     * @param id the application ID
     * @return the application and HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(@PathVariable Long id) {
        ApplicationResponseDTO response = applicationService.getApplicationById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new application.
     *
     * @param requestDTO the application data
     * @return the created application and HTTP 201
     */
    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> createApplication(@Valid @RequestBody ApplicationRequestDTO requestDTO) {
        ApplicationResponseDTO response = applicationService.createApplication(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing application.
     *
     * @param id the application ID
     * @param requestDTO the updated application data
     * @return the updated application and HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequestDTO requestDTO
    ) {
        ApplicationResponseDTO response = applicationService.updateApplication(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an application by ID.
     *
     * @param id the application ID
     * @return HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get roles associated with an application.
     * Returns a list containing the single role associated with the application.
     * This endpoint is used by the frontend dropdown for role selection when creating/editing users.
     *
     * @param id the application ID
     * @return list containing the role associated with the application and HTTP 200
     */
    @GetMapping("/{id}/roles")
    public ResponseEntity<List<RoleResponseDTO>> getRolesByApplicationId(@PathVariable Long id) {
        List<RoleResponseDTO> response = applicationService.getRolesByApplicationId(id);
        return ResponseEntity.ok(response);
    }
}
