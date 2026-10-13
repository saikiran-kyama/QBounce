package com.qbounce.exception;

import com.qbounce.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global exception handler for the QBounce service. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");

    log.warn("Validation error on {}: {}", request.getRequestURI(), message);

    return ResponseEntity.badRequest().body(buildError(HttpStatus.BAD_REQUEST, message, request));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    log.warn("Malformed request body on {}: {}", request.getRequestURI(), ex.getMessage());

    return ResponseEntity.badRequest()
        .body(buildError(HttpStatus.BAD_REQUEST, "Malformed or missing request body", request));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {
    log.warn("Illegal argument on {}: {}", request.getRequestURI(), ex.getMessage());

    return ResponseEntity.badRequest()
        .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ErrorResponse> handleDataAccessException(
      DataAccessException ex, HttpServletRequest request) {
    log.error("Database error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(
            buildError(
                HttpStatus.SERVICE_UNAVAILABLE, "Database temporarily unavailable", request));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {
    log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

    return ResponseEntity.internalServerError()
        .body(
            buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request));
  }

  private ErrorResponse buildError(HttpStatus status, String message, HttpServletRequest request) {
    return ErrorResponse.builder()
        .status(status.value())
        .message(message)
        .timestamp(LocalDateTime.now().format(FORMATTER))
        .path(request.getRequestURI())
        .build();
  }
}
