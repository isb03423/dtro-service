package com.project.dtro.complaint.dto;

import com.project.dtro.complaint.domain.Complaint;
import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.domain.ComplaintType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ComplaintResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String station;
    private final ComplaintType type;
    private final ComplaintStatus status;
    private final String replyContent;
    private final LocalDateTime createdAt;
    private final Long createdBy;
    private final LocalDateTime updatedAt;

    public static ComplaintResponse from(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getTitle(),
                complaint.getContent(),
                complaint.getStation(),
                complaint.getType(),
                complaint.getStatus(),
                complaint.getReplyContent(),
                complaint.getCreatedAt(),
                complaint.getCreatedBy() == null ? null : complaint.getCreatedBy().getId(),
                complaint.getUpdatedAt()
        );
    }

}
