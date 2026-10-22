package com.project.dtro.trainoperation.dto;

import com.project.dtro.trainoperation.domain.TrainOperation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class OperationResponse {
    private final Long id;
    private final String operationNumber;
    private final Long trainId;
    private final String trainNumber;
    private final String lineCode;
    private final String direction;
    private final String departureStationCode;
    private final String departureStationName;
    private final String arrivalStationCode;
    private final String arrivalStationName;
    private final LocalTime firstTrainTime;
    private final LocalTime lastTrainTime;
    private final LocalDateTime createdAt;
    private final String createdByName;
    private final LocalDateTime updatedAt;
    private final String updatedByName;

    // 운행정보 엔티티를 상세 응답 데이터로 변환한다.
    public static OperationResponse from(TrainOperation operation) {
        var departure = operation.getDepartureStation();
        var arrival = operation.getArrivalStation();
        return new OperationResponse(
                operation.getId(),
                operation.getOperationNumber(),
                operation.getTrain().getId(),
                operation.getTrain().getTrainNumber(),
                operation.getLineCode(),
                departure.getStationOrder() < arrival.getStationOrder() ? "DOWN" : "UP",
                departure.getStationCode(),
                departure.getStationName(),
                arrival.getStationCode(),
                arrival.getStationName(),
                operation.getFirstTrainTime(),
                operation.getLastTrainTime(),
                operation.getCreatedAt(),
                operation.getCreatedByName(),
                operation.getUpdatedAt(),
                operation.getUpdatedByName()
        );
    }
}
