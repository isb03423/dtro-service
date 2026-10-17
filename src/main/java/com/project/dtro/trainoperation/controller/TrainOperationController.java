package com.project.dtro.trainoperation.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.station.dto.StationOptionResponse;
import com.project.dtro.trainoperation.dto.OperationPageResponse;
import com.project.dtro.trainoperation.dto.OperationResponse;
import com.project.dtro.trainoperation.dto.OperationSaveRequest;
import com.project.dtro.trainoperation.dto.OperationSearchRequest;
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

    @PostMapping("/search")
    // 검색 조건으로 운행정보 목록을 조회한다.
    public ApiResponse<OperationPageResponse> search(@Valid @RequestBody OperationSearchRequest request) {
        return ApiResponse.ok(service.search(request));
    }

    @PutMapping
    // 추가·수정한 운행정보 행을 일괄 저장한다.
    public ApiResponse<List<OperationResponse>> save(@Valid @RequestBody OperationSaveRequest request) {
        return ApiResponse.ok(service.save(request));
    }

    @DeleteMapping
    // 선택한 운행정보를 일괄 삭제한다.
    public ApiResponse<Void> delete(@RequestBody List<Long> ids) {
        service.delete(ids);
        return ApiResponse.ok(null);
    }

    @GetMapping("/lines")
    // 운행정보에서 선택할 노선 목록을 반환한다.
    public ApiResponse<List<String>> lines() {
        return ApiResponse.ok(service.lines());
    }

    @GetMapping("/stations")
    // 노선과 운행 방향에 맞는 역 목록을 반환한다.
    public ApiResponse<List<StationOptionResponse>> stations(@RequestParam String lineCode,
                                                             @RequestParam(defaultValue = "DOWN") String direction) {
        return ApiResponse.ok(service.stations(lineCode, direction));
    }
}
