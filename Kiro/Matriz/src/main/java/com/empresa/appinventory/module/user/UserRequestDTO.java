package com.empresa.appinventory.module.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO for creating or updating a User.
 * Contains validation constraints for input data.
 * Required fields: name, email, userType, status, startDate, scope, informationAccess.
 * Optional fields: areaId, companyId, supplierId, applicationId, roleId, position, manager, endDate.
 */
public record UserRequestDTO(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
        String name,

        @NotBlank(message = "El correo electrónico es requerido")
        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 255, message = "El correo electrónico no puede exceder 255 caracteres")
        String email,

        @NotNull(message = "El tipo de usuario es requerido")
        UserType userType,

        @NotNull(message = "El estatus es requerido")
        UserStatus status,

        @NotNull(message = "La fecha de alta es requerida")
        LocalDate startDate,

        @NotNull(message = "El alcance es requerido")
        Scope scope,

        @NotNull(message = "El acceso a la información es requerido")
        InformationAccess informationAccess,

        // Optional fields - no validation annotations for requirement
        Long areaId,

        Long companyId,

        Long supplierId,

        Long applicationId,

        Long roleId,

        @Size(max = 150, message = "El puesto no puede exceder 150 caracteres")
        String position,

        @Size(max = 150, message = "El jefe no puede exceder 150 caracteres")
        String manager,

        LocalDate endDate
) {
}
