package com.project.dtro.complaint.service;

import com.project.dtro.complaint.domain.Complaint;
import com.project.dtro.complaint.domain.ComplaintType;
import com.project.dtro.complaint.dto.ComplaintPageResponse;
import com.project.dtro.complaint.repository.ComplaintRepository;
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
class ComplaintAccessTest {
    @Mock ComplaintRepository repository;
    @InjectMocks ComplaintService service;

    @ParameterizedTest
    @CsvSource({"10,USER,true", "11,USER,false", "10,TRAIN_MANAGER,true",
            "11,TRAIN_MANAGER,false", "11,ADMIN,true"})
    void detailAndDeleteRequireOwnerOrAdmin(Long memberId, String role, boolean allowed) {
        login(memberId, role);
        Complaint record = record(10L);
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
        Complaint record = record(10L);
        record.complaintAnswered("private reply");
        var page = ComplaintPageResponse.from(new PageImpl<>(List.of(record)));
        var summary = page.getContent().getFirst();
        assertNull(summary.getContent());
        assertNull(summary.getReplyContent());
        assertEquals("title", summary.getTitle());
        assertEquals(10L, summary.getCreatedBy());
    }

    private Complaint record(Long owner) {
        Complaint record = new Complaint("title", "private body", "station", ComplaintType.FACILITY);
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
