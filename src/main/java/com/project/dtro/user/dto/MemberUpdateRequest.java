package com.project.dtro.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 30) String phone
) {
}
