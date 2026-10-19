package com.project.dtro.complaint.service;

import com.project.dtro.complaint.domain.Complaint;
import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.domain.ComplaintType;
import com.project.dtro.complaint.dto.ComplaintAnswerRequest;
import com.project.dtro.complaint.dto.ComplaintPageResponse;
import com.project.dtro.complaint.dto.ComplaintOverviewResponse;
import com.project.dtro.complaint.dto.ComplaintRequest;
import com.project.dtro.complaint.dto.ComplaintResponse;
import com.project.dtro.complaint.repository.ComplaintRepository;
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
public class ComplaintService {
    private static final Set<String> SORT_FIELDS =
            Set.of("id", "title", "station", "type", "status", "createdAt", "updatedAt");

    private final ComplaintRepository complaintRepository;

    // 집계와 최근 5건만 조회하며 전체 목록을 메모리에 적재하지 않는다.
    public ComplaintOverviewResponse getOverview() {
        Map<ComplaintStatus, Long> counts = new EnumMap<>(ComplaintStatus.class);
        for (ComplaintStatus status : ComplaintStatus.values()) {
            counts.put(status, complaintRepository.countByStatus(status));
        }
        PageRequest pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        List<ComplaintOverviewResponse.RecentComplaint> recent = complaintRepository.findAll(pageable)
                .getContent().stream()
                .map(item -> new ComplaintOverviewResponse.RecentComplaint(item.getId(), item.getTitle(),
                        item.getStation(), item.getStatus(), item.getCreatedAt()))
                .toList();
        return new ComplaintOverviewResponse(Map.copyOf(counts), recent);
    }

    public ComplaintPageResponse search(
            ComplaintType type,
            ComplaintStatus status,
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

        return ComplaintPageResponse.from(
                complaintRepository.search(blankToNull(keyword), status, type, pageable)
        );
    }

    public ComplaintResponse get(Long id) {
        return ComplaintResponse.from(find(id));
    }

    @Transactional
    public ComplaintResponse create(ComplaintRequest request) {
        Complaint complaint = new Complaint(
                request.getTitle().trim(),
                request.getContent().trim(),
                request.getStation().trim(),
                request.getType()
        );
        complaintRepository.saveAndFlush(complaint);
        return ComplaintResponse.from(complaint);
    }

    @Transactional
    public ComplaintResponse update(Long id, ComplaintRequest request) {
        MemberPrincipal member = currentMember();
        Complaint complaint = find(id);
        checkOwner(complaint, member);
        if (complaint.getStatus() != ComplaintStatus.PENDING) {
            throw new IllegalStateException("답변 대기 상태에서만 민원을 수정할 수 있습니다.");
        }
        complaint.update(
                request.getTitle().trim(),
                request.getContent().trim(),
                request.getStation().trim(),
                request.getType()
        );
        complaintRepository.flush();
        return ComplaintResponse.from(complaint);
    }

    @Transactional
    public ComplaintResponse answer(Long id, ComplaintAnswerRequest request) {
        MemberPrincipal member = currentMember();
        if (!isAdmin(member)) {
            throw new AccessDeniedException("관리자만 답변을 등록할 수 있습니다.");
        }
        Complaint complaint = find(id);
        complaint.complaintAnswered(request.getReplyContent().trim());
        return ComplaintResponse.from(complaint);
    }

    @Transactional
    public void delete(Long id) {
        complaintRepository.delete(find(id));
    }

    private String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private Complaint find(Long id) {
        return complaintRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 민원을 찾을 수 없습니다.")
        );
    }

    private void checkOwner(Complaint complaint, MemberPrincipal member) {
        if (isAdmin(member) || complaint.getCreatedBy() == null
                || !member.getMemberId().equals(complaint.getCreatedBy().getId())) {
            throw new AccessDeniedException("작성한 본인만 민원 내용을 수정할 수 있습니다.");
        }
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
