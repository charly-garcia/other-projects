package com.empresa.appinventory.exception;

/**
 * Exception thrown when business validation fails.
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends AppException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
