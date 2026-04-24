package com.empresa.appinventory.module.area;

/**
 * DTO for Area responses.
 * Contains all area information to be sent to clients.
 */
public record AreaResponseDTO(
        Long id,
        String name,
        String description
) {
    /**
     * Factory method to create an AreaResponseDTO from an AreaEntity.
     *
     * @param entity the area entity
     * @return a new AreaResponseDTO
     */
    public static AreaResponseDTO fromEntity(AreaEntity entity) {
        return new AreaResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
