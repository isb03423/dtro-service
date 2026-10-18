package com.project.dtro.user.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.user.dto.MemberPasswordRequest;
import com.project.dtro.user.dto.MemberRegisterRequest;
import com.project.dtro.user.dto.MemberResponse;
import com.project.dtro.user.dto.MemberUpdateRequest;
import com.project.dtro.user.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService service;

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> register(@Valid @RequestBody MemberRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.register(request)));
    }

    @GetMapping("/me")
    public ApiResponse<MemberResponse> me(Authentication authentication) {
        return ApiResponse.ok(service.me(authentication.getName()));
    }

    @PutMapping("/me")
    public ApiResponse<MemberResponse> update(Authentication authentication, @Valid @RequestBody MemberUpdateRequest request) {
        return ApiResponse.ok(service.update(authentication.getName(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication, @Valid @RequestBody MemberPasswordRequest request) {
        service.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
