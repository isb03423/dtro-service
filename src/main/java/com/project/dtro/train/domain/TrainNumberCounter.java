package com.project.dtro.train.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 호선마다 마지막으로 발급한 순번을 DB에 보관한다.
@Entity
@Table(name = "train_number_counters")
public class TrainNumberCounter {
    @Id
    @Column(name = "line_name", nullable = false, length = 30)
    private String lineName;

    @Column(name = "last_value", nullable = false)
    private Long lastValue;

    // JPA가 DB 행을 객체로 읽을 때 사용하는 기본 생성자다.
    protected TrainNumberCounter() {
    }
}
