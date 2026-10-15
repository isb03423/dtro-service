package com.project.dtro.train.dto;

import com.project.dtro.train.domain.Train;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class TrainPageResponse {
    private final List<TrainResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    // 열차 조회 결과와 페이지 정보를 목록 응답으로 변환한다.
    public static TrainPageResponse from(Page<Train> page) {
        return new TrainPageResponse(
                page.getContent().stream().map(TrainResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
