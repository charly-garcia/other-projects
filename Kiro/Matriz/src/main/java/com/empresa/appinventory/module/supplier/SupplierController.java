package com.empresa.appinventory.module.supplier;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Supplier management.
 * Exposes CRUD endpoints for the Supplier catalog.
 */
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * Get paginated list of suppliers with optional search.
     *
     * @param page the page number (0-indexed, default 0)
     * @param size the page size (default 20)
     * @param search optional search term for name filtering
     * @return paginated response with suppliers and HTTP 200
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<SupplierResponseDTO>> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        PagedResponseDTO<SupplierResponseDTO> response = supplierService.getSuppliers(page, size, search);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a supplier by ID.
     *
     * @param id the supplier ID
     * @return the supplier and HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(@PathVariable Long id) {
        SupplierResponseDTO response = supplierService.getSupplierById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new supplier.
     *
     * @param requestDTO the supplier data
     * @return the created supplier and HTTP 201
     */
    @PostMapping
    public ResponseEntity<SupplierResponseDTO> createSupplier(@Valid @RequestBody SupplierRequestDTO requestDTO) {
        SupplierResponseDTO response = supplierService.createSupplier(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing supplier.
     *
     * @param id the supplier ID
     * @param requestDTO the updated supplier data
     * @return the updated supplier and HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequestDTO requestDTO
    ) {
        SupplierResponseDTO response = supplierService.updateSupplier(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a supplier by ID.
     *
     * @param id the supplier ID
     * @return HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
