package com.project.dtro.user.controller;

import com.project.dtro.common.error.ApiError;
import com.project.dtro.user.service.MemberConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdminMemberController.class)
public class AdminMemberExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();
        return ResponseEntity.badRequest()
                .body(new ApiError(false, "ADMIN_MEMBER_VALIDATION_FAILED", "입력값을 확인해주세요.", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("ADMIN_MEMBER_INVALID_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler({MemberConflictException.class, DataIntegrityViolationException.class})
    ResponseEntity<ApiError> conflict(Exception exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("ADMIN_MEMBER_USERID_CONFLICT", "이미 사용 중인 아이디입니다."));
    }
}
