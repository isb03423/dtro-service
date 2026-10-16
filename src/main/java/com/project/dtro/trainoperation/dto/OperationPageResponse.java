package com.project.dtro.trainoperation.dto;

import com.project.dtro.trainoperation.domain.TrainOperation;
import org.springframework.data.domain.Page;

import java.util.List;

public record OperationPageResponse(List<OperationResponse> content, int page, int size,
                                    long totalElements, int totalPages) {
    public static OperationPageResponse from(Page<TrainOperation> page) {
        return new OperationPageResponse(page.getContent().stream().map(OperationResponse::from).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
