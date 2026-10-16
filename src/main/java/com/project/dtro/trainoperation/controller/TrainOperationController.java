package com.project.dtro.trainoperation.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.station.domain.Station;
import com.project.dtro.station.repository.StationRepository;
import com.project.dtro.trainoperation.dto.*;
import com.project.dtro.trainoperation.service.TrainOperationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/train-operations")
@PreAuthorize("hasAnyRole('TRAIN_MANAGER', 'ADMIN')")
@RequiredArgsConstructor
public class TrainOperationController {
    private final TrainOperationService service;
    private final StationRepository stations;

    @PostMapping("/search")
    public ApiResponse<OperationPageResponse> search(@RequestBody OperationSearchRequest request) {
        return ApiResponse.ok(service.search(request));
    }

    @PutMapping
    public ApiResponse<List<OperationResponse>> save(@Valid @RequestBody OperationSaveRequest request) {
        return ApiResponse.ok(service.save(request));
    }

    @DeleteMapping
    public ApiResponse<Void> delete(@RequestBody List<Long> ids) {
        service.delete(ids);
        return ApiResponse.ok(null);
    }

    @GetMapping("/lines")
    public ApiResponse<List<String>> lines() {
        return ApiResponse.ok(stations.findLineCodes());
    }

    @GetMapping("/stations")
    public ApiResponse<List<StationOption>> stations(@RequestParam String lineCode,
            @RequestParam(defaultValue = "DOWN") String direction) {
        if (!List.of("UP", "DOWN").contains(direction))
            throw new IllegalArgumentException("방향 조건이 올바르지 않습니다.");
        List<Station> ordered = "UP".equals(direction)
                ? stations.findByLineCodeOrderByStationOrderDesc(lineCode)
                : stations.findByLineCodeOrderByStationOrderAsc(lineCode);
        return ApiResponse.ok(ordered.stream().map(StationOption::from).toList());
    }

    public record StationOption(String stationCode, String stationName, int stationOrder) {
        static StationOption from(Station station) {
            return new StationOption(station.getStationCode(), station.getStationName(), station.getStationOrder());
        }
    }
}
