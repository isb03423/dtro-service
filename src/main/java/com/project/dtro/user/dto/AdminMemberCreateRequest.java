package com.project.dtro.user.dto;

import com.project.dtro.user.domain.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminMemberCreateRequest(
        @NotBlank @Size(min = 4, max = 50) String userid,
        @NotBlank @Size(max = 50) String name,
        @Email @Size(max = 254) String email,
        @Size(max = 30) String phone,
        @NotNull MemberRole role,
        @NotNull Boolean enabled
) {
}
