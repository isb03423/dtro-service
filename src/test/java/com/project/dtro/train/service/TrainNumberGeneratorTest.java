package com.project.dtro.train.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainNumberGeneratorTest {
    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private TrainNumberGenerator generator;

    @Test
    // 각 호선의 첫 순번을 요청한 번호 형식으로 바꾸는지 확인한다.
    void givenFirstSequence_whenGenerate_thenStartAtLineSpecificNumber() {
        when(jdbc.queryForObject(anyString(), eq(Long.class), anyString())).thenReturn(1L);

        assertEquals("TR-1001", generator.nextNumber("1호선"));
        assertEquals("TR-2001", generator.nextNumber("2호선"));
        assertEquals("TR-3001", generator.nextNumber("3호선"));
    }

    @Test
    // 순번이 999를 넘더라도 다른 호선의 접두어와 겹치지 않는지 확인한다.
    void givenLargeSequence_whenGenerate_thenKeepLinePrefix() {
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq("1호선"))).thenReturn(1000L);

        assertEquals("TR-11000", generator.nextNumber("1호선"));
    }

    @Test
    // 번호 규칙에 없는 호선은 발급하지 않는지 확인한다.
    void givenUnsupportedLine_whenGenerate_thenReject() {
        assertThrows(IllegalArgumentException.class, () -> generator.nextNumber("4호선"));
    }
}
