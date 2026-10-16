package com.project.dtro.train.dto;

import com.project.dtro.train.domain.Train;
import com.project.dtro.train.domain.TrainStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TrainResponse {
    private final Long id;
    private final String trainNumber;
    private final String formationName;
    private final String carType;
    private final Integer carCount;
    private final TrainStatus status;
    private final LocalDate introducedOn;
    private final LocalDate lastInspectedOn;
    private final LocalDateTime createdAt;
    private final String createdByName;
    private final LocalDateTime updatedAt;
    private final String updatedByName;

    // 열차 엔티티를 상세 응답 데이터로 변환한다.
    public static TrainResponse from(Train train) {
        return new TrainResponse(
                train.getId(),
                train.getTrainNumber(),
                train.getFormationName(),
                train.getCarType(),
                train.getCarCount(),
                train.getStatus(),
                train.getIntroducedOn(),
                train.getLastInspectedOn(),
                train.getCreatedAt(),
                train.getCreatedByName(),
                train.getUpdatedAt(),
                train.getUpdatedByName()
        );
    }
}
