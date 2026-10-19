package com.project.dtro.qna.dto;

import com.project.dtro.qna.domain.Qna;
import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.domain.QnaType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class QnaResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String station;
    private final QnaType type;
    private final QnaStatus status;
    private final String replyContent;
    private final LocalDateTime createdAt;
    private final Long createdBy;
    private final LocalDateTime updatedAt;

    public static QnaResponse from(Qna qna) {
        return new QnaResponse(
                qna.getId(),
                qna.getTitle(),
                qna.getContent(),
                qna.getStation(),
                qna.getType(),
                qna.getStatus(),
                qna.getReplyContent(),
                qna.getCreatedAt(),
                qna.getCreatedBy(),
                qna.getUpdatedAt()
        );
    }

}
