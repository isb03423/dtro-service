package com.project.dtro.trainoperation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class OperationRowRequest {
    private Long id;
    @NotNull private Long trainId;
    @NotBlank private String lineCode;
    @NotBlank private String departureStationCode;
    @NotBlank private String arrivalStationCode;
    @NotNull private LocalTime firstTrainTime;
    @NotNull private LocalTime lastTrainTime;
}
