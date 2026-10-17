package com.project.dtro.trainoperation.dto;

import com.project.dtro.trainoperation.domain.TrainOperation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class OperationPageResponse {
    private final List<OperationResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    // 운행정보 조회 결과와 페이지 정보를 목록 응답으로 변환한다.
    public static OperationPageResponse from(Page<TrainOperation> page) {
        return new OperationPageResponse(
                page.getContent().stream().map(OperationResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
