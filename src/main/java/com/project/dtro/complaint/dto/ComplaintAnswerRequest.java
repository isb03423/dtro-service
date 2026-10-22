package com.project.dtro.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ComplaintAnswerRequest {

    @NotBlank
    @Size(min = 10, message = "답변은 10글자 이상 작성하세요")
    private String replyContent;
}
