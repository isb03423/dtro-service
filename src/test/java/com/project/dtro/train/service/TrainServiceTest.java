package com.project.dtro.train.service;

import com.project.dtro.train.domain.Train;
import com.project.dtro.train.domain.TrainStatus;
import com.project.dtro.train.dto.TrainRequest;
import com.project.dtro.train.repository.TrainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainServiceTest {
    @Mock
    private TrainRepository repository;

    @Mock
    private TrainNumberGenerator numberGenerator;

    @InjectMocks
    private TrainService service;

    private TrainRequest request;

    @BeforeEach
    // 각 테스트에 사용할 열차 입력값을 준비한다.
    void setUp() {
        request = new TrainRequest();
        request.setFormationName("101편성");
        request.setLineName("1호선");
        request.setCarType("전동차");
        request.setCarCount(6);
        request.setStatus(TrainStatus.AVAILABLE);
        request.setIntroducedOn(LocalDate.of(2024, 1, 15));
        request.setLastInspectedOn(LocalDate.of(2026, 10, 2));
    }

    @Test
    // 열차 등록 후 생성된 열차번호가 응답에 포함되는지 확인한다.
    void givenValidRequest_whenCreate_thenGenerateTrainNumber() {
        when(numberGenerator.nextNumber("1호선")).thenReturn("TR-1001");

        var result = service.create(request);

        assertEquals("TR-1001", result.getTrainNumber());
        assertEquals("101편성", result.getFormationName());
        verify(repository).save(any(Train.class));
        verify(repository).flush();
    }

    @Test
    // 호선을 변경하면 변경된 호선의 번호를 새로 발급하는지 확인한다.
    void givenDifferentLine_whenUpdate_thenGenerateNewLineNumber() {
        Train train = new Train("101편성", "1호선", "전동차", 6,
                TrainStatus.AVAILABLE, LocalDate.of(2024, 1, 15), LocalDate.of(2026, 10, 2));
        train.assignTrainNumber("TR-1001");
        when(repository.findById(1L)).thenReturn(Optional.of(train));
        when(numberGenerator.nextNumber("2호선")).thenReturn("TR-2001");
        request.setLineName("2호선");

        var result = service.update(1L, request);

        assertEquals("2호선", result.getLineName());
        assertEquals("TR-2001", result.getTrainNumber());
    }

    @Test
    // 없는 열차를 조회하면 없음 오류가 발생하는지 확인한다.
    void givenMissingTrain_whenGet_thenThrowNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TrainNotFoundException.class, () -> service.get(99L));
    }

    @Test
    // 잘못된 페이지 크기를 서비스가 거부하는지 확인한다.
    void givenInvalidPageSize_whenSearch_thenReject() {
        assertThrows(IllegalArgumentException.class,
                () -> service.search(null, null, null, 0, 101, "id,asc"));
    }
}
