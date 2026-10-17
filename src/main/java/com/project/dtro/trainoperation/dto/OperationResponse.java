package com.project.dtro.trainoperation.dto;

import com.project.dtro.trainoperation.domain.TrainOperation;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record OperationResponse(
        Long id, String operationNumber, Long trainId, String trainNumber, String lineCode, String direction,
        String departureStationCode, String departureStationName,
        String arrivalStationCode, String arrivalStationName,
        LocalTime firstTrainTime, LocalTime lastTrainTime,
        LocalDateTime createdAt, String createdByName,
        LocalDateTime updatedAt, String updatedByName) {

    public static OperationResponse from(TrainOperation operation) {
        var departure = operation.getDepartureStation();
        var arrival = operation.getArrivalStation();
        return new OperationResponse(
                operation.getId(), operation.getOperationNumber(), operation.getTrain().getId(), operation.getTrain().getTrainNumber(),
                operation.getLineCode(), departure.getStationOrder() < arrival.getStationOrder() ? "DOWN" : "UP",
                departure.getStationCode(), departure.getStationName(),
                arrival.getStationCode(), arrival.getStationName(),
                operation.getFirstTrainTime(), operation.getLastTrainTime(),
                operation.getCreatedAt(), operation.getCreatedByName(),
                operation.getUpdatedAt(), operation.getUpdatedByName());
    }
}
