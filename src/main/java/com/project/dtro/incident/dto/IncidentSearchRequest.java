package com.project.dtro.incident.dto;

import com.project.dtro.incident.domain.IncidentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IncidentSearchRequest {
    private String incidentType;
    private IncidentStatus status;
    private Long operationId;
    private int page = 0;
    private int size = 20;
    private String sort;
}
