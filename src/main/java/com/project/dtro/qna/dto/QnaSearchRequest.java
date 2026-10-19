package com.project.dtro.qna.dto;

import com.project.dtro.qna.domain.QnaStatus;
import com.project.dtro.qna.domain.QnaType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QnaSearchRequest {
    private QnaStatus qnaStatus;
    private QnaType qnaType;
    private String keyword;
    private int page = 0;
    private int size = 20;
    @NotBlank
    private String sort = "id, desc";
}
