package com.empresa.appinventory.module.supplier;

/**
 * DTO for Supplier responses.
 * Contains all supplier information to be sent to clients.
 */
public record SupplierResponseDTO(
        Long id,
        String name,
        Boolean compliance
) {
    /**
     * Factory method to create a SupplierResponseDTO from a SupplierEntity.
     *
     * @param entity the supplier entity
     * @return a new SupplierResponseDTO
     */
    public static SupplierResponseDTO fromEntity(SupplierEntity entity) {
        return new SupplierResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getCompliance()
        );
    }
}
