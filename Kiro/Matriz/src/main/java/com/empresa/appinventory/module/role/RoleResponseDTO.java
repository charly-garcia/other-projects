package com.empresa.appinventory.module.role;

/**
 * DTO for Role responses.
 * Contains all role information to be sent to clients.
 */
public record RoleResponseDTO(
        Long id,
        String name,
        String description
) {
    /**
     * Factory method to create a RoleResponseDTO from a RoleEntity.
     *
     * @param entity the role entity
     * @return a new RoleResponseDTO
     */
    public static RoleResponseDTO fromEntity(RoleEntity entity) {
        return new RoleResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
