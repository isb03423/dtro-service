package com.project.dtro.notice.dto;

import com.project.dtro.notice.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NoticeResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final boolean importantYn;
    private final boolean publicYn;
    private final LocalDateTime createdAt;
    private final String createdByName;
    private final LocalDateTime updatedAt;
    private final String updatedByName;

    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getContent(),
                Boolean.TRUE.equals(notice.getImportantYn()),
                Boolean.TRUE.equals(notice.getPublicYn()),
                notice.getCreatedAt(),
                notice.getCreatedByName(),
                notice.getUpdatedAt(),
                notice.getModifiedByName()
        );
    }
}
