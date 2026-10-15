package com.project.dtro.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class ApiError {
    private final boolean success;
    private final String code;
    private final String message;
    private final List<String> errors;

    // 오류 코드와 메시지로 기본 오류 응답을 만든다.
    public static ApiError of(String code, String message) {
        return new ApiError(false, code, message, List.of());
    }
}
