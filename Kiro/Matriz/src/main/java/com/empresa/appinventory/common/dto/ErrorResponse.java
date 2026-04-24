package com.empresa.appinventory.common.dto;

import java.time.LocalDateTime;

/**
 * Standard error response structure for API errors.
 * Used for all error responses except validation errors with multiple fields.
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {
}
