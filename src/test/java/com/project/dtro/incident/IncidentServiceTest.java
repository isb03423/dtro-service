package com.project.dtro.incident;

import com.project.dtro.common.number.NumberGenerator;
import com.project.dtro.incident.domain.*;
import com.project.dtro.incident.dto.*;
import com.project.dtro.incident.repository.*;
import com.project.dtro.incident.service.IncidentService;
import com.project.dtro.train.domain.Train;
import com.project.dtro.trainoperation.domain.TrainOperation;
import com.project.dtro.trainoperation.repository.TrainOperationRepository;
import com.project.dtro.user.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {
    @Mock IncidentRepository incidents;
    @Mock IncidentStatusHistoryRepository histories;
    @Mock TrainOperationRepository operations;
    @Mock AuditorAware<Member> auditor;
    @Mock NumberGenerator numberGenerator;
    IncidentService service;
    Member actor;
    TrainOperation operation;
    OffsetDateTime now;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-17T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        service = new IncidentService(incidents, histories, operations, auditor, clock, numberGenerator);
        actor = Member.builder().id(11L).name("운영자").build();
        operation = mock(TrainOperation.class);
        now = OffsetDateTime.now(clock);
    }

    private Incident incident(long id) {
        Incident incident = new Incident(operation, "신호 이상", now, "구간 지연", actor, now);
        ReflectionTestUtils.setField(incident, "id", id);
        ReflectionTestUtils.setField(incident, "version", 0L);
        return incident;
    }

    private void responseOperation() {
        Train train = mock(Train.class);
        when(operation.getTrain()).thenReturn(train);
    }


    private IncidentActionRequest actionRequest(Long version, String content, String reason) {
        IncidentActionRequest request = new IncidentActionRequest();
        request.setVersion(version);
        request.setActionDetail(content);
        request.setReason(reason);
        return request;
    }

    private IncidentSaveRequest saveRequest(Long operationId, String type, OffsetDateTime occurredAt,
                                            String impact, Long version) {
        IncidentSaveRequest request = new IncidentSaveRequest();
        request.setOperationId(operationId);
        request.setIncidentType(type);
        request.setOccurredAt(occurredAt);
        request.setImpactDetail(impact);
        request.setVersion(version);
        return request;
    }

    private IncidentDeleteRequest deleteRequest(Long id, Long version) {
        IncidentDeleteRequest request = new IncidentDeleteRequest();
        request.setId(id);
        request.setVersion(version);
        return request;
    }

    @Test
    void registrationKeepsUpdateAuditEmptyAndRecordsInitialStatus() {
        when(numberGenerator.nextNumber("INC", 5)).thenReturn("INC-00001");
        when(auditor.getCurrentAuditor()).thenReturn(Optional.of(actor));
        when(operations.findById(1L)).thenReturn(Optional.of(operation));
        when(incidents.saveAndFlush(any())).thenAnswer(invocation -> {
            Incident incident = invocation.getArgument(0);
            ReflectionTestUtils.setField(incident, "id", 101L);
            ReflectionTestUtils.setField(incident, "version", 0L);
            return incident;
        });
        responseOperation();

        IncidentResponse result = service.create(saveRequest(1L, "신호 이상", now, "구간 지연", null));

        assertEquals(IncidentStatus.RECEIVED, result.getStatus());
        assertNull(result.getUpdatedAt());
        assertNull(result.getUpdatedBy());
        ArgumentCaptor<IncidentStatusHistory> capture = ArgumentCaptor.forClass(IncidentStatusHistory.class);
        verify(histories).save(capture.capture());
        assertNull(capture.getValue().getPreviousStatus());
        assertEquals("장애 등록", capture.getValue().getReason());
    }

    @Test
    void workflowUsesAutomaticReasonsAndKeepsActionSnapshot() {
        Incident incident = incident(101L);
        when(incidents.findById(101L)).thenReturn(Optional.of(incident));
        when(auditor.getCurrentAuditor()).thenReturn(Optional.of(actor));
        responseOperation();

        service.action(101L, "check", actionRequest(0L, null, null));
        service.action(101L, "start", actionRequest(0L, "현장 확인", null));
        service.action(101L, "verify", actionRequest(0L, "신호 장치 복구", null));
        service.action(101L, "resume", actionRequest(0L, "추가 점검 필요", null));
        service.action(101L, "verify", actionRequest(0L, "추가 점검 완료", null));
        service.action(101L, "complete", actionRequest(0L, "신호 장치 복구", ""));

        ArgumentCaptor<IncidentStatusHistory> capture = ArgumentCaptor.forClass(IncidentStatusHistory.class);
        verify(histories, times(6)).save(capture.capture());
        assertEquals("확인 시작", capture.getAllValues().get(0).getReason());
        assertEquals("조치 시작", capture.getAllValues().get(1).getReason());
        assertEquals("현장 확인", capture.getAllValues().get(1).getActionDetail());
        assertEquals("복구 확인 시작", capture.getAllValues().get(2).getReason());
        assertEquals("추가 조치", capture.getAllValues().get(3).getReason());
        assertEquals(IncidentStatus.VERIFYING, capture.getAllValues().get(3).getPreviousStatus());
        assertEquals("조치 완료", capture.getAllValues().get(5).getReason());
        assertEquals("신호 장치 복구", capture.getAllValues().get(5).getActionDetail());
        assertEquals(IncidentStatus.COMPLETED, incident.getStatus());
    }

    @Test
    void actionSaveDoesNotAddAStatusHistory() {
        Incident incident = incident(101L);
        incident.check(actor, now);
        incident.start(null, actor, now);
        when(incidents.findById(101L)).thenReturn(Optional.of(incident));
        when(auditor.getCurrentAuditor()).thenReturn(Optional.of(actor));
        responseOperation();

        service.action(101L, "save", actionRequest(0L, "현장 점검 중", null));

        assertEquals(IncidentStatus.IN_PROGRESS, incident.getStatus());
        assertEquals("현장 점검 중", incident.getActionDetail());
        verifyNoInteractions(histories);
    }

    @Test
    void completedIncidentPreventsTheEntireBatchFromBeingDeleted() {
        Incident pending = incident(101L);
        Incident completed = incident(102L);
        completed.check(actor, now);
        completed.start(null, actor, now);
        completed.verifyRecovery("복구", actor, now);
        completed.complete("복구", actor, now);
        when(incidents.findById(101L)).thenReturn(Optional.of(pending));
        when(incidents.findById(102L)).thenReturn(Optional.of(completed));

        assertThrows(IllegalStateException.class, () -> service.delete(List.of(
                deleteRequest(101L, 0L), deleteRequest(102L, 0L))));

        verify(incidents, never()).delete(any(Incident.class));
        verifyNoInteractions(histories);
    }

    @Test
    void staleVersionCannotChangeStatusOrDeleteHistory() {
        Incident incident = incident(101L);
        when(incidents.findById(101L)).thenReturn(Optional.of(incident));

        assertThrows(IllegalStateException.class, () -> service.action(101L, "start", actionRequest(3L, null, null)));
        assertThrows(IllegalStateException.class, () -> service.delete(List.of(deleteRequest(101L, 3L))));

        assertEquals(IncidentStatus.RECEIVED, incident.getStatus());
        verifyNoInteractions(histories);
    }

    @Test
    void completionRequiresRecoveryVerificationAndActionContent() {
        Incident incident = incident(101L);
        assertThrows(IllegalStateException.class, () -> incident.complete("복구", actor, now));
        incident.check(actor, now);
        incident.start(null, actor, now);
        assertThrows(IllegalStateException.class, () -> incident.complete("복구", actor, now));
        assertThrows(IllegalArgumentException.class, () -> incident.verifyRecovery(" ", actor, now));
        incident.verifyRecovery("복구", actor, now);
        assertThrows(IllegalArgumentException.class, () -> incident.complete(" ", actor, now));
        assertEquals(IncidentStatus.VERIFYING, incident.getStatus());
    }
}
