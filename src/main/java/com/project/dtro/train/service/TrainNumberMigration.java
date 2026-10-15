package com.project.dtro.train.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TrainNumberMigration implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final TrainNumberGenerator numberGenerator;

    // 서버 시작 시 기존 열차번호를 호선별 번호 체계로 한 번 변환한다.
    @Override
    public void run(ApplicationArguments args) {
        transactions.executeWithoutResult(status -> migrate());
    }

    // 기존 번호만 열차 ID 순서로 변경하고 이미 새 규칙인 번호는 유지한다.
    void migrate() {
        // 번호 변경 중 다른 요청이 열차나 순번을 수정하지 못하도록 잠근다.
        jdbc.execute("LOCK TABLE trains, train_number_counters IN ACCESS EXCLUSIVE MODE");
        List<LegacyTrain> oldTrains = jdbc.query("""
                SELECT train_id, line_name FROM trains
                WHERE line_name IN ('1호선', '2호선', '3호선')
                  AND train_number !~ ('^TR-' || left(line_name, 1) || '[0-9]{3,}$')
                ORDER BY line_name, train_id
                """, (result, index) -> new LegacyTrain(result.getLong("train_id"), result.getString("line_name")));

        // 발급기가 순번 테이블을 갱신하므로 삭제한 번호를 다시 사용하지 않는다.
        for (LegacyTrain train : oldTrains) {
            jdbc.update("UPDATE trains SET train_number = ? WHERE train_id = ?",
                    numberGenerator.nextNumber(train.lineName()), train.id());
        }
    }

    private record LegacyTrain(long id, String lineName) {
    }
}
