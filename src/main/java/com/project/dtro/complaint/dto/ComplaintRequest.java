package com.project.dtro.complaint.dto;

import com.project.dtro.complaint.domain.ComplaintType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ComplaintRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    private ComplaintType type;

    @NotBlank
    @Size(max = 30)
    private String station;

    @NotBlank
    private String content;
}
