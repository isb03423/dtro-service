package com.project.dtro.qna.service;

import com.project.dtro.qna.domain.Qna;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.domain.QnaType;
import com.project.dtro.qna.dto.QnaAnswerRequest;
import com.project.dtro.qna.dto.QnaPageResponse;
import com.project.dtro.qna.dto.QnaRequest;
import com.project.dtro.qna.dto.QnaResponse;
import com.project.dtro.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {
    private static final Set<String> SORT_FIELDS =
            Set.of("id", "title", "station", "type", "status", "createdAt", "updatedAt");

    private final QnaRepository qnaRepository;

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
        return QnaResponse.from(find(id));
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
        Qna qna = find(id);
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
        Qna qna = find(id);
        qna.qnaAnswered(request.getReplyContent().trim());
        return QnaResponse.from(qna);
    }

    @Transactional
    public void delete(Long id) {
        qnaRepository.delete(find(id));
    }

    private String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private Qna find(Long id) {
        return qnaRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 질문을 찾을 수 없습니다.")
        );
    }

}
