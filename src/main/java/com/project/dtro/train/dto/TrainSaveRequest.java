package com.project.dtro.train.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TrainSaveRequest {
    @Valid
    @NotNull
    private List<TrainRowRequest> rows = new ArrayList<>();
}
