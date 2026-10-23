package com.project.dtro.trainoperation.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class OperationRowRequest {
    private Long id;

    @NotNull
    private Long trainId;

    @NotBlank
    private String lineCode;

    @NotBlank
    private String departureStationCode;

    @NotBlank
    private String arrivalStationCode;

    @NotNull
    private LocalTime firstTrainTime;

    @NotNull
    private LocalTime lastTrainTime;

    // 도착시각이 출발시각보다 빠른 운행정보의 저장을 막는다.
    @AssertTrue(message = "도착시각은 출발시각보다 빠를 수 없습니다.")
    public boolean isTimeRangeValid() {
        return firstTrainTime == null || lastTrainTime == null || !lastTrainTime.isBefore(firstTrainTime);
    }
}
