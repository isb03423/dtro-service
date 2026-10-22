package com.project.dtro.qna.dto;

import com.project.dtro.qna.domain.Qna;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.domain.QnaType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class QnaResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String station;
    private final List<String> lineCodes;
    private final QnaType type;
    private final QnaStatus status;
    private final String replyContent;
    private final LocalDateTime createdAt;
    private final Long createdBy;
    private final String createdByName;
    private final LocalDateTime updatedAt;
    private final String updatedByName;

    // 공개 목록에는 본문과 답변을 포함하지 않는다.
    public static QnaResponse summary(Qna qna) {
        return new QnaResponse(
                qna.getId(), qna.getTitle(), null, qna.getStation(), List.of(),
                qna.getType(), qna.getStatus(), null, qna.getCreatedAt(),
                qna.getCreatedBy() == null ? null : qna.getCreatedBy().getId(),
                null, qna.getUpdatedAt(), null
        );
    }

    public static QnaResponse from(Qna qna, List<String> lineCodes) {
        return new QnaResponse(
                qna.getId(),
                qna.getTitle(),
                qna.getContent(),
                qna.getStation(),
                lineCodes,
                qna.getType(),
                qna.getStatus(),
                qna.getReplyContent(),
                qna.getCreatedAt(),
                qna.getCreatedBy() == null ? null : qna.getCreatedBy().getId(),
                qna.getCreatedByName(),
                qna.getUpdatedAt(),
                qna.getModifiedByName()
        );
    }

}
