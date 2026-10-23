package com.project.dtro.train.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.train.dto.TrainPageResponse;
import com.project.dtro.train.dto.TrainRequest;
import com.project.dtro.train.dto.TrainResponse;
import com.project.dtro.train.dto.TrainSaveRequest;
import com.project.dtro.train.dto.TrainSearchRequest;
import com.project.dtro.train.service.TrainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/trains")
@PreAuthorize("hasAnyRole('TRAIN_MANAGER', 'ADMIN')")
@RequiredArgsConstructor
public class TrainController {
    private final TrainService service;

    @GetMapping("/search")
    // 검색 조건으로 열차 목록을 조회한다.
    public ApiResponse<TrainPageResponse> search(@Valid @ModelAttribute TrainSearchRequest request) {
        return ApiResponse.ok(
                service.search(
                        request.getStatus(),
                        request.getKeyword(),
                        request.getPage(),
                        request.getSize(),
                        request.getSort()
                )
        );
    }

    @GetMapping("/{trainId}")
    // 열차 ID에 해당하는 상세 정보를 반환한다.
    public ApiResponse<TrainResponse> get(@PathVariable Long trainId) {
        return ApiResponse.ok(service.get(trainId));
    }

    @PostMapping
    // 새 열차를 등록하고 생성된 정보를 반환한다.
    public ResponseEntity<ApiResponse<TrainResponse>> create(@Valid @RequestBody TrainRequest request) {
        TrainResponse train = service.create(request);
        return ResponseEntity.created(URI.create("/api/trains/" + train.getId()))
                .body(ApiResponse.ok(train));
    }

    @PutMapping
    // 목록에서 추가·수정한 열차 행을 일괄 저장한다.
    public ApiResponse<List<TrainResponse>> save(@Valid @RequestBody TrainSaveRequest request) {
        return ApiResponse.ok(service.save(request));
    }

    @DeleteMapping
    // 목록에서 선택한 열차를 일괄 삭제한다.
    public ApiResponse<Void> delete(@RequestBody List<Long> ids) {
        service.delete(ids);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{trainId}")
    // 지정한 열차의 정보를 수정한다.
    public ApiResponse<TrainResponse> update(@PathVariable Long trainId, @Valid @RequestBody TrainRequest request) {
        return ApiResponse.ok(service.update(trainId, request));
    }

    @DeleteMapping("/{trainId}")
    // 지정한 열차를 삭제하고 빈 응답을 반환한다.
    public ResponseEntity<Void> delete(@PathVariable Long trainId) {
        service.delete(trainId);
        return ResponseEntity.noContent().build();
    }
}
