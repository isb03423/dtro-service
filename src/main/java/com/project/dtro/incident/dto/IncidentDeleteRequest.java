package com.project.dtro.incident.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IncidentDeleteRequest {
    @NotNull
    @Positive
    private Long id;

    @NotNull
    @PositiveOrZero
    private Long version;
}
