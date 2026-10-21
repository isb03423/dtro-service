package com.project.dtro.customerservice.service;

import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.dto.ComplaintOverviewResponse;
import com.project.dtro.complaint.service.ComplaintService;
import com.project.dtro.customerservice.dto.CustomerServiceOverviewResponse;
import com.project.dtro.customerservice.dto.CustomerServiceOverviewResponse.RecentRequest;
import com.project.dtro.customerservice.dto.CustomerServiceOverviewResponse.RequestStatus;
import com.project.dtro.customerservice.dto.CustomerServiceOverviewResponse.ServiceType;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.dto.QnaOverviewResponse;
import com.project.dtro.qna.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomerServiceOverviewService {
    private static final int RECENT_LIMIT = 5;
    private static final ZoneId AUDIT_ZONE = ZoneId.of("Asia/Seoul");

    private final ComplaintService complaintService;
    private final QnaService qnaService;
    private final Clock clock;

    // 각 기능의 공개 서비스 계약으로 집계하고 동일한 DB 스냅샷에서 현황을 조회한다.
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public CustomerServiceOverviewResponse getOverview() {
        ComplaintOverviewResponse complaints = complaintService.getOverview();
        QnaOverviewResponse qnas = qnaService.getOverview();

        Stream<RecentRequest> recentComplaints = complaints.recent().stream()
                .map(item -> new RecentRequest(item.id(), ServiceType.COMPLAINT, item.title(), item.station(),
                        RequestStatus.valueOf(item.status().name()), toInstant(item.createdAt())));
        Stream<RecentRequest> recentQnas = qnas.recent().stream()
                .map(item -> new RecentRequest(item.id(), ServiceType.QNA, item.title(), item.station(),
                        RequestStatus.valueOf(item.status().name()), toInstant(item.createdAt())));

        List<RecentRequest> recent = Stream.concat(recentComplaints, recentQnas)
                .sorted(Comparator.comparing(RecentRequest::createdAt,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(RecentRequest::id, Comparator.reverseOrder())
                        .thenComparing(RecentRequest::serviceType))
                .limit(RECENT_LIMIT)
                .toList();

        long complaintTotal = complaints.byStatus().values().stream().mapToLong(Long::longValue).sum();
        long qnaTotal = qnas.byStatus().values().stream().mapToLong(Long::longValue).sum();
        long pendingTotal = complaints.byStatus().getOrDefault(ComplaintStatus.PENDING, 0L)
                + qnas.byStatus().getOrDefault(QnaStatus.PENDING, 0L);
        long answeredTotal = complaints.byStatus().getOrDefault(ComplaintStatus.ANSWERED, 0L)
                + qnas.byStatus().getOrDefault(QnaStatus.ANSWERED, 0L);

        return new CustomerServiceOverviewResponse(complaintTotal, complaints.byStatus(), qnaTotal,
                qnas.byStatus(), pendingTotal, answeredTotal, recent, clock.instant());
    }

    // 기존 감사 필드는 서울 기준 LocalDateTime이므로 신규 응답에서는 UTC Instant로 변환한다.
    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.atZone(AUDIT_ZONE).toInstant();
    }
}
