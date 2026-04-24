package com.empresa.appinventory.module.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Company entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {

    /**
     * Check if a company with the given name exists (case-insensitive).
     *
     * @param name the company name to check
     * @return true if a company with this name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find companies by name containing the search term (case-insensitive) with pagination.
     *
     * @param name the search term to match against company names
     * @param pageable pagination information
     * @return a page of companies matching the search criteria
     */
    Page<CompanyEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
