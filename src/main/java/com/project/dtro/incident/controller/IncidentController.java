package com.project.dtro.incident.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.incident.dto.IncidentActionRequest;
import com.project.dtro.incident.dto.IncidentDeleteRequest;
import com.project.dtro.incident.dto.IncidentHistoryResponse;
import com.project.dtro.incident.dto.IncidentPageResponse;
import com.project.dtro.incident.dto.IncidentResponse;
import com.project.dtro.incident.dto.IncidentSaveRequest;
import com.project.dtro.incident.dto.IncidentSearchRequest;
import com.project.dtro.incident.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@PreAuthorize("hasAnyRole('TRAIN_MANAGER', 'ADMIN')")
@RequiredArgsConstructor
public class IncidentController {
    private final IncidentService service;

    @PostMapping("/search")
    // 검색 조건으로 운행 장애 목록을 조회한다.
    public ApiResponse<IncidentPageResponse> search(@Valid @RequestBody IncidentSearchRequest request) {
        return ApiResponse.ok(service.search(request));
    }

    @GetMapping("/{id}")
    // 장애 ID에 해당하는 접수 및 조치 내용을 반환한다.
    public ApiResponse<IncidentResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @GetMapping("/{id}/history")
    // 해당 장애의 상태 변경 이력을 최신순으로 반환한다.
    public ApiResponse<List<IncidentHistoryResponse>> history(@PathVariable Long id) {
        return ApiResponse.ok(service.history(id));
    }

    @PostMapping
    // 운행 장애를 접수하고 최초 상태 이력을 생성한다.
    public ApiResponse<IncidentResponse> create(@Valid @RequestBody IncidentSaveRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/{id}")
    // 지정한 장애의 접수 내용을 수정한다.
    public ApiResponse<IncidentResponse> update(@PathVariable Long id, @Valid @RequestBody IncidentSaveRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @PostMapping("/{id}/check")
    // 접수한 장애의 확인을 시작한다.
    public ApiResponse<IncidentResponse> check(@PathVariable Long id, @Valid @RequestBody IncidentActionRequest request) {
        return ApiResponse.ok(service.action(id, "check", request));
    }

    @PostMapping("/{id}/start")
    // 확인을 마친 장애의 조치를 시작한다.
    public ApiResponse<IncidentResponse> start(@PathVariable Long id, @Valid @RequestBody IncidentActionRequest request) {
        return ApiResponse.ok(service.action(id, "start", request));
    }

    @PutMapping("/{id}/action")
    // 현재 상태를 유지하면서 조치 내용만 저장한다.
    public ApiResponse<IncidentResponse> saveAction(@PathVariable Long id, @Valid @RequestBody IncidentActionRequest request) {
        return ApiResponse.ok(service.action(id, "save", request));
    }

    @PostMapping("/{id}/verify")
    // 조치 결과를 저장하고 복구 여부 확인을 시작한다.
    public ApiResponse<IncidentResponse> verifyRecovery(@PathVariable Long id, @Valid @RequestBody IncidentActionRequest request) {
        return ApiResponse.ok(service.action(id, "verify", request));
    }

    @PostMapping("/{id}/resume")
    // 복구 확인 중인 장애를 추가 조치 단계로 돌린다.
    public ApiResponse<IncidentResponse> resume(@PathVariable Long id, @Valid @RequestBody IncidentActionRequest request) {
        return ApiResponse.ok(service.action(id, "resume", request));
    }

    @PostMapping("/{id}/complete")
    // 복구 확인을 마친 장애를 완료 처리한다.
    public ApiResponse<IncidentResponse> complete(@PathVariable Long id, @Valid @RequestBody IncidentActionRequest request) {
        return ApiResponse.ok(service.action(id, "complete", request));
    }

    @DeleteMapping
    // 선택한 장애와 처리 이력을 일괄 삭제한다.
    public ApiResponse<Void> delete(@RequestBody List<IncidentDeleteRequest> rows) {
        service.delete(rows);
        return ApiResponse.ok(null);
    }
}
