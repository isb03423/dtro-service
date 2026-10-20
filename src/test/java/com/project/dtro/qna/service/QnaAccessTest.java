package com.project.dtro.qna.service;

import com.project.dtro.qna.domain.Qna;
import com.project.dtro.qna.domain.QnaType;
import com.project.dtro.qna.dto.QnaPageResponse;
import com.project.dtro.qna.repository.QnaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.project.dtro.security.MemberPrincipal;
import com.project.dtro.user.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
class QnaAccessTest {
    @Mock QnaRepository repository;
    @InjectMocks QnaService service;

    @ParameterizedTest
    @CsvSource({"10,USER,true", "11,USER,false", "10,TRAIN_MANAGER,true",
            "11,TRAIN_MANAGER,false", "11,ADMIN,true"})
    void detailAndDeleteRequireOwnerOrAdmin(Long memberId, String role, boolean allowed) {
        login(memberId, role);
        Qna record = record(10L);
        when(repository.findById(1L)).thenReturn(Optional.of(record));
        if (allowed) {
            assertEquals("private body", service.get(1L).getContent());
            service.delete(1L);
            verify(repository).delete(record);
        } else {
            assertThrows(AccessDeniedException.class, () -> service.get(1L));
            assertThrows(AccessDeniedException.class, () -> service.delete(1L));
            verify(repository, never()).delete(any());
        }
    }

    @Test
    void anonymousCannotReadOrDelete() {
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, () -> service.get(1L));
        assertThrows(AccessDeniedException.class, () -> service.delete(1L));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @CsvSource({"11,USER", "11,TRAIN_MANAGER", "10,ADMIN"})
    void nonOwnerAndAdminCannotUpdateContent(Long memberId, String role) {
        login(memberId, role);
        when(repository.findById(1L)).thenReturn(Optional.of(record(10L)));
        assertThrows(AccessDeniedException.class, () -> service.update(1L, new com.project.dtro.qna.dto.QnaRequest()));
        verify(repository, never()).flush();
    }

    @ParameterizedTest
    @CsvSource({"10,USER", "10,TRAIN_MANAGER", "11,USER"})
    void onlyAdminCanAnswer(Long memberId, String role) {
        login(memberId, role);
        assertThrows(AccessDeniedException.class, () -> service.answer(1L, new com.project.dtro.qna.dto.QnaAnswerRequest()));
        verifyNoInteractions(repository);
    }

    @Test
    void recordsWithoutAuthorAreOnlyVisibleToAdmin() {
        when(repository.findById(1L)).thenReturn(Optional.of(record(null)));
        login(10L, "USER");
        assertThrows(AccessDeniedException.class, () -> service.get(1L));
        login(11L, "ADMIN");
        assertEquals("private body", service.get(1L).getContent());
    }

    @Test
    void publicListDoesNotContainBodyOrReply() {
        Qna record = record(10L);
        record.qnaAnswered("private reply");
        var page = QnaPageResponse.from(new PageImpl<>(List.of(record)));
        var summary = page.getContent().getFirst();
        assertNull(summary.getContent());
        assertNull(summary.getReplyContent());
        assertEquals("title", summary.getTitle());
        assertEquals(10L, summary.getCreatedBy());
    }

    private Qna record(Long owner) {
        Qna record = new Qna("title", "private body", "station", QnaType.FACILITY);
        if (owner != null) ReflectionTestUtils.setField(record, "createdBy", Member.builder().id(owner).build());
        return record;
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
