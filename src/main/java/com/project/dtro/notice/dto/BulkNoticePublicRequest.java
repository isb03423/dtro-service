package com.project.dtro.notice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BulkNoticePublicRequest {
    @NotEmpty
    private List<@NotNull Long> ids;

    @NotNull
    private Boolean publicYn;
}
