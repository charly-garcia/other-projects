package com.empresa.appinventory.module.company;

/**
 * DTO for Company responses.
 * Contains all company information to be sent to clients.
 */
public record CompanyResponseDTO(
        Long id,
        String name,
        String country
) {
    /**
     * Factory method to create a CompanyResponseDTO from a CompanyEntity.
     *
     * @param entity the company entity
     * @return a new CompanyResponseDTO
     */
    public static CompanyResponseDTO fromEntity(CompanyEntity entity) {
        return new CompanyResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getCountry()
        );
    }
}
