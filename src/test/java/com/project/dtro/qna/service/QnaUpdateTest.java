package com.project.dtro.qna.service;

import com.project.dtro.qna.domain.Qna;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.domain.QnaType;
import com.project.dtro.qna.dto.QnaAnswerRequest;
import com.project.dtro.qna.dto.QnaRequest;
import com.project.dtro.qna.repository.QnaRepository;
import com.project.dtro.security.MemberPrincipal;
import com.project.dtro.user.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
class QnaUpdateTest {
    @Mock QnaRepository qnaRepository;
    @InjectMocks QnaService service;

    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test
    void ownerCanUpdateBeforeAnswer() {
        login(10L, "USER");
        Qna record = record(false);
        var result = service.update(1L, request());
        assertEquals("수정 제목", result.getTitle());
        assertEquals("수정 내용", result.getContent());
        assertEquals(QnaStatus.PENDING, result.getStatus());
        assertNull(result.getReplyContent());
        verify(qnaRepository).flush();
    }

    @Test
    void ownerCannotUpdateAfterAnswerAndOriginalContentIsPreserved() {
        login(10L, "USER");
        Qna record = record(true);
        assertThrows(IllegalStateException.class, () -> service.update(1L, request()));
        assertOriginalQuestion(record);
        assertEquals("기존 답변을 유지합니다.", record.getReplyContent());
        verify(qnaRepository, never()).flush();
    }

    @ParameterizedTest
    @CsvSource({"10,ADMIN,false", "10,ADMIN,true", "11,USER,false", "11,USER,true"})
    void adminAndOtherUsersCannotModifyQuestion(Long memberId, String role, boolean answered) {
        login(memberId, role);
        Qna record = record(answered);
        assertThrows(AccessDeniedException.class, () -> service.update(1L, request()));
        assertOriginalQuestion(record);
        verify(qnaRepository, never()).flush();
    }

    @Test
    void adminCanUpdateOnlyExistingAnswer() {
        login(11L, "ADMIN");
        Qna record = record(true);
        var result = service.updateAnswer(1L, reply());
        assertEquals("변경된 관리자 답변입니다.", result.getReplyContent());
        assertEquals(QnaStatus.ANSWERED, result.getStatus());
        assertOriginalQuestion(record);
        assertEquals(10L, result.getCreatedBy());
        verify(qnaRepository).flush();
    }

    @Test
    void answerUpdateCannotCreateFirstAnswer() {
        login(11L, "ADMIN");
        Qna record = record(false);
        assertThrows(IllegalStateException.class, () -> service.updateAnswer(1L, reply()));
        assertEquals(QnaStatus.PENDING, record.getStatus());
        assertNull(record.getReplyContent());
        verify(qnaRepository, never()).flush();
    }

    @ParameterizedTest
    @CsvSource({"10,USER", "10,TRAIN_MANAGER", "11,USER"})
    void ownerAndOtherUsersCannotUpdateAnswers(Long memberId, String role) {
        login(memberId, role);
        assertThrows(AccessDeniedException.class, () -> service.updateAnswer(1L, reply()));
        verifyNoInteractions(qnaRepository);
    }

    @Test
    void anonymousCannotUpdateQuestionOrAnswer() {
        assertThrows(AccessDeniedException.class, () -> service.update(1L, request()));
        assertThrows(AccessDeniedException.class, () -> service.updateAnswer(1L, reply()));
        verifyNoInteractions(qnaRepository);
    }

    @Test
    void missingQuestionIsRejected() {
        login(11L, "ADMIN");
        when(qnaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateAnswer(99L, reply()));
    }

    private Qna record(boolean answered) {
        Qna record = new Qna("제목", "내용", "반월당", QnaType.FACILITY);
        ReflectionTestUtils.setField(record, "createdBy", Member.builder().id(10L).build());
        if (answered) record.qnaAnswered("기존 답변을 유지합니다.");
        when(qnaRepository.findById(1L)).thenReturn(Optional.of(record));
        return record;
    }

    private void assertOriginalQuestion(Qna record) {
        assertEquals("제목", record.getTitle());
        assertEquals("내용", record.getContent());
        assertEquals("반월당", record.getStation());
        assertEquals(QnaType.FACILITY, record.getType());
    }

    private QnaRequest request() {
        QnaRequest request = new QnaRequest();
        request.setTitle("  수정 제목  ");
        request.setContent("  수정 내용  ");
        request.setStation("  중앙로  ");
        request.setType(QnaType.SAFETY);
        return request;
    }

    private QnaAnswerRequest reply() {
        QnaAnswerRequest request = new QnaAnswerRequest();
        request.setReplyContent("  변경된 관리자 답변입니다.  ");
        return request;
    }

    private void login(Long id, String role) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        var member = new MemberPrincipal(id, "test", "unused", true, authorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(member, null, authorities));
    }
}
