package com.project.dtro.complaint.service;

import com.project.dtro.complaint.domain.Complaint;
import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.domain.ComplaintType;
import com.project.dtro.complaint.dto.ComplaintAnswerRequest;
import com.project.dtro.complaint.dto.ComplaintPageResponse;
import com.project.dtro.complaint.dto.ComplaintRequest;
import com.project.dtro.complaint.dto.ComplaintResponse;
import com.project.dtro.complaint.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintService {
    private static final Set<String> SORT_FIELDS =
            Set.of("id", "title", "station", "type", "status", "createdAt", "updatedAt");

    private final ComplaintRepository complaintRepository;

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
        Complaint complaint = find(id);
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

    private void checkOwner(Complaint complaint, Long id) {
    }
}
