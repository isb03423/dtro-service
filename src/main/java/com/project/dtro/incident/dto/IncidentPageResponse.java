package com.project.dtro.incident.dto;

import com.project.dtro.incident.domain.Incident;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class IncidentPageResponse {
    private final List<IncidentResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    // 운행 장애 조회 결과와 페이지 정보를 목록 응답으로 변환한다.
    public static IncidentPageResponse from(Page<Incident> page) {
        return new IncidentPageResponse(
                page.getContent().stream().map(IncidentResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
