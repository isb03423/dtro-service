package com.project.dtro.complaint.domain;

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
@Table(name = "complaints")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 30)
    private String station;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.PENDING;

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

    public void complaintAnswered() {
        this.status = ComplaintStatus.ANSWERED;
    }
}
