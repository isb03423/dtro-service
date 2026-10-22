package com.project.dtro.auth.controller;

import com.project.dtro.common.response.ApiResponse;
import com.project.dtro.user.domain.Member;
import com.project.dtro.user.repository.MemberRepository;
import com.project.dtro.user.dto.MemberResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberRepository members;

    // 요청에 발급된 CSRF 토큰과 헤더 이름을 반환한다.
    @GetMapping("/csrf")
    public ApiResponse<CsrfResponse> csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CSRF 토큰을 발급할 수 없습니다.");
        }
        return ApiResponse.ok(new CsrfResponse(token.getHeaderName(), token.getToken()));
    }

    // 인증된 사용자의 계정 정보를 반환한다.
    @GetMapping("/me")
    public ApiResponse<MemberResponse> me(Authentication authentication) {
        Member member = members.findByUserid(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return ApiResponse.ok(MemberResponse.from(member));
    }

    @Getter
    @AllArgsConstructor
    public static class CsrfResponse {
        private final String headerName;
        private final String token;
    }

}
