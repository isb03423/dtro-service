package com.project.dtro.incident.dto;

import com.project.dtro.incident.domain.IncidentStatus;
import com.project.dtro.incident.domain.IncidentStatusHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class IncidentHistoryResponse {
    private final Long id;
    private final Long incidentId;
    private final IncidentStatus previousStatus;
    private final IncidentStatus newStatus;
    private final String reason;
    private final String actionDetail;
    private final Long changedBy;
    private final String changedByName;
    private final OffsetDateTime changedAt;

    // 상태 변경 이력을 응답 데이터로 변환한다.
    public static IncidentHistoryResponse from(IncidentStatusHistory history) {
        return new IncidentHistoryResponse(
                history.getId(),
                history.getIncident().getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getReason(),
                history.getActionDetail(),
                history.getChangedBy().getId(),
                history.getChangedBy().getName(),
                history.getChangedAt()
        );
    }
}
