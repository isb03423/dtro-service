package com.project.dtro.complaint.domain;

import com.project.dtro.common.domain.AuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "complaints")
@Getter
@NoArgsConstructor
public class Complaint extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "station", nullable = false, length = 30)
    private String station;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ComplaintType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.PENDING;

    @Column(name = "reply_content", columnDefinition = "TEXT")
    private String replyContent;

    public Complaint(String title, String content, String station, ComplaintType type) {
        this.title = title;
        this.content = content;
        this.station = station;
        this.type = type;
    }

    public void update(String title, String content, String station, ComplaintType type) {
        this.title = title;
        this.content = content;
        this.station = station;
        this.type = type;
    }

    public void complaintAnswered(String replyContent) {
        if (this.status == ComplaintStatus.ANSWERED) {
            throw new IllegalStateException("이미 답변한 민원입니다.");
        }
        this.replyContent = replyContent;
        this.status = ComplaintStatus.ANSWERED;
    }

    public void updateReply(String replyContent) {
        if (this.status != ComplaintStatus.ANSWERED) {
            throw new IllegalStateException("등록된 답변이 없습니다.");
        }
        this.replyContent = replyContent;
    }
}
