package com.project.dtro.complaint.dto;

import com.project.dtro.complaint.domain.Complaint;
import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.domain.ComplaintType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ComplaintResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String station;
    private final List<String> lineCodes;
    private final ComplaintType type;
    private final ComplaintStatus status;
    private final String replyContent;
    private final LocalDateTime createdAt;
    private final Long createdBy;
    private final String createdByName;
    private final LocalDateTime updatedAt;
    private final String updatedByName;

    // 공개 목록에는 본문과 답변을 포함하지 않는다.
    public static ComplaintResponse summary(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getId(), complaint.getTitle(), null, complaint.getStation(), List.of(),
                complaint.getType(), complaint.getStatus(), null, complaint.getCreatedAt(),
                complaint.getCreatedBy() == null ? null : complaint.getCreatedBy().getId(),
                null, complaint.getUpdatedAt(), null
        );
    }

    public static ComplaintResponse from(Complaint complaint, List<String> lineCodes) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getTitle(),
                complaint.getContent(),
                complaint.getStation(),
                lineCodes,
                complaint.getType(),
                complaint.getStatus(),
                complaint.getReplyContent(),
                complaint.getCreatedAt(),
                complaint.getCreatedBy() == null ? null : complaint.getCreatedBy().getId(),
                complaint.getCreatedByName(),
                complaint.getUpdatedAt(),
                complaint.getModifiedByName()
        );
    }

}
