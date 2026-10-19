package com.project.dtro.complaint.service;

import com.project.dtro.complaint.domain.Complaint;
import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.domain.ComplaintType;
import com.project.dtro.complaint.dto.ComplaintAnswerRequest;
import com.project.dtro.complaint.dto.ComplaintRequest;
import com.project.dtro.complaint.repository.ComplaintRepository;
import com.project.dtro.security.MemberPrincipal;
import com.project.dtro.user.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintUpdateTest {
    @Mock private ComplaintRepository complaintRepository;
    @InjectMocks private ComplaintService service;

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerCanUpdatePendingComplaint() {
        login(10L, "USER");
        Complaint complaint = record();
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        var result = service.update(1L, request());

        assertEquals("수정 제목", result.getTitle());
        assertEquals("<p>수정 내용</p>", result.getContent());
        assertEquals("중앙로", result.getStation());
        assertEquals(ComplaintType.SAFETY, result.getType());
        assertEquals(ComplaintStatus.PENDING, result.getStatus());
        assertNull(result.getReplyContent());
        verify(complaintRepository).flush();
    }

    @Test
    void anotherUserCannotUpdateContent() {
        login(11L, "USER");
        assertContentUpdateDenied(record());
    }

    @Test
    void adminCannotUpdateEvenOwnContent() {
        login(10L, "ADMIN");
        assertContentUpdateDenied(record());
    }

    @Test
    void adminCannotUpdateAnotherUsersContent() {
        login(11L, "ADMIN");
        assertContentUpdateDenied(record());
    }

    @Test
    void ownerCannotUpdateAnsweredComplaint() {
        login(10L, "USER");
        Complaint complaint = record();
        complaint.complaintAnswered("기존 답변을 유지합니다.");
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        assertThrows(IllegalStateException.class, () -> service.update(1L, request()));
        assertEquals("원본 내용", complaint.getContent());
        assertEquals("기존 답변을 유지합니다.", complaint.getReplyContent());
        verify(complaintRepository, never()).flush();
    }

    @Test
    void ownerCannotRegisterAnswer() {
        login(10L, "USER");
        assertThrows(AccessDeniedException.class, () -> service.answer(1L, answer()));
        verifyNoInteractions(complaintRepository);
    }

    @Test
    void adminCanAnswerWithoutChangingComplaintContent() {
        login(11L, "ADMIN");
        Complaint complaint = record();
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        var result = service.answer(1L, answer());

        assertEquals("원본 내용", result.getContent());
        assertEquals("제목", result.getTitle());
        assertEquals(ComplaintStatus.ANSWERED, result.getStatus());
        assertEquals("확인 후 조치하겠습니다.", result.getReplyContent());
    }

    @Test
    void existingAnswerCannotBeOverwritten() {
        login(11L, "ADMIN");
        Complaint complaint = record();
        complaint.complaintAnswered("기존 답변을 유지합니다.");
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        assertThrows(IllegalStateException.class, () -> service.answer(1L, answer()));
        assertEquals("기존 답변을 유지합니다.", complaint.getReplyContent());
    }

    @Test
    void anonymousCannotUpdateOrAnswer() {
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, () -> service.update(1L, request()));
        assertThrows(AccessDeniedException.class, () -> service.answer(1L, answer()));
        verifyNoInteractions(complaintRepository);
    }

    @Test
    void missingRecordIsRejected() {
        login(10L, "USER");
        when(complaintRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.update(99L, request()));
    }

    private void assertContentUpdateDenied(Complaint complaint) {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        assertThrows(AccessDeniedException.class, () -> service.update(1L, request()));
        assertEquals("원본 내용", complaint.getContent());
        verify(complaintRepository, never()).flush();
    }

    private Complaint record() {
        Complaint complaint = new Complaint("제목", "원본 내용", "반월당", ComplaintType.FACILITY);
        ReflectionTestUtils.setField(complaint, "createdBy", Member.builder().id(10L).build());
        return complaint;
    }

    private void login(Long memberId, String role) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        var member = new MemberPrincipal(memberId, "test-user", "unused", true, authorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(member, null, authorities));
    }

    private ComplaintRequest request() {
        ComplaintRequest request = new ComplaintRequest();
        request.setTitle("  수정 제목  ");
        request.setContent("  <p>수정 내용</p>  ");
        request.setStation("  중앙로  ");
        request.setType(ComplaintType.SAFETY);
        return request;
    }

    private ComplaintAnswerRequest answer() {
        ComplaintAnswerRequest request = new ComplaintAnswerRequest();
        request.setReplyContent("확인 후 조치하겠습니다.");
        return request;
    }
}
