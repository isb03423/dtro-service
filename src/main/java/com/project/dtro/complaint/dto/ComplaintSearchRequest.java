package com.project.dtro.complaint.dto;

import com.project.dtro.complaint.domain.ComplaintStatus;
import com.project.dtro.complaint.domain.ComplaintType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ComplaintSearchRequest {
    private ComplaintStatus complaintStatus;
    private ComplaintType complaintType;
    private String keyword;
    private int page = 0;
    private int size = 20;
    private String sort;
}
