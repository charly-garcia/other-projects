package com.empresa.appinventory.exception;

/**
 * Base exception class for all application-specific exceptions.
 * All custom exceptions in the application should extend this class.
 */
public class AppException extends RuntimeException {
    
    public AppException(String message) {
        super(message);
    }
    
    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
