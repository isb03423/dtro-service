package com.project.dtro.user.dto;

import com.project.dtro.user.domain.Member;
import org.springframework.data.domain.Page;
import java.util.List;

public record AdminMemberPageResponse(
        List<AdminMemberResponse> content, int page, int size, long totalElements, int totalPages
) {
    public static AdminMemberPageResponse from(Page<Member> members) {
        return new AdminMemberPageResponse(members.map(AdminMemberResponse::from).getContent(),
                members.getNumber(), members.getSize(), members.getTotalElements(), members.getTotalPages());
    }
}
