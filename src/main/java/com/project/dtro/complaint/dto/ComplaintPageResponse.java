package com.project.dtro.complaint.dto;

import com.project.dtro.complaint.domain.Complaint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class ComplaintPageResponse {
    private final List<ComplaintResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPage;

    public static ComplaintPageResponse from(Page<Complaint> page) {
        return new ComplaintPageResponse(
                page.getContent().stream().map(ComplaintResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
