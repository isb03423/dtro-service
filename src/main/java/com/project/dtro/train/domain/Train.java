package com.project.dtro.train.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trains")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "train_id_seq")
    @SequenceGenerator(name = "train_id_seq", sequenceName = "train_id_seq", allocationSize = 1)
    @Column(name = "train_id")
    private Long id;

    @Column(name = "train_number", nullable = false, unique = true, length = 30)
    private String trainNumber;

    @Column(name = "formation_name", nullable = false, length = 100)
    private String formationName;

    @Column(name = "line_name", nullable = false, length = 30)
    private String lineName;

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

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    // 등록할 열차의 기본 정보를 초기화한다.
    public Train(String formationName, String lineName, String carType,
                 Integer carCount, TrainStatus status, LocalDate introducedOn,
                 LocalDate lastInspectedOn) {
        update(formationName, lineName, carType, carCount, status, introducedOn, lastInspectedOn);
    }

    // 호선별 발급기에서 만든 열차번호를 저장한다.
    public void assignTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    // 수정 가능한 열차 정보를 새 값으로 바꾼다.
    public void update(String formationName, String lineName, String carType, Integer carCount,
                       TrainStatus status, LocalDate introducedOn, LocalDate lastInspectedOn) {
        this.formationName = formationName;
        this.lineName = lineName;
        this.carType = carType;
        this.carCount = carCount;
        this.status = status;
        this.introducedOn = introducedOn;
        this.lastInspectedOn = lastInspectedOn;
    }

}
