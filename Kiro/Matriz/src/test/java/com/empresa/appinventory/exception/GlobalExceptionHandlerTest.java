package com.empresa.appinventory.exception;

import com.empresa.appinventory.common.dto.ErrorResponse;
import com.empresa.appinventory.common.dto.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 * Verifies that each exception type is mapped to the correct HTTP status code
 * and error response structure.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Resource not found", response.getBody().message());
        assertEquals("/api/v1/test", response.getBody().path());
    }

    @Test
    void handleDuplicateResourceException_ShouldReturn400() {
        // Given
        DuplicateResourceException exception = new DuplicateResourceException("Duplicate resource");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateResourceException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Duplicate resource", response.getBody().message());
    }

    @Test
    void handleReferentialIntegrityException_ShouldReturn409() {
        // Given
        ReferentialIntegrityException exception = new ReferentialIntegrityException("Referential integrity violation");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleReferentialIntegrityException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Referential integrity violation", response.getBody().message());
    }

    @Test
    void handleValidationException_ShouldReturn400() {
        // Given
        ValidationException exception = new ValidationException("Validation failed");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Validation failed", response.getBody().message());
    }

    @Test
    void handleServiceUnavailableException_ShouldReturn503() {
        // Given
        ServiceUnavailableException exception = new ServiceUnavailableException("Service unavailable");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleServiceUnavailableException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals("Service unavailable", response.getBody().message());
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturn400WithFieldErrors() {
        // Given
        FieldError fieldError1 = new FieldError("user", "name", "El nombre es requerido");
        FieldError fieldError2 = new FieldError("user", "email", "El correo electrónico no tiene un formato válido");
        
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<ValidationErrorResponse> response = exceptionHandler.handleMethodArgumentNotValidException(
            methodArgumentNotValidException, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Validation Failed", response.getBody().error());
        assertEquals(2, response.getBody().fields().size());
        assertEquals("El nombre es requerido", response.getBody().fields().get("name"));
        assertEquals("El correo electrónico no tiene un formato válido", response.getBody().fields().get("email"));
    }

    @Test
    void handleDataIntegrityViolationException_WithRoleForeignKey_ShouldReturn409() {
        // Given
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Cannot delete or update a parent row: a foreign key constraint fails (fk_applications_role)"
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("El rol está en uso y no puede eliminarse", response.getBody().message());
    }

    @Test
    void handleDataIntegrityViolationException_WithGenericConstraint_ShouldReturn409() {
        // Given
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Integrity constraint violation"
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("El recurso está en uso y no puede eliminarse", response.getBody().message());
    }

    @Test
    void handleDataAccessException_ShouldReturn503() {
        // Given
        DataAccessException exception = new DataAccessException("Database connection failed") {};

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataAccessException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals("El servicio no está disponible temporalmente", response.getBody().message());
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        // Given
        Exception exception = new Exception("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Ha ocurrido un error interno. Por favor contacte al administrador.", response.getBody().message());
    }
}
