package com.nit.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.nit.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()));

        ApiResponse<Map<String, String>> response =
                new ApiResponse<>(
                        false,
                        "Validation failed",
                        errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex) {

        ApiResponse<Void> response =
                new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    // Handle specific HTTP status exceptions, including 404 Not Found.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(
            ResponseStatusException ex) {

        HttpStatus status =
                HttpStatus.valueOf(ex.getStatusCode().value());

        String message =
                ex.getReason() != null
                        ? ex.getReason()
                        : status.getReasonPhrase();

        ApiResponse<Void> response =
                new ApiResponse<>(
                        false,
                        message,
                        null);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    // Handle application/business validation errors.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex) {

        ApiResponse<Void> response =
                new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception ex) {

        logger.error(
                "Unexpected application error",
                ex);

        ApiResponse<Void> response =
                new ApiResponse<>(
                        false,
                        "An internal server error occurred.",
                        null);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}