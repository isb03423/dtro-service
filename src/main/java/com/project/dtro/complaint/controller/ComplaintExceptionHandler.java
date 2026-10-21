package com.project.dtro.complaint.controller;

import com.project.dtro.common.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ComplaintController.class)
public class ComplaintExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiError> invalidState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("COMPLAINT_INVALID_STATE", exception.getMessage()));
    }
}
