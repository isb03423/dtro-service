package com.project.dtro.trainoperation.domain;

import com.project.dtro.common.domain.AuditEntity;
import com.project.dtro.station.domain.Station;
import com.project.dtro.train.domain.Train;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "train_operation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainOperation extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_id")
    private Long id;

    @Column(name = "operation_number", unique = true, length = 30)
    private String operationNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(name = "line_code", nullable = false, length = 20)
    private String lineCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departure_station", referencedColumnName = "station_code", nullable = false)
    private Station departureStation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arrival_station", referencedColumnName = "station_code", nullable = false)
    private Station arrivalStation;

    @Column(name = "first_train_time", nullable = false)
    private LocalTime firstTrainTime;

    @Column(name = "last_train_time", nullable = false)
    private LocalTime lastTrainTime;

    public TrainOperation(Train train, String lineCode, Station departureStation,
                          Station arrivalStation, LocalTime firstTrainTime, LocalTime lastTrainTime) {
        update(train, lineCode, departureStation, arrivalStation, firstTrainTime, lastTrainTime);
    }

    public void assignOperationNumber(String operationNumber) {
        this.operationNumber = operationNumber;
    }

    public void update(Train train, String lineCode, Station departureStation,
                       Station arrivalStation, LocalTime firstTrainTime, LocalTime lastTrainTime) {
        this.train = train;
        this.lineCode = lineCode;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.firstTrainTime = firstTrainTime;
        this.lastTrainTime = lastTrainTime;
    }
}
