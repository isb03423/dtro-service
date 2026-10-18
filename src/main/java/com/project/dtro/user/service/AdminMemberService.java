package com.project.dtro.user.service;

import com.project.dtro.user.domain.Member;
import com.project.dtro.user.domain.MemberRole;
import com.project.dtro.user.dto.AdminMemberPageResponse;
import com.project.dtro.user.dto.AdminMemberCreateRequest;
import com.project.dtro.user.dto.AdminMemberCreateResponse;
import com.project.dtro.user.dto.AdminMemberResponse;
import com.project.dtro.user.dto.AdminMemberUpdateRequest;
import com.project.dtro.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminMemberService {
    private static final Set<String> SORT_FIELDS = Set.of("id", "userid", "name", "role", "enabled", "createdAt", "updatedAt");
    private final MemberRepository members;
    private final PasswordEncoder passwords;
    private final SecureRandom random = new SecureRandom();

    @Transactional(readOnly = true)
    public AdminMemberPageResponse search(String keyword, MemberRole role, Boolean enabled, int page, int size, String sort) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 조건이 올바르지 않습니다.");
        }
        String[] parts = sort.split(",", -1);
        if (parts.length != 2 || !SORT_FIELDS.contains(parts[0])
                || !(parts[1].equalsIgnoreCase("asc") || parts[1].equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("정렬 조건이 올바르지 않습니다.");
        }
        String term = keyword == null || keyword.isBlank() ? null : keyword.trim().toLowerCase(Locale.ROOT);
        Specification<Member> filter = (root, query, cb) -> {
            var conditions = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (role != null) conditions.add(cb.equal(root.get("role"), role));
            if (enabled != null) conditions.add(cb.equal(root.get("enabled"), enabled));
            if (term != null) {
                String pattern = "%" + term + "%";
                conditions.add(cb.or(cb.like(cb.lower(root.get("userid")), pattern),
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)));
            }
            return cb.and(conditions.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(parts[1]), parts[0]));
        return AdminMemberPageResponse.from(members.findAll(filter, pageable));
    }

    @Transactional(readOnly = true)
    public AdminMemberResponse get(Long id) {
        return AdminMemberResponse.from(find(id));
    }

    @Transactional
    public AdminMemberResponse update(Long id, Long actorId, AdminMemberUpdateRequest request) {
        Member member = find(id);
        if (id.equals(actorId) && (request.role() != MemberRole.ADMIN || !request.enabled())) {
            throw new IllegalArgumentException("본인의 관리자 권한 또는 사용 상태는 변경할 수 없습니다.");
        }
        member.updateByAdmin(request.name().trim(), blankToNull(request.email()),
                blankToNull(request.phone()), request.role(), request.enabled());
        members.flush();
        return AdminMemberResponse.from(member);
    }

    @Transactional
    public AdminMemberCreateResponse create(AdminMemberCreateRequest request) {
        String userid = request.userid().trim();
        if (userid.length() < 4 || userid.length() > 50) {
            throw new IllegalArgumentException("아이디는 4~50자로 입력해주세요.");
        }
        if (members.existsByUserid(userid)) {
            throw new MemberConflictException();
        }
        String temporaryPassword = newTemporaryPassword();
        Member member = members.save(Member.builder()
                .userid(userid)
                .password(passwords.encode(temporaryPassword))
                .name(request.name().trim())
                .email(blankToNull(request.email()))
                .phone(blankToNull(request.phone()))
                .role(request.role())
                .enabled(request.enabled())
                .build());
        members.flush();
        return new AdminMemberCreateResponse(AdminMemberResponse.from(member), temporaryPassword);
    }

    @Transactional
    public String resetPassword(Long id, Long actorId) {
        if (id.equals(actorId)) {
            throw new IllegalArgumentException("본인 비밀번호는 내 정보 화면에서 변경해주세요.");
        }
        Member member = find(id);
        String temporaryPassword = newTemporaryPassword();
        member.changePassword(passwords.encode(temporaryPassword));
        members.flush();
        return temporaryPassword;
    }

    private String newTemporaryPassword() {
        byte[] bytes = new byte[18];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Member find(Long id) {
        return members.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
