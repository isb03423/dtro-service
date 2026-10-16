package com.project.dtro.train.dto;

import com.project.dtro.train.domain.TrainStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrainSearchRequest {
    private TrainStatus status;
    private String keyword;
    private int page = 0;
    private int size = 20;
    @NotBlank
    private String sort = "id,desc";
}
