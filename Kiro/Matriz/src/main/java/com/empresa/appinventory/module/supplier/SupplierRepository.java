package com.empresa.appinventory.module.supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Supplier entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> {

    /**
     * Check if a supplier with the given name exists (case-insensitive).
     *
     * @param name the supplier name to check
     * @return true if a supplier with this name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find suppliers by name containing the search term (case-insensitive) with pagination.
     *
     * @param name the search term to match against supplier names
     * @param pageable pagination information
     * @return a page of suppliers matching the search criteria
     */
    Page<SupplierEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
