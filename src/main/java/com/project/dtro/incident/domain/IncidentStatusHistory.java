package com.project.dtro.incident.domain;

import com.project.dtro.user.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "incident_status_history", indexes = @Index(name = "idx_incident_history", columnList = "incident_id,changed_at"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncidentStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private IncidentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private IncidentStatus newStatus;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "action_detail", columnDefinition = "text")
    private String actionDetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private Member changedBy;

    @Column(name = "changed_at", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime changedAt;

    // 상태가 변경된 당시의 조치 내용과 작업자를 이력으로 기록한다.
    public IncidentStatusHistory(Incident incident, IncidentStatus previous, String reason,
                                 Member actor, OffsetDateTime now) {
        this.incident = incident;
        this.previousStatus = previous;
        this.newStatus = incident.getStatus();
        this.reason = reason;
        this.actionDetail = incident.getActionDetail();
        this.changedBy = actor;
        this.changedAt = now;
    }
}
