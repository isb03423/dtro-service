package com.project.dtro.train.controller;

import com.project.dtro.common.error.ApiError;
import com.project.dtro.train.service.TrainNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice(assignableTypes = TrainController.class)
public class TrainExceptionHandler {
    @ExceptionHandler(TrainNotFoundException.class)
    // 없는 열차 요청을 404 오류로 변환한다.
    ResponseEntity<ApiError> notFound(TrainNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("TRAIN_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 입력값 검증 오류를 필드별 메시지와 함께 반환한다.
    ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();
        return ResponseEntity.badRequest()
                .body(new ApiError(false, "TRAIN_VALIDATION_FAILED", "입력값을 확인해주세요.", errors));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    // 잘못된 검색 매개변수를 400 오류로 변환한다.
    ResponseEntity<ApiError> invalidParameter(Exception exception) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("TRAIN_INVALID_PARAMETER", "검색 조건을 확인해주세요."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    // 데이터 무결성 충돌을 409 오류로 변환한다.
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("TRAIN_CONFLICT", "중복된 열차번호이거나 연결된 운행정보가 있습니다."));
    }
}
