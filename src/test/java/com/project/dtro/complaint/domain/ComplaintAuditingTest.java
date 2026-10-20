package com.project.dtro.complaint.domain;

import com.project.dtro.complaint.dto.ComplaintResponse;
import com.project.dtro.user.domain.Member;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.Test;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.mapping.context.PersistentEntities;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComplaintAuditingTest {
    @Test
    void memberProxyCanBeAssignedAsAuditorAndResponseStillContainsNumericId() {
        Member proxy = mock(Member.class, withSettings().extraInterfaces(HibernateProxy.class));
        when(proxy.getId()).thenReturn(10L);
        AuditingHandler handler = new AuditingHandler(new PersistentEntities(List.of()));
        handler.setAuditorAware(() -> Optional.of(proxy));
        Complaint complaint = new Complaint("제목", "내용", "중앙역", ComplaintType.FACILITY);

        assertDoesNotThrow(() -> handler.markCreated(complaint));

        assertInstanceOf(HibernateProxy.class, complaint.getCreatedBy());
        assertSame(proxy, complaint.getCreatedBy());
        assertSame(proxy, complaint.getUpdatedBy());
        assertEquals(10L, ComplaintResponse.from(complaint).getCreatedBy());
        assertNotNull(complaint.getCreatedAt());
    }

    @Test
    void modificationPreservesCreatorAndStoresNewMemberAsModifier() {
        Member creator = Member.builder().id(10L).build();
        Member modifier = Member.builder().id(11L).build();
        AuditingHandler handler = new AuditingHandler(new PersistentEntities(List.of()));
        handler.setAuditorAware(() -> Optional.of(creator));
        Complaint complaint = new Complaint("제목", "내용", "중앙역", ComplaintType.FACILITY);
        handler.markCreated(complaint);

        handler.setAuditorAware(() -> Optional.of(modifier));
        handler.markModified(complaint);

        assertSame(creator, complaint.getCreatedBy());
        assertSame(modifier, complaint.getUpdatedBy());
        assertEquals(10L, ComplaintResponse.from(complaint).getCreatedBy());
    }
}
