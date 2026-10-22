package com.project.dtro.resource.dto;

import com.project.dtro.resource.domain.Attachment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AttachmentResponse {

    private Long attachmentId;
    private String originalName;
    private String filePath;
    private LocalDateTime createdAt;
    private String createdBy;

    public static AttachmentResponse from(Attachment entity) {
        return AttachmentResponse.builder()
                .attachmentId(entity.getAttachmentId())
                .originalName(entity.getOriginalName())
                .filePath("/api/v1/resources/" + entity.getResource().getResourceId()
                        + "/attachments/" + entity.getAttachmentId())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getName() : null)
                .build();
    }
}
