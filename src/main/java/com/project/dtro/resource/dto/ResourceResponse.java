package com.project.dtro.resource.dto;

import com.project.dtro.resource.domain.Resource;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ResourceResponse {

    private Long resourceId;
    private String title;
    private String content;
    private Boolean publicYn;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private List<AttachmentResponse> attachments;

    public static ResourceResponse from(Resource entity) {
        List<AttachmentResponse> attachmentResponses = entity.getAttachments() != null ?
                entity.getAttachments().stream()
                        .map(AttachmentResponse::from)
                        .collect(Collectors.toList()) : Collections.emptyList();

        return ResourceResponse.builder()
                .resourceId(entity.getResourceId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .publicYn(entity.getPublicYn())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().toString() : null)
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy().toString() : null)
                .attachments(attachmentResponses)
                .build();
    }
}