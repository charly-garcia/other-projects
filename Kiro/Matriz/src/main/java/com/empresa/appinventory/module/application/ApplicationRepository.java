package com.empresa.appinventory.module.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ApplicationEntity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    /**
     * Check if an application with the given name exists (case-insensitive).
     *
     * @param name the application name to check
     * @return true if an application with this name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find applications by name containing the search term (case-insensitive, paginated).
     *
     * @param name the search term to match against application names
     * @param pageable pagination information
     * @return a page of applications matching the search criteria
     */
    Page<ApplicationEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find applications by role ID (paginated).
     *
     * @param roleId the ID of the role to filter by
     * @param pageable pagination information
     * @return a page of applications associated with the given role
     */
    Page<ApplicationEntity> findByRoleId(Long roleId, Pageable pageable);
}
