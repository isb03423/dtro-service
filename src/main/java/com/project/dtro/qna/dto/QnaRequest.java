package com.project.dtro.qna.dto;

import com.project.dtro.qna.domain.QnaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QnaRequest {

    @NotBlank(message = "제목을 입력하세요")
    @Size(max = 100)
    private String title;

    @NotNull(message = "질문 유형을 선택하세요.")
    private QnaType type;

    @Size(max = 30)
    private String station;

    @NotBlank(message = "내용을 입력하세요")
    private String content;
}
