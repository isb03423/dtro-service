package com.project.dtro.user.dto;

import com.project.dtro.user.domain.Member;
import com.project.dtro.user.domain.MemberRole;
import java.time.LocalDateTime;

public record AdminMemberResponse(
        Long id, String userid, String name, String email, String phone,
        MemberRole role, boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static AdminMemberResponse from(Member member) {
        return new AdminMemberResponse(member.getId(), member.getUserid(), member.getName(),
                member.getEmail(), member.getPhone(), member.getRole(), member.isEnabled(),
                member.getCreatedAt(), member.getUpdatedAt());
    }
}
