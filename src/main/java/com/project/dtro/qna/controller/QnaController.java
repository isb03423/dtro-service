package com.project.dtro.qna.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.qna.dto.*;
import com.project.dtro.qna.service.QnaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/qnas")
@RequiredArgsConstructor
public class QnaController {
    private final QnaService qnaService;

    @GetMapping("")
    public ApiResponse<QnaPageResponse> search(@Valid @ModelAttribute QnaSearchRequest request) {
        return ApiResponse.ok(
                qnaService.search(
                        request.getQnaType(),
                        request.getQnaStatus(),
                        request.getKeyword(),
                        request.getPage(),
                        request.getSize(),
                        request.getSort()
                )
        );
    }

    @PreAuthorize("hasAnyRole('USER', 'TRAIN_MANAGER') and !hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<QnaResponse>> create(@Valid @RequestBody QnaRequest request) {
        QnaResponse qna = qnaService.create(request);
        return ResponseEntity
                .created(URI.create("/api/qnas/" + qna.getId()))
                .body(ApiResponse.ok(qna));
    }

    @GetMapping("/{id}")
    public ApiResponse<QnaResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(qnaService.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<QnaResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody QnaRequest request
    ) {
        return ApiResponse.ok(qnaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        qnaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ApiResponse<QnaResponse> answer(
            @PathVariable Long id,
            @Valid @RequestBody QnaAnswerRequest request
    ) {
        return ApiResponse.ok(
                qnaService.answer(id, request)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/answer")
    public ApiResponse<QnaResponse> updateAnswer(
            @PathVariable Long id,
            @Valid @RequestBody QnaAnswerRequest request
    ) {
        return ApiResponse.ok(qnaService.updateAnswer(id, request));
    }
}
