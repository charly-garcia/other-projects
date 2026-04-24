package com.empresa.appinventory.module.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating a Role.
 * Contains validation constraints for input data.
 */
public record RoleRequestDTO(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        String description
) {
}
