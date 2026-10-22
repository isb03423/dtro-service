package com.project.dtro.resource.dto;

import com.project.dtro.resource.domain.Resource;
import com.project.dtro.resource.domain.Attachment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;

@Getter
@Builder
public class ResourceResponse {

    private Long resourceId;
    private String title;
    private String content;
    private Boolean publicYn;
    private LocalDateTime createdAt;
    private String createdByName;
    private LocalDateTime updatedAt;
    private String updatedByName;
    private List<AttachmentResponse> attachments;

    public static ResourceResponse from(Resource entity) {
        return from(entity, attachment -> null);
    }

    public static ResourceResponse from(Resource entity, Function<Attachment, Long> sizeResolver) {
        List<AttachmentResponse> attachmentResponses = entity.getAttachments() != null ?
                entity.getAttachments().stream()
                        .map(attachment -> AttachmentResponse.from(attachment, sizeResolver.apply(attachment)))
                        .collect(Collectors.toList()) : Collections.emptyList();

        return ResourceResponse.builder()
                .resourceId(entity.getResourceId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .publicYn(entity.getPublicYn())
                .createdAt(entity.getCreatedAt())
                .createdByName(entity.getCreatedByName())
                .updatedAt(entity.getUpdatedAt())
                .updatedByName(entity.getModifiedByName())
                .attachments(attachmentResponses)
                .build();
    }
}
