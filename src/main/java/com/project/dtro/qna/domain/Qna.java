package com.project.dtro.qna.domain;

import com.project.dtro.common.domain.AuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qnas")
@Getter
@NoArgsConstructor
public class Qna extends AuditEntity {

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

    public Qna(String title, String content, String station, QnaType type) {
        this.title = title;
        this.content = content;
        this.station = station;
        this.type = type;
    }

    public void update(String title, String content, String station, QnaType type) {
        if (this.status != QnaStatus.PENDING) {
            throw new IllegalStateException("답변 대기 상태에서만 질문을 수정할 수 있습니다.");
        }
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
