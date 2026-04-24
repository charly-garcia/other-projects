package com.empresa.appinventory.module.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Role entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    /**
     * Check if a role with the given name exists (case-insensitive).
     *
     * @param name the role name to check
     * @return true if a role with this name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find roles by name containing the search term (case-insensitive) with pagination.
     *
     * @param name the search term to match against role names
     * @param pageable pagination information
     * @return a page of roles matching the search criteria
     */
    Page<RoleEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
