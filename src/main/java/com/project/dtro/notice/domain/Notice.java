package com.project.dtro.notice.domain;

import com.project.dtro.common.domain.AuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "important_yn", nullable = false)
    private Boolean importantYn; // Boolean 타입으로 변경

    @Column(name = "public_yn", nullable = false)
    private Boolean publicYn; // Boolean 타입으로 변경

    @Column(name = "publish_start_at", nullable = false)
    private LocalDateTime publishStartAt;

    @Column(name = "publish_end_at")
    private LocalDateTime publishEndAt;

    @Builder
    public Notice(String title, String content, Boolean importantYn, Boolean publicYn,
                  LocalDateTime publishStartAt, LocalDateTime publishEndAt) {
        this.title = title;
        this.content = content;
        this.importantYn = importantYn != null ? importantYn : false;
        this.publicYn = publicYn != null ? publicYn : false;
        this.publishStartAt = publishStartAt != null ? publishStartAt : LocalDateTime.now();
        this.publishEndAt = publishEndAt;
    }

    public void update(String title, String content, Boolean importantYn, Boolean publicYn,
                       LocalDateTime publishStartAt, LocalDateTime publishEndAt) {
        this.title = title;
        this.content = content;
        this.importantYn = importantYn;
        this.publicYn = publicYn;
        if (publishStartAt != null) {
            this.publishStartAt = publishStartAt;
        }
        this.publishEndAt = publishEndAt;
    }

    public void updatePublicYn(Boolean publicYn) {
        this.publicYn = publicYn;
    }

    @PrePersist
    private void fn_ensurePublishStartAt() {
        if (publishStartAt == null) {
            publishStartAt = LocalDateTime.now();
        }
    }
}
