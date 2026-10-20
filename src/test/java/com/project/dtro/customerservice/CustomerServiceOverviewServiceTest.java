package com.project.dtro.customerservice;

import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.dto.ComplaintOverviewResponse;
import com.project.dtro.complaint.dto.ComplaintOverviewResponse.RecentComplaint;
import com.project.dtro.complaint.service.ComplaintService;
import com.project.dtro.customerservice.dto.CustomerServiceOverviewResponse;
import com.project.dtro.customerservice.dto.CustomerServiceOverviewResponse.ServiceType;
import com.project.dtro.customerservice.service.CustomerServiceOverviewService;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.dto.QnaOverviewResponse;
import com.project.dtro.qna.dto.QnaOverviewResponse.RecentQna;
import com.project.dtro.qna.service.QnaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceOverviewServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-19T06:00:00Z");

    @Mock private ComplaintService complaintService;
    @Mock private QnaService qnaService;
    private CustomerServiceOverviewService service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceOverviewService(complaintService, qnaService, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void givenMixedRequests_whenOverview_thenAggregateAndReturnLatestFiveInUtc() {
        when(complaintService.getOverview()).thenReturn(new ComplaintOverviewResponse(
                Map.of(ComplaintStatus.PENDING, 2L, ComplaintStatus.ANSWERED, 1L),
                List.of(complaint(1L, 18), complaint(2L, 15), complaint(3L, 13))));
        when(qnaService.getOverview()).thenReturn(new QnaOverviewResponse(
                Map.of(QnaStatus.PENDING, 2L, QnaStatus.ANSWERED, 1L),
                List.of(qna(1L, 17), qna(2L, 16), qna(3L, 14))));

        CustomerServiceOverviewResponse result = service.getOverview();

        assertThat(result.complaintTotal()).isEqualTo(3);
        assertThat(result.qnaTotal()).isEqualTo(3);
        assertThat(result.pendingTotal()).isEqualTo(4);
        assertThat(result.answeredTotal()).isEqualTo(2);
        assertThat(result.fetchedAt()).isEqualTo(NOW);
        assertThat(result.recentRequests()).hasSize(5);
        assertThat(result.recentRequests()).extracting(item -> item.serviceType() + ":" + item.id())
                .containsExactly("COMPLAINT:1", "QNA:1", "QNA:2", "COMPLAINT:2", "QNA:3");
        assertThat(result.recentRequests().getFirst().createdAt()).isEqualTo(Instant.parse("2026-10-18T00:00:00Z"));
    }

    @Test
    void givenNoRequests_whenOverview_thenReturnZeroCountsAndEmptyList() {
        when(complaintService.getOverview()).thenReturn(new ComplaintOverviewResponse(
                Map.of(ComplaintStatus.PENDING, 0L, ComplaintStatus.ANSWERED, 0L), List.of()));
        when(qnaService.getOverview()).thenReturn(new QnaOverviewResponse(
                Map.of(QnaStatus.PENDING, 0L, QnaStatus.ANSWERED, 0L), List.of()));

        CustomerServiceOverviewResponse result = service.getOverview();

        assertThat(result.complaintTotal() + result.qnaTotal()).isZero();
        assertThat(result.pendingTotal()).isZero();
        assertThat(result.answeredTotal()).isZero();
        assertThat(result.recentRequests()).isEmpty();
    }

    @Test
    void givenSameTimestampAndId_whenOverview_thenKeepBothDomainsWithStableOrder() {
        when(complaintService.getOverview()).thenReturn(new ComplaintOverviewResponse(
                Map.of(ComplaintStatus.PENDING, 1L), List.of(complaint(1L, 18))));
        when(qnaService.getOverview()).thenReturn(new QnaOverviewResponse(
                Map.of(QnaStatus.PENDING, 1L), List.of(qna(1L, 18))));

        CustomerServiceOverviewResponse result = service.getOverview();

        assertThat(result.recentRequests()).extracting(CustomerServiceOverviewResponse.RecentRequest::serviceType)
                .containsExactly(ServiceType.COMPLAINT, ServiceType.QNA);
    }

    private RecentComplaint complaint(Long id, int day) {
        return new RecentComplaint(id, "민원 " + id, "중앙역", ComplaintStatus.PENDING,
                LocalDateTime.of(2026, 10, day, 9, 0));
    }

    private RecentQna qna(Long id, int day) {
        return new RecentQna(id, "질문 " + id, "중앙역", QnaStatus.PENDING,
                LocalDateTime.of(2026, 10, day, 9, 0));
    }
}
