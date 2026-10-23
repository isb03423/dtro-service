package com.project.dtro.complaint.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.complaint.dto.*;
import com.project.dtro.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {
    private final ComplaintService complaintService;

    @GetMapping("")
    public ApiResponse<ComplaintPageResponse> search(@Valid @ModelAttribute ComplaintSearchRequest request) {
        return ApiResponse.ok(
                complaintService.search(
                        request.getComplaintType(),
                        request.getComplaintStatus(),
                        request.getKeyword(),
                        request.getPage(),
                        request.getSize(),
                        request.getSort()
                )
        );
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<ComplaintResponse>> create(@Valid @RequestBody ComplaintRequest request) {
        ComplaintResponse complaint = complaintService.create(request);
        return ResponseEntity
                .created(URI.create("/api/complaints/" + complaint.getId()))
                .body(ApiResponse.ok(complaint));
    }

    @GetMapping("/{id}")
    public ApiResponse<ComplaintResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(complaintService.get(id));
    }

    @PreAuthorize("isAuthenticated() and !hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<ComplaintResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintRequest request
    ) {
        return ApiResponse.ok(complaintService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        complaintService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ApiResponse<ComplaintResponse> answer(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintAnswerRequest request
    ) {
        return ApiResponse.ok(
                complaintService.answer(id, request)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/answer")
    public ApiResponse<ComplaintResponse> updateAnswer(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintAnswerRequest request
    ) {
        return ApiResponse.ok(complaintService.updateAnswer(id, request));
    }
}
