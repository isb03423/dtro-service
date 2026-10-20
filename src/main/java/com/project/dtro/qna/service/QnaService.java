package com.project.dtro.qna.service;

import com.project.dtro.qna.domain.Qna;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.domain.QnaType;
import com.project.dtro.qna.dto.QnaAnswerRequest;
import com.project.dtro.qna.dto.QnaPageResponse;
import com.project.dtro.qna.dto.QnaOverviewResponse;
import com.project.dtro.qna.dto.QnaRequest;
import com.project.dtro.qna.dto.QnaResponse;
import com.project.dtro.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import com.project.dtro.security.MemberPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {
    private static final Set<String> SORT_FIELDS =
            Set.of("id", "title", "station", "type", "status", "createdAt", "updatedAt");

    private final QnaRepository qnaRepository;

    // 집계와 최근 5건만 조회하며 전체 목록을 메모리에 적재하지 않는다.
    public QnaOverviewResponse getOverview() {
        Map<QnaStatus, Long> counts = new EnumMap<>(QnaStatus.class);
        for (QnaStatus status : QnaStatus.values()) {
            counts.put(status, qnaRepository.countByStatus(status));
        }
        PageRequest pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        List<QnaOverviewResponse.RecentQna> recent = qnaRepository.findAll(pageable)
                .getContent().stream()
                .map(item -> new QnaOverviewResponse.RecentQna(item.getId(), item.getTitle(),
                        item.getStation(), item.getStatus(), item.getCreatedAt()))
                .toList();
        return new QnaOverviewResponse(Map.copyOf(counts), recent);
    }

    public QnaPageResponse search(
            QnaType type,
            QnaStatus status,
            String keyword,
            int page,
            int size,
            String sort
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 조건이 올바르지 않습니다.");
        }

        if (sort == null) {
            throw new IllegalArgumentException("정렬 조건이 올바르지 않습니다.");
        }

        String[] parts = sort.split(",", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("정렬 조건이 올바르지 않습니다.");
        }

        String field = parts[0].trim();
        String order = parts[1].trim();

        if (!SORT_FIELDS.contains(field)
                || !(order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("정렬 조건이 올바르지 않습니다.");
        }

        Sort.Direction direction = Sort.Direction.fromString(order);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, field));

        return QnaPageResponse.from(
                qnaRepository.search(blankToNull(keyword), status, type, pageable)
        );
    }

    public QnaResponse get(Long id) {
        MemberPrincipal member = currentMember();
        Qna qna = find(id);
        checkReadAccess(qna, member);
        return QnaResponse.from(qna);
    }

    @Transactional
    public QnaResponse create(QnaRequest request) {
        Qna qna = new Qna(
                request.getTitle().trim(),
                request.getContent().trim(),
                request.getStation().trim(),
                request.getType()
        );
        qnaRepository.saveAndFlush(qna);
        return QnaResponse.from(qna);
    }

    @Transactional
    public QnaResponse update(Long id, QnaRequest request) {
        MemberPrincipal member = currentMember();
        Qna qna = find(id);
        if (isAdmin(member) || qna.getCreatedBy() == null
                || !member.getMemberId().equals(qna.getCreatedBy().getId())) {
            throw new AccessDeniedException("작성한 본인만 질문 내용을 수정할 수 있습니다.");
        }
        qna.update(
                request.getTitle().trim(),
                request.getContent().trim(),
                request.getStation().trim(),
                request.getType()
        );
        qnaRepository.flush();
        return QnaResponse.from(qna);
    }

    @Transactional
    public QnaResponse answer(Long id, QnaAnswerRequest request) {
        MemberPrincipal member = currentMember();
        if (!isAdmin(member)) {
            throw new AccessDeniedException("관리자만 답변을 등록할 수 있습니다.");
        }
        Qna qna = find(id);
        qna.qnaAnswered(request.getReplyContent().trim());
        return QnaResponse.from(qna);
    }

    @Transactional
    public QnaResponse updateAnswer(Long id, QnaAnswerRequest request) {
        MemberPrincipal member = currentMember();
        if (!isAdmin(member)) {
            throw new AccessDeniedException("관리자만 답변을 수정할 수 있습니다.");
        }
        Qna qna = find(id);
        qna.updateReply(request.getReplyContent().trim());
        qnaRepository.flush();
        return QnaResponse.from(qna);
    }

    @Transactional
    public void delete(Long id) {
        MemberPrincipal member = currentMember();
        Qna qna = find(id);
        checkReadAccess(qna, member);
        qnaRepository.delete(qna);
    }

    private void checkReadAccess(Qna qna, MemberPrincipal member) {
        if (!isAdmin(member) && (qna.getCreatedBy() == null
                || !member.getMemberId().equals(qna.getCreatedBy().getId()))) {
            throw new AccessDeniedException("작성자 본인과 관리자만 접근할 수 있습니다.");
        }
    }

    private String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private Qna find(Long id) {
        return qnaRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 질문을 찾을 수 없습니다.")
        );
    }

    // 요청 본문이 아닌 서버의 로그인 세션에서 현재 회원을 확인한다.
    private MemberPrincipal currentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof MemberPrincipal member)
                || member.getMemberId() == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return member;
    }

    private boolean isAdmin(MemberPrincipal member) {
        return member.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
