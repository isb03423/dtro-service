package com.project.dtro.trainoperation.controller;

import com.project.dtro.common.error.ApiError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TrainOperationController.class)
public class TrainOperationExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<ApiError> invalid(Exception exception) {
        return ResponseEntity.badRequest().body(ApiError.of("OPERATION_INVALID", exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(409).body(ApiError.of("OPERATION_CONFLICT", "연결된 데이터 때문에 처리할 수 없습니다."));
    }
}
