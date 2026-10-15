package com.project.dtro.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class MemberPrincipal extends User {
    private final Long memberId;

    // 회원 ID와 로그인 권한 정보를 인증 객체에 보관한다.
    public MemberPrincipal(Long memberId, String username, String password, boolean enabled,
                           Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.memberId = memberId;
    }
}
