package com.project.dtro.common.domain;

import com.project.dtro.user.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    private Member createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private Member updatedBy;

    public String getCreatedByName() {
        return createdBy == null ? null : createdBy.getName();
    }

    public String getUpdatedByName() {
        return updatedBy == null ? null : updatedBy.getName();
    }

    public String getModifiedByName() {
        if (updatedBy == null || updatedAt == null
                || (createdAt != null && !updatedAt.isAfter(createdAt))) {
            return null;
        }
        return updatedBy.getName();
    }

    protected void markUpdatedBy(Member member) {
        this.updatedBy = member;
    }
}
