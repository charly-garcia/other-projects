package com.empresa.appinventory.module.application;

/**
 * Data Transfer Object for Application responses.
 * Contains all application fields including the resolved role name.
 */
public record ApplicationResponseDTO(
        Long id,
        String name,
        String owner,
        String url,
        Long roleId,
        String roleName
) {
}
