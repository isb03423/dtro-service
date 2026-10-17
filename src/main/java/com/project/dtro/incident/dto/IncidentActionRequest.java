package com.project.dtro.incident.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IncidentActionRequest {
    @NotNull
    @PositiveOrZero
    private Long version;

    private String actionDetail;

    @Size(max = 500)
    private String reason;
}
