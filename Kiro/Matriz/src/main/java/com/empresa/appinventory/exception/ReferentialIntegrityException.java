package com.empresa.appinventory.exception;

/**
 * Exception thrown when a referential integrity constraint is violated.
 * Maps to HTTP 409 Conflict.
 */
public class ReferentialIntegrityException extends AppException {
    
    public ReferentialIntegrityException(String message) {
        super(message);
    }
    
    public ReferentialIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }
}
