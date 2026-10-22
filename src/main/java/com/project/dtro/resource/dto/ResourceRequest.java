package com.project.dtro.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResourceRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private Boolean publicYn;

    private List<Long> removedAttachmentIds;
}
