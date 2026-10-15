package com.project.dtro.train.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TrainNumberGenerator {
    private final JdbcTemplate jdbc;

    // 호선별 순번을 DB에서 하나 올리고 TR-1001 형태의 번호를 만든다.
    public String nextNumber(String lineName) {
        int line = switch (lineName) {
            case "1호선" -> 1;
            case "2호선" -> 2;
            case "3호선" -> 3;
            default -> throw new IllegalArgumentException("지원하지 않는 호선입니다.");
        };
        String prefix = "TR-" + line;

        // 최초 발급 시 이미 저장된 해당 호선 번호가 있으면 가장 큰 순번부터 이어 간다.
        jdbc.update("""
                INSERT INTO train_number_counters (line_name, last_value)
                VALUES (?, COALESCE((
                    SELECT MAX(CAST(SUBSTRING(train_number FROM ?) AS BIGINT))
                    FROM trains
                    WHERE line_name = ? AND train_number ~ ?
                ), 0))
                ON CONFLICT (line_name) DO NOTHING
                """, lineName, prefix.length() + 1, lineName, "^" + prefix + "[0-9]{3,}$");

        // UPDATE 한 번으로 값을 올려 동시 등록도 서로 다른 번호를 받게 한다.
        Long sequence = jdbc.queryForObject("""
                UPDATE train_number_counters
                SET last_value = last_value + 1
                WHERE line_name = ?
                RETURNING last_value
                """, Long.class, lineName);
        if (sequence == null) {
            throw new IllegalStateException("열차번호를 발급할 수 없습니다.");
        }
        return prefix + String.format(Locale.ROOT, "%03d", sequence);
    }
}
