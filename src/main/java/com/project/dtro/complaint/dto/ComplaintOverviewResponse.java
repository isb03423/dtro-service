package com.project.dtro.complaint.dto;

import com.project.dtro.complaint.domain.ComplaintStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ComplaintOverviewResponse(
        Map<ComplaintStatus, Long> byStatus,
        List<RecentComplaint> recent
) {
    public record RecentComplaint(
            Long id,
            String title,
            String station,
            ComplaintStatus status,
            LocalDateTime createdAt
    ) {
    }
}
