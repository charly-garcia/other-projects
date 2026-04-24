package com.empresa.appinventory.exception;

/**
 * Exception thrown when attempting to create a resource with a duplicate unique field.
 * Maps to HTTP 400 Bad Request.
 */
public class DuplicateResourceException extends AppException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
