package com.project.dtro.common.number;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "number_counters")
@Getter
public class NumberCounter {
    @Id
    @Column(name = "prefix", nullable = false, length = 12)
    private String prefix;

    @Column(name = "last_value", nullable = false)
    private long lastValue;

    // JPA가 DB 행을 객체로 읽을 때 사용하는 기본 생성자다.
    protected NumberCounter() {
    }

    // 번호 종류별 마지막 순번을 저장할 카운터를 만든다.
    public NumberCounter(String prefix, long lastValue) {
        this.prefix = prefix;
        this.lastValue = lastValue;
    }

    // 기존 번호가 더 크면 다음 발급 번호가 겹치지 않도록 순번을 맞춘다.
    public void ensureAtLeast(long value) {
        if (lastValue < value) {
            lastValue = value;
        }
    }

    // 잠긴 카운터의 순번을 하나 올려 반환한다.
    public long nextValue() {
        return ++lastValue;
    }
}
