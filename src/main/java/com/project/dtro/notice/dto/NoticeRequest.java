package com.project.dtro.notice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NoticeRequest {

    private String title;
    private String content;
    private Boolean importantYn;
    private Boolean publicYn;
    // 요청에서 시작일을 생략해도 DB 필수값이 null로 내려가지 않도록 기본값을 둔다.
    private LocalDateTime publishStartAt = LocalDateTime.now();
    private LocalDateTime publishEndAt;
    private Long createdBy;

    // 만약 Lombok의 기본 Getter/Setter 외에 명시적으로 is 접두사 메서드가 필요하다면 아래와 같이 추가할 수 있습니다.
    public boolean isImportantYn() {
        return Boolean.TRUE.equals(this.importantYn);
    }

    public boolean isPublicYn() {
        return Boolean.TRUE.equals(this.publicYn);
    }
}
