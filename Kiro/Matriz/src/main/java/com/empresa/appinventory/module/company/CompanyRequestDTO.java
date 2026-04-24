package com.empresa.appinventory.module.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating a Company.
 * Contains validation constraints for input data.
 */
public record CompanyRequestDTO(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name,

        @NotBlank(message = "El país es requerido")
        @Size(max = 100, message = "El país no puede exceder 100 caracteres")
        String country
) {
}
