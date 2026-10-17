package com.project.dtro.incident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class IncidentSaveRequest {
    @NotNull
    @Positive
    private Long operationId;

    @NotBlank
    @Size(max = 50)
    private String incidentType;

    @NotNull
    private OffsetDateTime occurredAt;

    @NotBlank
    private String impactDetail;

    @PositiveOrZero
    private Long version;
}
