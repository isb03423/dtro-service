package com.project.dtro.common.config;

import com.project.dtro.security.MemberPrincipal;
import com.project.dtro.user.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<Member> {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Member> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 비로그인 또는 익명 사용자
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof MemberPrincipal principal) {
            return Optional.of(entityManager.getReference(Member.class, principal.getMemberId()));
        }

        return Optional.empty();
    }
}
