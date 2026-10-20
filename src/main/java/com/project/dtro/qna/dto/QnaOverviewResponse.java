package com.project.dtro.qna.dto;

import com.project.dtro.qna.domain.QnaStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record QnaOverviewResponse(
        Map<QnaStatus, Long> byStatus,
        List<RecentQna> recent
) {
    public record RecentQna(
            Long id,
            String title,
            String station,
            QnaStatus status,
            LocalDateTime createdAt
    ) {
    }
}
