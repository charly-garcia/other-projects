package com.empresa.appinventory.module.area;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Area management.
 * Exposes CRUD endpoints for the Area catalog.
 */
@RestController
@RequestMapping("/api/v1/areas")
public class AreaController {

    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    /**
     * Get paginated list of areas with optional search.
     *
     * @param page the page number (0-indexed, default 0)
     * @param size the page size (default 20)
     * @param search optional search term for name filtering
     * @return paginated response with areas and HTTP 200
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<AreaResponseDTO>> getAreas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        PagedResponseDTO<AreaResponseDTO> response = areaService.getAreas(page, size, search);
        return ResponseEntity.ok(response);
    }

    /**
     * Get an area by ID.
     *
     * @param id the area ID
     * @return the area and HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<AreaResponseDTO> getAreaById(@PathVariable Long id) {
        AreaResponseDTO response = areaService.getAreaById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new area.
     *
     * @param requestDTO the area data
     * @return the created area and HTTP 201
     */
    @PostMapping
    public ResponseEntity<AreaResponseDTO> createArea(@Valid @RequestBody AreaRequestDTO requestDTO) {
        AreaResponseDTO response = areaService.createArea(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing area.
     *
     * @param id the area ID
     * @param requestDTO the updated area data
     * @return the updated area and HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<AreaResponseDTO> updateArea(
            @PathVariable Long id,
            @Valid @RequestBody AreaRequestDTO requestDTO
    ) {
        AreaResponseDTO response = areaService.updateArea(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an area by ID.
     *
     * @param id the area ID
     * @return HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable Long id) {
        areaService.deleteArea(id);
        return ResponseEntity.noContent().build();
    }
}
