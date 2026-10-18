package com.project.dtro.user.controller;

import com.project.dtro.common.error.ApiError;
import com.project.dtro.user.service.InvalidCurrentPasswordException;
import com.project.dtro.user.service.MemberConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MemberController.class)
public class MemberExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();
        return ResponseEntity.badRequest()
                .body(new ApiError(false, "MEMBER_VALIDATION_FAILED", "입력값을 확인해주세요.", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> invalidJson(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("MEMBER_INVALID_BODY", "요청 내용을 확인해주세요."));
    }

    @ExceptionHandler({MemberConflictException.class, DataIntegrityViolationException.class})
    ResponseEntity<ApiError> conflict(Exception exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("MEMBER_USERID_CONFLICT", "이미 사용 중인 아이디입니다."));
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    ResponseEntity<ApiError> invalidPassword(InvalidCurrentPasswordException exception) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("MEMBER_CURRENT_PASSWORD_INVALID", exception.getMessage()));
    }
}
