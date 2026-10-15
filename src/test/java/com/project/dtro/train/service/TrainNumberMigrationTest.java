package com.project.dtro.train.service;

import com.project.dtro.train.domain.Train;
import com.project.dtro.train.domain.TrainStatus;
import com.project.dtro.train.repository.TrainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TrainNumberMigrationTest {
    @Autowired
    private TrainRepository repository;

    @Autowired
    private TrainNumberMigration migration;

    @Autowired
    private TrainNumberGenerator generator;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    // 기존 번호만 새 번호로 바꾸고 이미 발급된 번호는 유지하는지 확인한다.
    void givenLegacyNumber_whenMigrate_thenRenumberOnce() {
        Train valid = new Train("현재 편성", "3호선", "전동차", 6,
                TrainStatus.AVAILABLE, LocalDate.of(2024, 1, 15), LocalDate.of(2026, 10, 2));
        String validNumber = generator.nextNumber("3호선");
        valid.assignTrainNumber(validNumber);
        repository.saveAndFlush(valid);
        Train train = new Train("이전 편성", "3호선", "전동차", 6,
                TrainStatus.AVAILABLE, LocalDate.of(2024, 1, 15), LocalDate.of(2026, 10, 2));
        train.assignTrainNumber("TR-OLD-TEST");
        repository.saveAndFlush(train);

        migration.migrate();

        String actual = jdbc.queryForObject(
                "SELECT train_number FROM trains WHERE train_id = ?", String.class, train.getId());
        assertEquals(Long.parseLong(validNumber.substring(4)) + 1,
                Long.parseLong(actual.substring(4)));
        assertEquals(validNumber, jdbc.queryForObject(
                "SELECT train_number FROM trains WHERE train_id = ?", String.class, valid.getId()));
        Long counter = jdbc.queryForObject(
                "SELECT last_value FROM train_number_counters WHERE line_name = '3호선'", Long.class);
        assertTrue(counter >= Long.parseLong(actual.substring(4)));

        migration.migrate();
        assertEquals(actual, jdbc.queryForObject(
                "SELECT train_number FROM trains WHERE train_id = ?", String.class, train.getId()));
    }
}
