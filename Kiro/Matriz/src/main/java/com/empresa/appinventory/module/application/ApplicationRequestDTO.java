package com.empresa.appinventory.module.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

/**
 * Data Transfer Object for Application creation and update requests.
 * Contains validation constraints for all required fields.
 */
public record ApplicationRequestDTO(
        @NotBlank(message = "El nombre es requerido")
        String name,

        @NotBlank(message = "El owner es requerido")
        String owner,

        @NotBlank(message = "La URL es requerida")
        @URL(message = "La URL proporcionada no tiene un formato válido")
        String url,

        @NotNull(message = "El rol es requerido")
        Long roleId
) {
}
