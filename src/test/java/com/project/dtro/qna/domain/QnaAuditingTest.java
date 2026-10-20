package com.project.dtro.qna.domain;

import com.project.dtro.common.domain.AuditEntity;
import com.project.dtro.qna.dto.QnaResponse;
import com.project.dtro.user.domain.Member;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.Test;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.mapping.context.PersistentEntities;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QnaAuditingTest {
    @Test
    void inheritedAuditingAcceptsMemberProxyAndResponseExposesItsId() {
        Member proxy = mock(Member.class, withSettings().extraInterfaces(HibernateProxy.class));
        when(proxy.getId()).thenReturn(10L);
        AuditingHandler handler = new AuditingHandler(new PersistentEntities(List.of()));
        handler.setAuditorAware(() -> Optional.of(proxy));
        Qna qna = new Qna("제목", "내용", "중앙역", QnaType.values()[0]);

        assertInstanceOf(AuditEntity.class, qna);
        assertDoesNotThrow(() -> handler.markCreated(qna));
        assertSame(proxy, qna.getCreatedBy());
        assertSame(proxy, qna.getUpdatedBy());
        assertNotNull(qna.getCreatedAt());
        assertEquals(10L, QnaResponse.from(qna).getCreatedBy());
    }

    @Test
    void inheritedAuditingPreservesCreatorWhenModifierChanges() {
        Member creator = Member.builder().id(10L).build();
        Member modifier = Member.builder().id(11L).build();
        AuditingHandler handler = new AuditingHandler(new PersistentEntities(List.of()));
        handler.setAuditorAware(() -> Optional.of(creator));
        Qna qna = new Qna("제목", "내용", "중앙역", QnaType.values()[0]);
        handler.markCreated(qna);

        handler.setAuditorAware(() -> Optional.of(modifier));
        handler.markModified(qna);

        assertSame(creator, qna.getCreatedBy());
        assertSame(modifier, qna.getUpdatedBy());
        assertEquals(10L, QnaResponse.from(qna).getCreatedBy());
        assertNotNull(qna.getUpdatedAt());
    }
}
