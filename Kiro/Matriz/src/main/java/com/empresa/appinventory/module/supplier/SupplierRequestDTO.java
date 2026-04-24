package com.empresa.appinventory.module.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating a Supplier.
 * Contains validation constraints for input data.
 */
public record SupplierRequestDTO(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name,

        @NotNull(message = "El cumplimiento es requerido")
        Boolean compliance
) {
}
