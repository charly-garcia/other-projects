package com.empresa.appinventory.exception;

/**
 * Exception thrown when a service is temporarily unavailable.
 * Maps to HTTP 503 Service Unavailable.
 */
public class ServiceUnavailableException extends AppException {
    
    public ServiceUnavailableException(String message) {
        super(message);
    }
    
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
