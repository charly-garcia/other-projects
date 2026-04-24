package com.empresa.appinventory.module.area;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Area entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface AreaRepository extends JpaRepository<AreaEntity, Long> {

    /**
     * Check if an area with the given name exists (case-insensitive).
     *
     * @param name the area name to check
     * @return true if an area with this name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find areas by name containing the search term (case-insensitive) with pagination.
     *
     * @param name the search term to match against area names
     * @param pageable pagination information
     * @return a page of areas matching the search criteria
     */
    Page<AreaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
