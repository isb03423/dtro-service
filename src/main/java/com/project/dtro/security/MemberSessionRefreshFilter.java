package com.project.dtro.security;

import com.project.dtro.user.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class MemberSessionRefreshFilter extends OncePerRequestFilter {
    private final MemberRepository members;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof MemberPrincipal principal) {
            var member = members.findById(principal.getMemberId());
            if (member.isEmpty() || !member.get().isEnabled()
                    || !member.get().getPassword().equals(principal.getSessionPasswordHash())) {
                SecurityContextHolder.clearContext();
                var session = request.getSession(false);
                if (session != null) session.invalidate();
            } else {
                var current = member.get();
                var authority = new SimpleGrantedAuthority("ROLE_" + current.getRole().name());
                var refreshed = new MemberPrincipal(current.getId(), current.getUserid(),
                        current.getPassword(), true, List.of(authority));
                var updated = UsernamePasswordAuthenticationToken.authenticated(refreshed,
                        authentication.getCredentials(), refreshed.getAuthorities());
                updated.setDetails(authentication.getDetails());
                SecurityContextHolder.getContext().setAuthentication(updated);
            }
        }
        chain.doFilter(request, response);
    }
}
