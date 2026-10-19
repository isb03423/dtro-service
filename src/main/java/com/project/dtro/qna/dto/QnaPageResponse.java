package com.project.dtro.qna.dto;

import com.project.dtro.qna.domain.Qna;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class QnaPageResponse {
    private final List<QnaResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPage;

    public static QnaPageResponse from(Page<Qna> page) {
        return new QnaPageResponse(
                page.getContent().stream().map(QnaResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
