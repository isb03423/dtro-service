package com.project.dtro.incident.dto;

import com.project.dtro.incident.domain.Incident;
import com.project.dtro.incident.domain.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class IncidentResponse {
    private final Long id;
    private final String incidentNumber;
    private final Long operationId;
    private final String operationNumber;
    private final String trainNumber;
    private final String lineCode;
    private final String incidentType;
    private final OffsetDateTime occurredAt;
    private final String impactDetail;
    private final String actionDetail;
    private final IncidentStatus status;
    private final Long version;
    private final OffsetDateTime createdAt;
    private final Long createdBy;
    private final String createdByName;
    private final OffsetDateTime updatedAt;
    private final Long updatedBy;
    private final String updatedByName;

    // 운행 장애 엔티티를 상세 응답 데이터로 변환한다.
    public static IncidentResponse from(Incident incident) {
        var operation = incident.getOperation();
        var updater = incident.getUpdatedBy();
        return new IncidentResponse(
                incident.getId(),
                incident.getIncidentNumber(),
                operation.getId(),
                operation.getOperationNumber(),
                operation.getTrain().getTrainNumber(),
                operation.getLineCode(),
                incident.getIncidentType(),
                incident.getOccurredAt(),
                incident.getImpactDetail(),
                incident.getActionDetail(),
                incident.getStatus(),
                incident.getVersion(),
                incident.getCreatedAt(),
                incident.getCreatedBy().getId(),
                incident.getCreatedBy().getName(),
                incident.getUpdatedAt(),
                updater == null ? null : updater.getId(),
                updater == null ? null : updater.getName()
        );
    }
}
