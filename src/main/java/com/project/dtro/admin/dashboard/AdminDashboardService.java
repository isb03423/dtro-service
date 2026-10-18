package com.project.dtro.admin.dashboard;

import com.project.dtro.user.domain.MemberRole;
import com.project.dtro.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final MemberRepository members;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        long total = members.count();
        long enabled = members.countByEnabled(true);
        Map<MemberRole, Long> byRole = new EnumMap<>(MemberRole.class);
        for (MemberRole role : MemberRole.values()) {
            byRole.put(role, members.countByRole(role));
        }

        var recent = members.findTop5ByOrderByCreatedAtDescIdDesc().stream()
                .map(member -> new AdminDashboardResponse.RecentMember(
                        member.getId(), member.getUserid(), member.getName(), member.getRole(),
                        member.isEnabled(), member.getCreatedAt()))
                .toList();

        return new AdminDashboardResponse(total, enabled, total - enabled, byRole, recent, OffsetDateTime.now());
    }
}
