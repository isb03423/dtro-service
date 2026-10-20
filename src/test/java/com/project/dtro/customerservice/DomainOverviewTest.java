package com.project.dtro.customerservice;

import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.repository.ComplaintRepository;
import com.project.dtro.complaint.service.ComplaintService;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.repository.QnaRepository;
import com.project.dtro.qna.service.QnaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainOverviewTest {
    @Mock private ComplaintRepository complaints;
    @Mock private QnaRepository qnas;

    @Test
    void givenComplaintCounts_whenOverview_thenReturnCountsAndBoundedRecentQuery() {
        when(complaints.countByStatus(ComplaintStatus.PENDING)).thenReturn(12L);
        when(complaints.countByStatus(ComplaintStatus.ANSWERED)).thenReturn(7L);
        when(complaints.findAll(any(Pageable.class))).thenReturn(Page.empty());

        var result = new ComplaintService(complaints).getOverview();

        assertThat(result.byStatus()).containsEntry(ComplaintStatus.PENDING, 12L)
                .containsEntry(ComplaintStatus.ANSWERED, 7L);
        assertThat(result.recent()).isEmpty();
        ArgumentCaptor<Pageable> page = ArgumentCaptor.forClass(Pageable.class);
        verify(complaints).findAll(page.capture());
        assertThat(page.getValue().getPageSize()).isEqualTo(5);
        assertThat(page.getValue().getPageNumber()).isZero();
        assertThat(page.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }

    @Test
    void givenQnaCounts_whenOverview_thenReturnCountsAndBoundedRecentQuery() {
        when(qnas.countByStatus(QnaStatus.PENDING)).thenReturn(4L);
        when(qnas.countByStatus(QnaStatus.ANSWERED)).thenReturn(9L);
        when(qnas.findAll(any(Pageable.class))).thenReturn(Page.empty());

        var result = new QnaService(qnas).getOverview();

        assertThat(result.byStatus()).containsEntry(QnaStatus.PENDING, 4L)
                .containsEntry(QnaStatus.ANSWERED, 9L);
        assertThat(result.recent()).isEmpty();
        ArgumentCaptor<Pageable> page = ArgumentCaptor.forClass(Pageable.class);
        verify(qnas).findAll(page.capture());
        assertThat(page.getValue().getPageSize()).isEqualTo(5);
        assertThat(page.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }
}
