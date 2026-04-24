package com.empresa.appinventory.common.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error response structure for validation errors with multiple field errors.
 * Used when Bean Validation fails on multiple fields.
 */
public record ValidationErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    Map<String, String> fields,
    String path
) {
}
