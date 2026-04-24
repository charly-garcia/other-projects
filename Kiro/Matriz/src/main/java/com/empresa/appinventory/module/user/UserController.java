package com.empresa.appinventory.module.user;

import com.empresa.appinventory.common.dto.PagedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for User management.
 * Exposes CRUD endpoints for the User module.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get paginated list of users with optional search.
     *
     * @param page the page number (0-indexed, default 0)
     * @param size the page size (default 20)
     * @param search optional search term for name filtering
     * @return paginated response with users and HTTP 200
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<UserResponseDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        PagedResponseDTO<UserResponseDTO> response = userService.getUsers(page, size, search);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user and HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new user.
     *
     * @param requestDTO the user data
     * @return the created user and HTTP 201
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO requestDTO) {
        UserResponseDTO response = userService.createUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing user.
     *
     * @param id the user ID
     * @param requestDTO the updated user data
     * @return the updated user and HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO requestDTO
    ) {
        UserResponseDTO response = userService.updateUser(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a user by ID.
     *
     * @param id the user ID
     * @return HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
