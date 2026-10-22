package com.project.dtro.incident.controller;

import com.project.dtro.common.error.ApiError;
import com.project.dtro.incident.service.IncidentNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice(assignableTypes = IncidentController.class)
public class IncidentExceptionHandler {
    @ExceptionHandler(IncidentNotFoundException.class)
    // 없는 운행 장애 요청을 404 오류로 변환한다.
    ResponseEntity<ApiError> notFound(IncidentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("INCIDENT_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 입력값 검증 오류를 필드별 메시지와 함께 반환한다.
    ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();
        return ResponseEntity.badRequest()
                .body(new ApiError(false, "INCIDENT_INVALID", "입력값을 확인해주세요.", errors));
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    // 잘못된 입력 또는 검색 매개변수를 400 오류로 변환한다.
    ResponseEntity<ApiError> invalidParameter(Exception exception) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("INCIDENT_INVALID", "입력값을 확인해주세요."));
    }

    @ExceptionHandler(IllegalStateException.class)
    // 현재 단계에서 허용되지 않는 처리를 409 오류로 변환한다.
    ResponseEntity<ApiError> state(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("INCIDENT_CONFLICT", exception.getMessage()));
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    // 다른 사용자가 먼저 변경한 경우 새로 조회하도록 안내한다.
    ResponseEntity<ApiError> stale(Exception exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("INCIDENT_CONFLICT", "다른 사용자가 변경한 장애입니다. 새로 조회해주세요."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    // 연결된 데이터의 무결성 충돌을 409 오류로 변환한다.
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("INCIDENT_CONFLICT", "연결된 데이터가 변경되었습니다. 새로 조회해주세요."));
    }
}
