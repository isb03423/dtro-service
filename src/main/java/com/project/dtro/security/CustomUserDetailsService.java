package com.project.dtro.security;

import com.project.dtro.user.domain.Member;
import com.project.dtro.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
        Member member = memberRepository.findByUserid(userid)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다: " + userid));

        return new MemberPrincipal(member.getId(), member.getUserid(), member.getPassword(),
                member.isEnabled(),
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));
    }
}
