package com.project.dtro.user.dto;

import com.project.dtro.user.domain.Member;

public record MemberResponse(Long id, String userid, String name, String email, String phone, String role) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getUserid(), member.getName(),
                member.getEmail(), member.getPhone(), member.getRole().name());
    }
}
