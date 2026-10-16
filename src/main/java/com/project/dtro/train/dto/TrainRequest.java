package com.project.dtro.train.dto;

import com.project.dtro.train.domain.TrainStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TrainRequest {
    @NotBlank
    @Size(max = 100)
    private String formationName;

    @NotBlank
    @Size(max = 50)
    private String carType;

    @NotNull
    @Min(1)
    private Integer carCount;

    @NotNull
    private TrainStatus status;

    @NotNull
    private LocalDate introducedOn;

    @NotNull
    private LocalDate lastInspectedOn;
}
