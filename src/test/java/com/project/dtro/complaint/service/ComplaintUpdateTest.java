package com.project.dtro.complaint.service;

import com.project.dtro.complaint.domain.Complaint;
import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.domain.ComplaintType;
import com.project.dtro.complaint.dto.ComplaintRequest;
import com.project.dtro.complaint.repository.ComplaintRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintUpdateTest {
    @Mock
    private ComplaintRepository complaintRepository;

    @InjectMocks
    private ComplaintService service;

    @Test
    void givenAnsweredRecord_whenUpdate_thenPreserveReplyAndStatus() {
        Complaint record = new Complaint("제목", "내용", "반월당", ComplaintType.FACILITY);
        record.complaintAnswered("기존 답변을 유지합니다.");
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(record));
        ComplaintRequest request = new ComplaintRequest();
        request.setTitle("  수정 제목  ");
        request.setContent("  <p>수정 내용</p>  ");
        request.setStation("  중앙로  ");
        request.setType(ComplaintType.SAFETY);

        var result = service.update(1L, request);

        assertEquals("수정 제목", result.getTitle());
        assertEquals("<p>수정 내용</p>", result.getContent());
        assertEquals("중앙로", result.getStation());
        assertEquals(ComplaintType.SAFETY, result.getType());
        assertEquals(ComplaintStatus.ANSWERED, result.getStatus());
        assertEquals("기존 답변을 유지합니다.", result.getReplyContent());
        verify(complaintRepository).flush();
    }

    @Test
    void givenMissingRecord_whenUpdate_thenReject() {
        when(complaintRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.update(99L, new ComplaintRequest()));
    }
}
