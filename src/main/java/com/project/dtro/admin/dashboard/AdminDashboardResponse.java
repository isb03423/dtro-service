package com.project.dtro.admin.dashboard;

import com.project.dtro.user.domain.MemberRole;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record AdminDashboardResponse(
        long totalMembers,
        long enabledMembers,
        long disabledMembers,
        Map<MemberRole, Long> membersByRole,
        List<RecentMember> recentMembers,
        OffsetDateTime fetchedAt
) {
    public record RecentMember(
            Long id,
            String userid,
            String name,
            MemberRole role,
            boolean enabled,
            LocalDateTime createdAt
    ) {
    }
}
