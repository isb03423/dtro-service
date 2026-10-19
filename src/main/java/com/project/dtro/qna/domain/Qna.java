package com.project.dtro.qna.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "qnas")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "station", nullable = false, length = 30)
    private String station;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private QnaType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QnaStatus status = QnaStatus.PENDING;

    @Column(name = "reply_content", columnDefinition = "TEXT")
    private String replyContent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    public Qna(String title, String content, String station, QnaType type) {
        this.title = title;
        this.content = content;
        this.station = station;
        this.type = type;
    }

    public void update(String title, String content, String station, QnaType type) {
        this.title = title;
        this.content = content;
        this.station = station;
        this.type = type;
    }

    public void qnaAnswered(String replyContent) {
        if (this.status == QnaStatus.ANSWERED) {
            throw new IllegalStateException("이미 답변한 질문입니다.");
        }
        this.replyContent = replyContent;
        this.status = QnaStatus.ANSWERED;
    }

    public void updateReply(String replyContent) {
        if (this.status != QnaStatus.ANSWERED) {
            throw new IllegalStateException("등록된 답변이 없습니다.");
        }
        this.replyContent = replyContent;
    }
}
