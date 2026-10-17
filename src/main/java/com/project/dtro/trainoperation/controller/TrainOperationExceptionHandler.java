package com.project.dtro.trainoperation.controller;

import com.project.dtro.common.error.ApiError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice(assignableTypes = TrainOperationController.class)
public class TrainOperationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 입력값 검증 오류를 필드별 메시지와 함께 반환한다.
    ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();
        return ResponseEntity.badRequest()
                .body(new ApiError(false, "OPERATION_INVALID", "입력값을 확인해주세요.", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    // 운행정보의 검색 조건과 저장 조건 오류를 400 오류로 변환한다.
    ResponseEntity<ApiError> invalidParameter(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("OPERATION_INVALID", exception.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    // 요청 본문이나 매개변수의 형식이 잘못된 경우를 처리한다.
    ResponseEntity<ApiError> invalidFormat(Exception exception) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("OPERATION_INVALID", "입력값을 확인해주세요."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    // 데이터 무결성 충돌을 409 오류로 변환한다.
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("OPERATION_CONFLICT", "연결된 데이터 때문에 처리할 수 없습니다."));
    }
}
