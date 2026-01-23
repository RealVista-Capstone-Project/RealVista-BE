package com.sep.realvista.presentation.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sep.realvista.application.common.dto.ErrorResponse;
import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.DomainException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<ErrorResponse> handleBusinessConflict(
            BusinessConflictException ex,
            HttpServletRequest request
    ) {
        log.error("Business conflict: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex,
            HttpServletRequest request
    ) {
        log.error("Domain exception: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        log.error("Validation error: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(((FieldError) error).getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .errors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.error("HTTP message not readable: {}", ex.getMessage());

        String message = "Invalid request body";
        String errorCode = "INVALID_REQUEST_BODY";

        // Check if it's an InvalidFormatException (e.g., invalid enum value)
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatEx) {
            // Check if the cause is our custom IllegalArgumentException from enum
            Throwable rootCause = invalidFormatEx.getCause();
            if (rootCause instanceof IllegalArgumentException) {
                message = rootCause.getMessage();
            } else {
                // Generic invalid format message
                Object value = invalidFormatEx.getValue();
                String fieldName = invalidFormatEx.getPath().isEmpty()
                        ? "field"
                        : invalidFormatEx.getPath().getFirst().getFieldName();
                message = String.format("Invalid value '%s' for field '%s'", value, fieldName);
            }
            errorCode = "VALIDATION_ERROR";
        } else if (cause != null && cause.getMessage() != null) {
            message = cause.getMessage();
        }

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.error("Authentication error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Authentication failed: " + ex.getMessage())
                .errorCode("AUTHENTICATION_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.error("Access denied: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("Access denied: " + ex.getMessage())
                .errorCode("ACCESS_DENIED")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.error("Illegal argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .errorCode("INVALID_ARGUMENT")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        log.error("Unsupported media type: {}", ex.getMessage());

        String message = String.format("Content-Type '%s' is not supported. Supported media types are: %s",
                ex.getContentType(),
                ex.getSupportedMediaTypes());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .message(message)
                .errorCode("UNSUPPORTED_MEDIA_TYPE")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred. Please try again later.")
                .errorCode("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

