package com.project.dtro.qna.controller;

import com.project.dtro.common.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = QnaController.class)
public class QnaExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiError> invalidState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("QNA_INVALID_STATE", exception.getMessage()));
    }
}
