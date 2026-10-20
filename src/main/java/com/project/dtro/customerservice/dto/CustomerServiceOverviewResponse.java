package com.project.dtro.customerservice.dto;

import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.qna.domain.QnaStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CustomerServiceOverviewResponse(
        long complaintTotal,
        Map<ComplaintStatus, Long> complaintByStatus,
        long qnaTotal,
        Map<QnaStatus, Long> qnaByStatus,
        long pendingTotal,
        long answeredTotal,
        List<RecentRequest> recentRequests,
        Instant fetchedAt
) {
    public enum ServiceType {
        COMPLAINT, QNA
    }

    public enum RequestStatus {
        PENDING, ANSWERED
    }

    public record RecentRequest(
            Long id,
            ServiceType serviceType,
            String title,
            String station,
            RequestStatus status,
            Instant createdAt
    ) {
    }
}
