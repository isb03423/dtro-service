package com.project.dtro.train.domain;

import com.project.dtro.common.domain.AuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "trains")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Train extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "train_id")
    private Long id;

    @Column(name = "train_number", nullable = false, unique = true, length = 30)
    private String trainNumber;

    @Column(name = "formation_name", nullable = false, length = 100)
    private String formationName;

    @Column(name = "car_type", nullable = false, length = 50)
    private String carType;

    @Column(name = "car_count", nullable = false)
    private Integer carCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TrainStatus status;

    @Column(name = "introduced_on", nullable = false)
    private LocalDate introducedOn;

    @Column(name = "last_inspected_on", nullable = false)
    private LocalDate lastInspectedOn;

    // 등록할 열차의 기본 정보를 초기화한다.
    public Train(String formationName, String carType,
                 Integer carCount, TrainStatus status, LocalDate introducedOn,
                 LocalDate lastInspectedOn) {
        update(formationName, carType, carCount, status, introducedOn, lastInspectedOn);
    }

    // 공통 발급기에서 만든 열차번호를 저장한다.
    public void assignTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    // 수정 가능한 열차 정보를 새 값으로 바꾼다.
    public void update(String formationName, String carType, Integer carCount,
                       TrainStatus status, LocalDate introducedOn, LocalDate lastInspectedOn) {
        this.formationName = formationName;
        this.carType = carType;
        this.carCount = carCount;
        this.status = status;
        this.introducedOn = introducedOn;
        this.lastInspectedOn = lastInspectedOn;
    }

}
