package com.project.dtro.resource.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BulkResourcePublicRequest {
    @NotEmpty
    private List<@NotNull Long> ids;

    @NotNull
    private Boolean publicYn;
}
