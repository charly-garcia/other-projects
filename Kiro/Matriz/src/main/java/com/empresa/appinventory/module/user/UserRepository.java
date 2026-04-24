package com.empresa.appinventory.module.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Check if a user with the given email exists (case-insensitive).
     *
     * @param email the email to check
     * @return true if a user with this email exists, false otherwise
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find users by name containing the search term (case-insensitive) with pagination.
     *
     * @param name the search term to match against user names
     * @param pageable pagination information
     * @return a page of users matching the search criteria
     */
    Page<UserEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find users by email containing the search term (case-insensitive) with pagination.
     *
     * @param email the search term to match against user emails
     * @param pageable pagination information
     * @return a page of users matching the search criteria
     */
    Page<UserEntity> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
