package com.project.dtro.incident.domain;

import com.project.dtro.trainoperation.domain.TrainOperation;
import com.project.dtro.user.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "incident")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_id")
    private Long id;

    @Column(name = "incident_number", unique = true, length = 30)
    private String incidentNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_id", nullable = false)
    private TrainOperation operation;

    @Column(name = "incident_type", nullable = false, length = 50)
    private String incidentType;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime occurredAt;

    @Column(name = "impact_detail", nullable = false, columnDefinition = "text")
    private String impactDetail;

    @Column(name = "action_detail", columnDefinition = "text")
    private String actionDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private Member createdBy;

    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Member updatedBy;

    // 장애를 접수 상태로 생성하고 최초 등록자와 등록일시를 기록한다.
    public Incident(TrainOperation operation, String type, OffsetDateTime occurredAt,
                    String impact, Member actor, OffsetDateTime now) {
        this.operation = operation;
        this.incidentType = type;
        this.occurredAt = occurredAt;
        this.impactDetail = impact;
        this.status = IncidentStatus.RECEIVED;
        this.createdBy = actor;
        this.createdAt = now;
    }

    // 공통 발급기에서 만든 장애번호를 저장한다.
    public void assignIncidentNumber(String incidentNumber) {
        this.incidentNumber = incidentNumber;
    }

    // 실제 변경된 접수 내용이 있을 때만 수정자와 수정일시를 기록한다.
    public void updateReception(TrainOperation operation, String type, OffsetDateTime occurredAt,
                                String impact, Member actor, OffsetDateTime now) {
        if (this.operation.getId().equals(operation.getId()) && this.incidentType.equals(type)
                && this.occurredAt.isEqual(occurredAt) && this.impactDetail.equals(impact)) {
            return;
        }
        this.operation = operation;
        this.incidentType = type;
        this.occurredAt = occurredAt;
        this.impactDetail = impact;
        modified(actor, now);
    }

    // 접수한 장애의 현장 확인을 시작한다.
    public void check(Member actor, OffsetDateTime now) {
        requireStatus(IncidentStatus.RECEIVED, "접수된 장애만 확인을 시작할 수 있습니다.");
        status = IncidentStatus.CHECKING;
        modified(actor, now);
    }

    // 확인 내용을 저장하고 조치를 시작한다.
    public void start(String action, Member actor, OffsetDateTime now) {
        requireStatus(IncidentStatus.CHECKING, "확인 중인 장애만 조치를 시작할 수 있습니다.");
        saveAction(action, actor, now);
        status = IncidentStatus.IN_PROGRESS;
        modified(actor, now);
    }

    // 진행 단계는 유지하면서 조치 내용만 저장한다.
    public void saveAction(String action, Member actor, OffsetDateTime now) {
        if (status != IncidentStatus.CHECKING && status != IncidentStatus.IN_PROGRESS
                && status != IncidentStatus.VERIFYING) {
            throw new IllegalStateException("확인 또는 조치가 진행 중인 장애만 조치 내용을 저장할 수 있습니다.");
        }
        if (Objects.equals(actionDetail, action)) {
            return;
        }
        actionDetail = action;
        modified(actor, now);
    }

    // 조치 결과를 저장하고 복구 여부 확인을 시작한다.
    public void verifyRecovery(String action, Member actor, OffsetDateTime now) {
        requireStatus(IncidentStatus.IN_PROGRESS, "조치 중인 장애만 복구 확인을 시작할 수 있습니다.");
        requireAction(action);
        saveAction(action, actor, now);
        status = IncidentStatus.VERIFYING;
        modified(actor, now);
    }

    // 복구 확인 중 추가 작업이 필요하면 조치 단계로 돌아간다.
    public void resume(String action, Member actor, OffsetDateTime now) {
        requireStatus(IncidentStatus.VERIFYING, "복구 확인 중인 장애만 추가 조치할 수 있습니다.");
        saveAction(action, actor, now);
        status = IncidentStatus.IN_PROGRESS;
        modified(actor, now);
    }

    // 복구 확인을 마친 장애의 조치를 완료한다.
    public void complete(String action, Member actor, OffsetDateTime now) {
        requireStatus(IncidentStatus.VERIFYING, "복구 확인 중인 장애만 완료 처리할 수 있습니다.");
        requireAction(action);
        saveAction(action, actor, now);
        status = IncidentStatus.COMPLETED;
        modified(actor, now);
    }

    // 복구 확인 및 완료 처리에 필요한 조치 내용을 검사한다.
    private void requireAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("복구 확인 및 완료 처리 전에 조치 내용을 입력해주세요.");
        }
    }

    // 현재 상태에서 요청한 동작을 실행할 수 있는지 검사한다.
    private void requireStatus(IncidentStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    // 변경한 작업자와 시간대가 포함된 수정일시를 기록한다.
    private void modified(Member actor, OffsetDateTime now) {
        updatedBy = actor;
        updatedAt = now;
    }
}
