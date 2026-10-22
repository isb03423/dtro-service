package com.project.dtro.user.service;

import com.project.dtro.user.domain.Member;
import com.project.dtro.user.domain.MemberRole;
import com.project.dtro.user.dto.MemberPasswordRequest;
import com.project.dtro.user.dto.MemberRegisterRequest;
import com.project.dtro.user.dto.MemberResponse;
import com.project.dtro.user.dto.MemberUpdateRequest;
import com.project.dtro.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository members;
    private final PasswordEncoder passwords;

    @Transactional
    public MemberResponse register(MemberRegisterRequest request) {
        String userid = request.userid().trim();
        if (members.existsByUserid(userid)) {
            throw new MemberConflictException();
        }
        Member member = Member.builder()
                .userid(userid)
                .password(passwords.encode(request.password()))
                .name(request.name().trim())
                .email(request.email().trim())
                .phone(request.phone().trim())
                .role(MemberRole.USER)
                .build();
        return MemberResponse.from(members.save(member));
    }

    @Transactional(readOnly = true)
    public MemberResponse me(String userid) {
        return MemberResponse.from(findCurrent(userid));
    }

    @Transactional
    public MemberResponse update(String userid, MemberUpdateRequest request) {
        Member member = findCurrent(userid);
        member.updateProfile(request.name().trim(), request.email().trim(), request.phone().trim());
        return MemberResponse.from(member);
    }

    @Transactional
    public void changePassword(String userid, MemberPasswordRequest request) {
        Member member = findCurrent(userid);
        if (!passwords.matches(request.currentPassword(), member.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }
        member.changePassword(passwords.encode(request.newPassword()));
    }

    private Member findCurrent(String userid) {
        return members.findByUserid(userid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
