package com.project.dtro.customerservice.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.station.dto.StationOptionResponse;
import com.project.dtro.trainoperation.service.TrainOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer-service-options")
@PreAuthorize("hasAnyRole('USER', 'TRAIN_MANAGER', 'ADMIN')")
@RequiredArgsConstructor
public class CustomerServiceOptionsController {
    private final TrainOperationService service;

    // 고객 서비스 글 등록에 사용할 노선만 조회한다.
    @GetMapping("/lines")
    public ApiResponse<List<String>> lines() {
        return ApiResponse.ok(service.lines());
    }

    // 선택한 노선의 역을 순서대로 반환한다.
    @GetMapping("/stations")
    public ApiResponse<List<StationOptionResponse>> stations(@RequestParam String lineCode) {
        return ApiResponse.ok(service.stations(lineCode, "DOWN"));
    }
}
