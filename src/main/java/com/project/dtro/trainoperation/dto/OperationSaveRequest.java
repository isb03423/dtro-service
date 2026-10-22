package com.project.dtro.trainoperation.dto;

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
public class OperationSaveRequest {
    @Valid
    @NotNull
    private List<OperationRowRequest> rows = new ArrayList<>();
}
