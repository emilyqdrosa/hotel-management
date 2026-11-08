package com.redehoteleira.api_hotel.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgument(
          IllegalArgumentException exception) {

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("erro", exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationErrors(
          MethodArgumentNotValidException exception) {

    String message = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Dados inválidos.");

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("erro", message));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, String>> handleIllegalState(
          IllegalStateException exception) {

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of("erro", exception.getMessage()));
  }
}