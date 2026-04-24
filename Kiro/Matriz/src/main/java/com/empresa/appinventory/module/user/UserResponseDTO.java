package com.empresa.appinventory.module.user;

import java.time.LocalDate;

/**
 * DTO for User responses.
 * Contains all user information including resolved names for foreign key relationships.
 */
public record UserResponseDTO(
        Long id,
        String name,
        String email,
        UserType userType,
        UserStatus status,
        LocalDate startDate,
        Scope scope,
        InformationAccess informationAccess,
        Long areaId,
        String areaName,
        Long companyId,
        String companyName,
        Long supplierId,
        String supplierName,
        Long applicationId,
        String applicationName,
        Long roleId,
        String roleName,
        String position,
        String manager,
        LocalDate endDate
) {
    /**
     * Factory method to create a UserResponseDTO from a UserEntity.
     * Resolves all foreign key relationships to include both IDs and names.
     *
     * @param entity the user entity
     * @return a new UserResponseDTO
     */
    public static UserResponseDTO fromEntity(UserEntity entity) {
        return new UserResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getUserType(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getScope(),
                entity.getInformationAccess(),
                entity.getArea() != null ? entity.getArea().getId() : null,
                entity.getArea() != null ? entity.getArea().getName() : null,
                entity.getCompany() != null ? entity.getCompany().getId() : null,
                entity.getCompany() != null ? entity.getCompany().getName() : null,
                entity.getSupplier() != null ? entity.getSupplier().getId() : null,
                entity.getSupplier() != null ? entity.getSupplier().getName() : null,
                entity.getApplication() != null ? entity.getApplication().getId() : null,
                entity.getApplication() != null ? entity.getApplication().getName() : null,
                entity.getRole() != null ? entity.getRole().getId() : null,
                entity.getRole() != null ? entity.getRole().getName() : null,
                entity.getPosition(),
                entity.getManager(),
                entity.getEndDate()
        );
    }
}
