package com.project.dtro.user.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.security.MemberPrincipal;
import com.project.dtro.user.domain.MemberRole;
import com.project.dtro.user.dto.AdminMemberPageResponse;
import com.project.dtro.user.dto.AdminMemberCreateRequest;
import com.project.dtro.user.dto.AdminMemberCreateResponse;
import com.project.dtro.user.dto.AdminMemberResponse;
import com.project.dtro.user.dto.AdminMemberUpdateRequest;
import com.project.dtro.user.service.AdminMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMemberController {
    private final AdminMemberService service;

    @GetMapping
    public ApiResponse<AdminMemberPageResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MemberRole role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        return ApiResponse.ok(service.search(keyword, role, enabled, page, size, sort));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminMemberCreateResponse>> create(@Valid @RequestBody AdminMemberCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(service.create(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminMemberResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminMemberResponse> update(@PathVariable Long id,
            @AuthenticationPrincipal MemberPrincipal actor,
            @Valid @RequestBody AdminMemberUpdateRequest request) {
        return ApiResponse.ok(service.update(id, actor.getMemberId(), request));
    }

    @PostMapping("/{id}/password-reset")
    public ResponseEntity<ApiResponse<AdminMemberPasswordResetResponse>> resetPassword(
            @PathVariable Long id, @AuthenticationPrincipal MemberPrincipal actor) {
        String temporaryPassword = service.resetPassword(id, actor.getMemberId());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(new AdminMemberPasswordResetResponse(temporaryPassword)));
    }

    public record AdminMemberPasswordResetResponse(String temporaryPassword) {
    }
}
