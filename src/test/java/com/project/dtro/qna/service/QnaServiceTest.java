package com.project.dtro.qna.service;

import com.project.dtro.qna.domain.Qna;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.domain.QnaType;
import com.project.dtro.qna.dto.QnaAnswerRequest;
import com.project.dtro.qna.dto.QnaRequest;
import com.project.dtro.qna.repository.QnaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import com.project.dtro.security.MemberPrincipal;
import com.project.dtro.user.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QnaServiceTest {
    @Mock
    private QnaRepository qnaRepository;

    @Mock
    private Page<Qna> page;

    @InjectMocks
    private QnaService service;

    private QnaRequest request;

    @BeforeEach
    void setUp() {
        request = new QnaRequest();
        request.setTitle("  시설 이용 문의  ");
        request.setContent("  시설 이용 방법을 알려주세요.  ");
        request.setStation("  반월당  ");
        request.setType(QnaType.FACILITY);
    }

    @Test
    void givenValidRequest_whenCreate_thenTrimFieldsAndSetPending() {
        var result = service.create(request);

        assertEquals("시설 이용 문의", result.getTitle());
        assertEquals("시설 이용 방법을 알려주세요.", result.getContent());
        assertEquals("반월당", result.getStation());
        assertEquals(QnaType.FACILITY, result.getType());
        assertEquals(QnaStatus.PENDING, result.getStatus());
        verify(qnaRepository).saveAndFlush(any(Qna.class));
    }

    @Test
    void givenFilters_whenSearch_thenReturnPageAndTrimKeyword() {
        Qna qna = new Qna("시설 이용 문의", "시설 이용 방법", "반월당", QnaType.FACILITY);
        PageRequest pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(qnaRepository.search("시설 이용 문의", QnaStatus.PENDING, QnaType.FACILITY, pageable)).thenReturn(page);
        when(page.getContent()).thenReturn(List.of(qna));
        when(page.getNumber()).thenReturn(1);
        when(page.getSize()).thenReturn(10);
        when(page.getTotalElements()).thenReturn(11L);
        when(page.getTotalPages()).thenReturn(2);

        var result = service.search(QnaType.FACILITY, QnaStatus.PENDING, "  시설 이용 문의  ", 1, 10, "createdAt, desc");

        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(11L, result.getTotalElements());
        assertEquals(2, result.getTotalPage());
        assertEquals("시설 이용 문의", result.getContent().get(0).getTitle());
    }

    @Test
    void givenBlankKeyword_whenSearch_thenRemoveKeyword() {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        when(qnaRepository.search(null, null, null, pageable)).thenReturn(page);
        when(page.getContent()).thenReturn(List.of());

        service.search(null, null, "  ", 0, 20, "id, desc");

        verify(qnaRepository).search(null, null, null, pageable);
    }

    @Test
    void givenInvalidPage_whenSearch_thenReject() {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null, -1, 20, "id,desc"));
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null, 0, 0, "id,desc"));
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null, 0, 101, "id,desc"));
    }

    @Test
    void givenInvalidSort_whenSearch_thenReject() {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null, 0, 20, null));
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null, 0, 20, "replyContent,asc"));
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null, 0, 20, "id,invalid"));
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null, 0, 20, "id,asc,extra"));
    }

    @Test
    void givenMissingQuestion_whenGetAnswerOrDelete_thenReject() {
        QnaAnswerRequest answer = new QnaAnswerRequest();
        answer.setReplyContent("문의하신 내용을 안내드립니다.");
        login(10L, "ADMIN");
        when(qnaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.get(99L));
        assertThrows(IllegalArgumentException.class, () -> service.answer(99L, answer));
        assertThrows(IllegalArgumentException.class, () -> service.delete(99L));
    }

    @Test
    void givenPendingQuestion_whenAnswer_thenSetAnsweredAndRejectDuplicate() {
        Qna qna = new Qna("시설 이용 문의", "시설 이용 방법", "반월당", QnaType.FACILITY);
        QnaAnswerRequest answer = new QnaAnswerRequest();
        answer.setReplyContent("  문의하신 내용을 안내드립니다.  ");
        when(qnaRepository.findById(1L)).thenReturn(Optional.of(qna));

        login(10L, "ADMIN");
        var result = service.answer(1L, answer);

        assertEquals(QnaStatus.ANSWERED, result.getStatus());
        assertEquals("문의하신 내용을 안내드립니다.", result.getReplyContent());
        assertThrows(IllegalStateException.class, () -> service.answer(1L, answer));
    }

    @Test
    void givenExistingQuestion_whenDelete_thenDeleteEntity() {
        Qna qna = new Qna("시설 이용 문의", "시설 이용 방법", "반월당", QnaType.FACILITY);
        when(qnaRepository.findById(1L)).thenReturn(Optional.of(qna));

        login(10L, "ADMIN");
        service.delete(1L);

        verify(qnaRepository).delete(qna);
    }
    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    private void login(Long id, String role) {
        var authorities = java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role));
        var member = new MemberPrincipal(id, "test", "unused", true, authorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(member, null, authorities));
    }

}
