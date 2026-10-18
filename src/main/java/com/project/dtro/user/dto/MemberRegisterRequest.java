package com.project.dtro.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberRegisterRequest(
        @NotBlank @Size(min = 4, max = 50) String userid,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 30) String phone,
        @NotNull @AssertTrue(message = "필수 동의 항목을 확인해주세요.") Boolean agreement
) {
}
