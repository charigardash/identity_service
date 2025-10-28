package com.learning.controller;

import com.learning.customException.IdentityAppExcpetion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdentityAppExcpetion.class)
    public ResponseEntity<Map<String, Object>> handleIdentityAppException(IdentityAppExcpetion ex){
        Map<String, Object> error = new HashMap<>();
        error.put("error", ex.getClass().getSimpleName());
        error.put("message", ex.getMessage());
        error.put("status", ex.getStatus().value());
        error.put("timestamp", LocalDateTime.now());
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "InternalServerError");
        error.put("message", ex.getMessage());
        error.put("status", 500);
        error.put("timestamp", LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
